package ui;

import controller.GameController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import model.Disc;
import model.Peg;


import java.awt.*;
import java.util.*;
import java.util.List;

public class UiController {

   //constants
   public final double MAX_DISC_WIDTH = 180;
   public final double DISC_HEIGHT = 20;
   public final double PEG_BOX_HEIGHT = 230;
   public final double PEG_BOX_WIDTH = 200;
   public final double BASE_HEIGHT = 10;

   public final String VBOX_ID_PREFIX = "DB_";

   private Optional<Rectangle> selectedDisc = Optional.empty();
   private Optional<StackPane> selectedFrom = Optional.empty();

   //keep pegs of game in a list. Each element is an ObservableList of Rectangle objects, aka Discs
   private Map<String, ObservableList<Node>> gamePegs;
   private GameController gameController;
   private HBox gameBox;
   private HBox leaderBoardHBox;

   //default constructor
   public UiController() {
      this.gamePegs = new LinkedHashMap<>();
   }

   public ObservableList<Node> getDiscsInPeg(String pegId) {
      return this.gamePegs.get(pegId);
   }

   public Map<String, ObservableList<Node>> getPegs() {
      return this.gamePegs;
   }

   public void initialize(GameController gameController, HBox gameBox, HBox leaderBoardHBox) {
      this.gameController = gameController;
      this.gameBox = gameBox;
      this.leaderBoardHBox = leaderBoardHBox;

      //default state of a new game. Discs are always loaded at peg 0
      //get number of disc in peg 0, to calculate the base factor for disc width
      int numberOfDisc = gameController.getNumberOfDiscs();

      double discWidthBase = MAX_DISC_WIDTH / numberOfDisc + 2;

      for (Map.Entry<String, Peg> entry : gameController.getPegs().entrySet()) {
         ObservableList<Node> uiPeg = FXCollections.observableArrayList();
         Peg modelPeg = entry.getValue();
         String pegId = modelPeg.getPegId();

         Iterator<Disc> it = modelPeg.getDiscIterator();
         while(it.hasNext()) {
            Disc modelDisc = it.next();
            Rectangle uiDisc = UIHelper.getRectangleShape(discWidthBase * modelDisc.getDiscSize(), DISC_HEIGHT, modelDisc.getColor(), 20.0, 20.0);
            uiDisc.setId(modelDisc.getDiscId());
            uiPeg.add(uiDisc);
         }

         gamePegs.put(pegId, uiPeg);
      }
   }



   public void registerPegListeners() {

      gameBox.getChildren().filtered(c -> (c.getId() != null && c.getId().startsWith(GameController.PEG_ID_PREFIX)))
         .forEach(peg -> {
            peg.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseClicked);
            peg.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseEntered);
            peg.addEventFilter(MouseEvent.MOUSE_EXITED, mouseExited);
         });
   }

   public void deregisterPegListeners() {
      gameBox.getChildren().filtered(c -> (c.getId() != null && c.getId().startsWith(GameController.PEG_ID_PREFIX)))
         .forEach(peg -> {

            peg.removeEventHandler(MouseEvent.MOUSE_CLICKED, mouseClicked);
            peg.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEntered);
            peg.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExited);
         });
   }

   private EventHandler<MouseEvent> mouseEntered = e -> {
      StackPane source = (StackPane) e.getSource();
      source.setBackground(new Background(new BackgroundFill(Color.rgb(196, 196, 196, 0.4), CornerRadii.EMPTY, Insets.EMPTY)));
      e.consume();
   };

   private EventHandler<MouseEvent> mouseExited = e -> {
      StackPane source = (StackPane) e.getSource();
      if (selectedFrom.isPresent()) {
         if (!selectedFrom.get().getId().equals(source.getId())) {
            source.setBackground(Background.EMPTY);
         }
      } else {
         source.setBackground(Background.EMPTY);
      }
      e.consume();
   };

   private EventHandler<MouseEvent> mouseClicked = e -> {
      StackPane source = (StackPane) e.getSource();

      if (selectedDisc.isPresent()) {
         String toPegId = source.getId();
         String fromPegId = selectedFrom.get().getId();
         selectedFrom.get().setBackground(Background.EMPTY);
         //remove background
         moveSelectedDisc(fromPegId, toPegId);
         //reset selected
         selectedDisc = Optional.empty();
         selectedFrom = Optional.empty();
         //check if game ends
         boolean gameEnd = gameController.checkGameEnd();
         if (gameEnd) {
            deregisterPegListeners();
            //save game
            gameController.saveGame();
         }
      } else {
         selectedDisc = Optional.ofNullable(getTopMostDisc(source.getId()));
         selectedFrom = Optional.ofNullable(source);
         //set background
         source.setBackground(new Background(new BackgroundFill(Color.rgb(196, 196, 196, 0.4), CornerRadii.EMPTY, Insets.EMPTY)));
      }
      e.consume();
   };

   private void moveSelectedDisc(String fromPeg, String toPeg) {
      if (gameController.isMovable(fromPeg, toPeg)) {
         //remove the disc
         removeDiskFromSource(fromPeg);
         //add selected disc to target
         addSelectedDiskToTarget(toPeg);
         //move disc in data model
         gameController.moveDisc(fromPeg, toPeg);
      }
   }

   public void uiMoveDisc(String fromPeg, String toPeg) {
      //remove the disc
      Rectangle top = popDiskFromSource(fromPeg);
      //add disc to target
      addDiskToTarget(top, toPeg);
      //move disc in data model
      //gameController.moveDisc(fromPeg, toPeg);
   }

   void removeDiskFromSource(String pegId) {
      ObservableList<Node> fromPeg = this.gamePegs.get(pegId);
      if (fromPeg.size() > 0) {
         //remove the first element in the list
         fromPeg.remove(0, 1);
      }
   }

   Rectangle popDiskFromSource(String pegId) {
      ObservableList<Node> fromPeg = this.gamePegs.get(pegId);
      if (fromPeg.size() > 0) {
         //get the first element in the list
         Rectangle top = (Rectangle) fromPeg.get(0);
         //remove it
         fromPeg.remove(0, 1);
         return top;
      }
      return null;
   }

   private void addSelectedDiskToTarget(String pegId) {
      ObservableList<Node> toPeg = this.gamePegs.get(pegId);
      toPeg.add(0, selectedDisc.get());
   }

   private void addDiskToTarget(Rectangle disc, String pegId) {
      ObservableList<Node> toPeg = this.gamePegs.get(pegId);
      toPeg.add(0, disc);
   }


   private Rectangle getTopMostDisc(String pegId) {
      if (gamePegs.get(pegId).size() > 0) return (Rectangle) gamePegs.get(pegId).get(0);
      return null;
   }

   public GameController getGameController() {
      return gameController;
   }

   public void buildLeaderBoard() {
      List<Pair<String, Long>> leaderBoardRecords = gameController.getLeaderBoard();
      if (leaderBoardRecords != null && leaderBoardRecords.size() > 0) {
         for (Pair<String, Long> record : leaderBoardRecords) {
            //construct VBox
            VBox lbBox = new VBox();
            lbBox.setPrefWidth(150);
            lbBox.setAlignment(Pos.TOP_CENTER);
            Label userIdLabel = new Label();
            userIdLabel.setText(record.getKey());
            Label durationLabel = new Label();
            durationLabel.setText(UIHelper.longToHhMmSs(record.getValue()));
            lbBox.getChildren().addAll(userIdLabel, durationLabel);

            leaderBoardHBox.getChildren().add(lbBox);

         }
      }
   }
}
