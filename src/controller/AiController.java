package controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import model.Peg;
import ui.UiController;

import java.util.Map;

public class AiController extends Task<Void> {
   private GameController gameController;
   private UiController uiController;

   public AiController(UiController uiController) {
      this.uiController = uiController;
      gameController = this.uiController.getGameController();
   }

   private void solveTowers(int totalDisc, String sourcePegId, String desPegId, String tempPegId) throws InterruptedException {
      //get deque from source
      if (totalDisc == 1) {
         //base case
         gameController.moveDisc(sourcePegId, desPegId);
         Platform.runLater(new Runnable() {
            @Override
            public void run() {
               uiController.uiMoveDisc(sourcePegId, desPegId);
            }
         });
         Thread.sleep(500);
         return;
      }

      //move n-1 disk from source to temp
      solveTowers(totalDisc - 1, sourcePegId, tempPegId, desPegId);

      //move last disk from source to des
      gameController.moveDisc(sourcePegId, desPegId);
      Platform.runLater(new Runnable() {
         @Override
         public void run() {
            uiController.uiMoveDisc(sourcePegId, desPegId);
         }
      });
      Thread.sleep(500);
//      this.uiController.uiMoveDisc(sourcePegId, desPegId);
//      Thread.sleep(2000);

      //move n-1 disk from temp to des
      solveTowers(totalDisc - 1, tempPegId, desPegId, sourcePegId);
   }

   @Override
   protected Void call() throws Exception {
      int numberOfDiscs = this.gameController.getNumberOfDiscs();
      Map<String, Peg> pegs = gameController.getPegs();
      //identify source, temp and des
      int count = 0;
      String sourcePegId ="";
      String tempPegId = "";
      String desPegId = "";
      for (Map.Entry<String, Peg> entry : pegs.entrySet()) {
         if (count == gameController.getBeginPeg()) {
            sourcePegId = entry.getKey();
         } else if (count == gameController.getEndPeg()) {
            desPegId = entry.getKey();
         } else {
            tempPegId = entry.getKey();
         }
         count++;
      }
      solveTowers(numberOfDiscs, sourcePegId, desPegId, tempPegId);
      return null;
   }
}
