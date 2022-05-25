package ui;

import controller.AiController;
import controller.GameController;
import db.DbHelper;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;
import model.Peg;

import java.util.Map;
import java.util.Optional;


public class TowerOfHanoiController {
   //constants
   private final int max_disc = 10;
   private final int max_peg = 3;
   private int min_disc = 3;
   private final int min_peg = 3;


   private int discs;
   private int pegs;


   private UiController uiController;

   private GameController gameController;

   private DbHelper dbHelper;


   Pair<String, String> credentials;
   private String userId = "";
   private String pw;
   private boolean isLoginClicked;



   @FXML
   private HBox topBox;

   @FXML
   private HBox gameBox;

   @FXML
   private HBox bottomBox;

   @FXML
   private Label timerLabel;

   @FXML
   private Label userIdLabel;

   @FXML
   private BorderPane mainPane;

   @FXML
   private Button startGameButton;

   @FXML
   private Button giveUpButton;

   @FXML
   private Button lessDiscButton;

   @FXML
   private Button lessPegButton;

   @FXML
   private Button loadButton;

   @FXML
   private Button moreDiscButton;

   @FXML
   private Button morePegButton;

   @FXML
   private Label numberOfDisc;

   @FXML
   private Label numberOfPeg;

   @FXML
   private Button saveButton;

   @FXML
   private HBox leaderBoardHBox;

   Thread ai;

   public void initialize() {
      this.pegs = min_peg;
      this.discs = min_disc;
      this.uiController = new UiController();
      this.dbHelper = new DbHelper();
      //show login dialog, and do authentication or registering before letting user in
      processLoginDialog();
   }

   public void processLoginDialog() {
      //show login dialog
      String resultMessage = null;
      showLoginDialog(resultMessage);
      resultMessage = processLoginRegister();
      while (resultMessage != null) {
         showLoginDialog(resultMessage);
         resultMessage = processLoginRegister();
      }
   }

   private void showLoginDialog(String message) {
      //show login dialog
      Dialog<Pair<Pair<String, String>, Boolean>> loginDialog = UIHelper.getLoginDialog(message);
      Optional<Pair<Pair<String, String>, Boolean>> loginInfo;

      loginInfo = loginDialog.showAndWait();
      loginInfo.ifPresent(pair -> {
         credentials = pair.getKey();
         isLoginClicked = pair.getValue();
         userId = credentials.getKey();
         pw = credentials.getValue();
      });
      System.out.println(loginInfo);
   }

   private String processLoginRegister() {
      if (userId != null && userId.length() > 0 && pw != null && pw.length() > 0) {
         if (isLoginClicked) {
            //login
            if (dbHelper.authenticateUser(userId, pw)) {
               return null;
            } else {
               return "Invalid login";
            }
         } else {
            //register
            try {
               if (dbHelper.registerUser(userId, pw)) {
                  return null;
               } else {
                  return "Failed to register user";
               }
            } catch (IllegalArgumentException e) {
               return e.getMessage();
            }
         }
      } else {
         return "UserId/Password cannot be null";
      }
   }

   private void updateDiscLabel() {
      this.numberOfDisc.setText(Integer.toString(this.discs));
   }

   private void updatePegLabel() {
      this.numberOfPeg.setText(Integer.toString(this.pegs));
   }

   @FXML
   void adjustDisc(ActionEvent event) {
      Button triggeredButton = (Button) event.getSource();
      String btText = triggeredButton.getText();
      if (btText.equals("+")) {
         //increase disc
         if (this.discs < max_disc) {
            this.discs++;
         }
      } else {
         //decrease disc
         if (this.discs > min_disc) {
            this.discs--;
         }
      }
      //update ui
      updateDiscLabel();
   }

   @FXML
   void adjustPeg(ActionEvent event) {
      Button triggeredButton = (Button) event.getSource();
      String btText = triggeredButton.getText();

      if (btText.equals("+")) {
         //increase disc
         if (this.pegs < max_peg) {
            this.pegs++;
         }
      } else {
         //decrease disc
         if (this.pegs > min_peg) {
            this.pegs--;
         }
      }
      //update ui
      updatePegLabel();
      //update disc if it is less than min
      this.min_disc = this.pegs;
      if (this.discs < this.min_disc) {
         this.discs = this.min_disc;
         updateDiscLabel();
      }

   }


