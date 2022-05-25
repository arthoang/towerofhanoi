package controller;

import db.DbHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.util.Pair;
import model.Disc;
import model.Game;
import model.GameStatus;
import model.Peg;
import ui.UIHelper;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GameController {
   public static final String PEG_ID_PREFIX = "PEG_";
   private int begin_peg = 0;
   private int end_peg;
   private Map<String, Peg> pegs;
   private int numberOfDiscs;
   private int numberOfPegs;
   private boolean gameInProgress;
   private Long duration;
   private String userId;

   private DbHelper dbHelper;

   private long startTime;
   private long elapsedTime;

   //String property to update UI timer
   private SimpleStringProperty elapsedTimeStr = new SimpleStringProperty(this, "elapsedTimeStr");

   //Boolean property to bind UI button disable status
   private SimpleBooleanProperty isGameInProgressProp = new SimpleBooleanProperty(this, "isGameInProgressProp");

   public SimpleBooleanProperty getIsGameInProgressProp() {
      return this.isGameInProgressProp;
   }

   public void setIsGameInProgress(boolean bool) {
      this.isGameInProgressProp.set(bool);
   }

   public boolean getIsGameInProgressValue() {
      return this.isGameInProgressProp.get();
   }

   public GameController(int pegs, int disc, String userId) {
      this.duration = null;
      dbHelper = new DbHelper();
      this.numberOfDiscs = disc;
      this.numberOfPegs = pegs;
      this.userId = userId;
      gameInProgress = true;
      end_peg = pegs - 1;
      this.pegs = new LinkedHashMap<>();
      //default game
      for (int count = 0; count < pegs; count++) {
         Peg p = new Peg(PEG_ID_PREFIX + count);
         this.pegs.put(p.getPegId(), p);
      }
      //push discs onto first peg
      Peg p1 = this.pegs.get(PEG_ID_PREFIX + "0");
      for (int count = disc; count > 0; count--) {
         p1.pushDisc(new Disc(count));
      }
   }

   //seperate non-FX thread to calculate elapsed time
   Thread timerThread;
   Task timerTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {

         startTime = System.currentTimeMillis();

         while (isGameInProgress()) {
            if (getDuration() != null) {
               elapsedTime = getDuration() + System.currentTimeMillis() - startTime;
            } else {
               elapsedTime = System.currentTimeMillis() - startTime;
            }

            //update elapsedTimeStr Property
            Platform.runLater(() -> setElapsedTimeStr(UIHelper.longToHhMmSs(elapsedTime)));

            Thread.sleep(1000);

         }
         return null;
      }

   };

   public int getBeginPeg() {
      return this.begin_peg;
   }

   public int getEndPeg() {
      return this.end_peg;
   }

   public long getElapsedTime() {
      return this.elapsedTime;
   }

   public String getElapsedTimeStr() {
      return this.elapsedTimeStr.get();
   }

   public void setElapsedTimeStr(String timeStr) {
      this.elapsedTimeStr.set(timeStr);
   }

   public StringProperty getElapsedTimeStrProp() {
      return this.elapsedTimeStr;
   }

   public boolean isGameInProgress() {
      return this.gameInProgress;
   }

   public void setGameInProgress(boolean inProgress) {
      this.gameInProgress = inProgress;

   }

   public boolean checkGameEnd() {
      //get the end peg
      Peg endPeg = this.pegs.get(PEG_ID_PREFIX + end_peg);
      if (endPeg.getNumberOfDiscs() == numberOfDiscs && endPeg.peekDisc().getDiscSize() == 1) {
         this.gameInProgress = false;
         //dispatch to UI
         Platform.runLater(() -> setIsGameInProgress(false));
         return true;
      } else {
         return false;
      }
   }

   public boolean isMovable(String fromPegId, String toPegId) {
      //take a look at the top most disc in the target peg
      Disc topMostTo = pegs.get(toPegId).peekDisc();

      Disc topMostFrom = pegs.get(fromPegId).peekDisc();
      if (topMostTo == null) {
         return true;
      }
      if (topMostFrom.getDiscSize() < topMostTo.getDiscSize()) {
         return true;
      }
      return false;
   }


   public void moveDisc(String fromPegId, String toPegId) {
      Disc d = pegs.get(fromPegId).popDisc();
      pegs.get(toPegId).pushDisc(d);
   }

   //take in a disc and find out from which peg the disc is from
   public String discFromPeg(String discId) {
     List<Map.Entry<String, Peg>> result = this.pegs.entrySet()
         .stream()
         .filter(e -> e.getValue().getPegId().equals(discId))
         .toList();
      if (result.size() > 0) return result.get(0).getKey();
      //not found
      return null;
   }

   public int getNumberOfDiscs() {
      return this.numberOfDiscs;
   }

   public int getNumberOfPegs() {
      return this.numberOfPegs;
   }

   public Map<String, Peg> getPegs() {
      return pegs;
   }

   public void startTimer() {
      timerThread = new Thread(timerTask);
      timerThread.setDaemon(true);
      timerThread.start();
   }

   public void stopTimer() {
      timerThread.stop();
   }

   public void saveGame() {
      dbHelper.saveGame(userId, pegs, numberOfDiscs, numberOfPegs, elapsedTime, !isGameInProgress());
   }

   public List<Pair<String, Long>> getLeaderBoard() {
      return dbHelper.getLeaderBoard(numberOfDiscs, numberOfPegs);
   }

   public void loadGame(String userId) {
      Game game = dbHelper.loadGame(userId);
      this.numberOfDiscs = game.getDiscs();
      this.numberOfPegs = game.getPegs();
      this.duration = game.getDuration();
      this.gameInProgress = !game.getFinish();
      this.end_peg = numberOfPegs - 1;
      List<GameStatus> statuses = game.getGameStatuses();
      pegs = new LinkedHashMap<>();

      statuses.stream().map(s -> s.getPegId()).collect(Collectors.toSet())
         .stream().sorted()
         .forEach(pegId -> {
         //create peg
         Peg p = new Peg(pegId);
         pegs.put(pegId, p);
      });

      for (Map.Entry<String, Peg> entry : pegs.entrySet()) {
         String pegId = entry.getKey();
         Peg p = entry.getValue();
         statuses.stream().filter(s -> s.getPegId() == pegId)
            .sorted(new Comparator<GameStatus>() {
               @Override
               public int compare(GameStatus o1, GameStatus o2) {
                  if (o1.getDiscSize() > o2.getDiscSize()) {
                     return -1;
                  } else if (o1.getDiscSize() < o2.getDiscSize()) {
                     return 1;
                  } else {
                     return 0;
                  }
               }
            }).forEach(s -> {
               Disc d = new Disc(s.getDiscSize());
               p.pushDisc(d);
            });
      }

   }

   public Long getDuration() {
      return this.duration;
   }

   @Override
   public String toString() {
      String result = "";
      for (Map.Entry<String, Peg> entry : pegs.entrySet()) {
         result = result + entry.getKey() + ": " + entry.getKey() + "\n";
      }
      return result;
   }

}