   void constructPegElements(GameController gameController) {

      for (Map.Entry<String, Peg> entry : gameController.getPegs().entrySet()) {
         Peg modelPeg = entry.getValue();
         int pegNo = Integer.parseInt(modelPeg.getPegId().split("_")[1]);

         //create stack pane for each peg area
         StackPane pegArea = new StackPane();
         pegArea.setPrefWidth(uiController.PEG_BOX_WIDTH);
         pegArea.setPrefHeight(uiController.PEG_BOX_HEIGHT);
         pegArea.setId(modelPeg.getPegId());
         pegArea.setAlignment(Pos.BOTTOM_CENTER);

         //create vertical column for the peg
         //the last column is the goal, use a different color
         Rectangle pegColumn = UIHelper.getRectangleShape(10, uiController.PEG_BOX_HEIGHT, (pegNo == gameController.getNumberOfPegs() - 1) ? Color.DARKGREEN : Color.GREY, 5, 5);
         pegArea.getChildren().add(pegColumn);

         //VBox container. Top is disc container. Bottom is base
         VBox discBoxContainer = new VBox();
         discBoxContainer.setPrefWidth(uiController.PEG_BOX_WIDTH);
         discBoxContainer.setAlignment(Pos.BOTTOM_CENTER);

         //inner VBox container for discs and base
         VBox discBox = new VBox();
         discBox.setPrefWidth(uiController.PEG_BOX_WIDTH);
         discBox.setAlignment(Pos.BOTTOM_CENTER);
         discBox.setId(uiController.VBOX_ID_PREFIX + pegNo);
         //bind discBox to ObservableList
         Bindings.bindContentBidirectional(discBox.getChildren(), uiController.getPegs().get(modelPeg.getPegId()));

         //add discBox on top
         discBoxContainer.getChildren().add(discBox);

         //finally, at the base at bottom
         //create base
         Rectangle base = UIHelper.getRectangleShape(uiController.PEG_BOX_WIDTH, uiController.BASE_HEIGHT, Color.GREY, 5, 5);
         discBoxContainer.getChildren().add(base);

         //add discBoxContainer to pegArea
         pegArea.getChildren().add(discBoxContainer);

         //add area to gameBox
         gameBox.getChildren().add(pegArea);
      }
   }

   void renderGameBox(GameController gameController) {

      //clear game box
      int childrenSize = gameBox.getChildren().size();
      gameBox.getChildren().remove(0, childrenSize);

      //construct pegs
      constructPegElements(gameController);
      //add listeners
      uiController.registerPegListeners();

   }

   @FXML
   void giveUp(ActionEvent event) throws InterruptedException {
      //stop the timer thread
      if (gameController != null) {
         gameController.setGameInProgress(false);

         //give time for timer thread to read updated value
         Thread.sleep(1000);
      }

      //reset the game
      gameController = new GameController(this.pegs, this.discs, this.userId);
      uiController.initialize(gameController, gameBox, leaderBoardHBox);

      renderGameBox(gameController);

      //let AI solve the problem
      ai = new Thread(new AiController(uiController));
      ai.setDaemon(true);
      ai.start();
   }

   @FXML
   void loadGame(ActionEvent event) {
      gameController = new GameController(this.pegs, this.discs, this.userId);
      gameController.loadGame(userId);
      gameController.setIsGameInProgress(true);

      uiController.initialize(gameController, gameBox, leaderBoardHBox);
      renderGameBox(gameController);
      uiController.buildLeaderBoard();
      //bind button disable status
      bindButtonDisableProperties();
      bindTimerLabel();
      gameController.startTimer();

   }

   @FXML
   void saveGame(ActionEvent event) {
      gameController.saveGame();
   }

   @FXML
   void startGame(ActionEvent event) {
      gameController = new GameController(this.pegs, this.discs, this.userId);
      uiController.initialize(gameController, gameBox, leaderBoardHBox);
      //set boolean property that binds with UI button
      gameController.setIsGameInProgress(true);

      renderGameBox(gameController);
      uiController.buildLeaderBoard();
      //bind button disable status
      bindButtonDisableProperties();
      bindTimerLabel();
      gameController.startTimer();

   }

   void bindButtonDisableProperties() {
      startGameButton.disableProperty().bind(Bindings.createBooleanBinding(() -> gameController.getIsGameInProgressValue()));
      startGameButton.disableProperty().bind(Bindings.createBooleanBinding(() -> gameController.getIsGameInProgressValue()));
      moreDiscButton.disableProperty().bind(Bindings.createBooleanBinding(() -> gameController.getIsGameInProgressValue()));
      morePegButton.disableProperty().bind(Bindings.createBooleanBinding(() -> gameController.getIsGameInProgressValue()));
      lessDiscButton.disableProperty().bind(Bindings.createBooleanBinding(() -> gameController.getIsGameInProgressValue()));
      lessPegButton.disableProperty().bind(Bindings.createBooleanBinding(() -> gameController.getIsGameInProgressValue()));
   }

   void bindTextLabelValue() {
      userIdLabel.textProperty().bind(Bindings.createStringBinding(() -> this.userId));
   }

   void bindTimerLabel() {
      timerLabel.textProperty().bind(Bindings.convert(gameController.getElapsedTimeStrProp()));
   }


}
