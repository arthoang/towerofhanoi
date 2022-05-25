package ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Pair;

public class UIHelper {
   public static Rectangle getRectangleShape(double width, double height, Color color, double arcWidth, double arcHeight) {
      Rectangle rec = new Rectangle();

      rec.setWidth(width);
      rec.setHeight(height);
      rec.setFill(color);
      rec.setArcWidth(arcWidth);
      rec.setArcHeight(arcHeight);
      return rec;
   }

   public static String longToHhMmSs(long time) {
      long durationInSeconds = time / 1000;
      int hours = (int) durationInSeconds / 3600;
      long remainder = durationInSeconds % 3600;
      int minutes = (int) remainder / 60;
      int seconds = (int) remainder % 60;
      return hours + ":" + minutes + ":" + seconds;
   }

   public static Dialog<Pair<Pair<String, String>, Boolean>> getLoginDialog(String message) {
      //Dialog which return Pair of userId/Password and whether it is login or not (create new account)
      Dialog<Pair<Pair<String, String>, Boolean>> loginDialog = new Dialog<>();
      loginDialog.setTitle("Login or Register to begin");
      //set buttons
      ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
      ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OTHER);
      loginDialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType);

      //layout the dialog
      GridPane gridPane = new GridPane();
      gridPane.setHgap(20);
      gridPane.setVgap(10);
      gridPane.setPadding(new Insets(10, 10, 10, 10));
      //user ID
      Label userIdLabel = new Label();
      userIdLabel.setText("User ID");
      TextField userId = new TextField();


      //password
      Label pwLabel = new Label();
      pwLabel.setText("Password");
      PasswordField pw = new PasswordField();



      gridPane.add(userIdLabel, 0, 0);
      gridPane.add(userId, 1, 0);
      gridPane.add(pwLabel, 0, 1);
      gridPane.add(pw, 1, 1);

      //message

      if (message != null && message.length() > 0) {
         Label msgLabel = new Label();
         msgLabel.setStyle("-fx-text-fill: red;");
         msgLabel.setText(message);
         gridPane.add(msgLabel, 0, 2, 2, 1);
      }

      loginDialog.getDialogPane().setContent(gridPane);

      //extract pair of userId and password from dialog
      loginDialog.setResultConverter(dialogButton -> {
         if (dialogButton == loginButtonType) {
            return new Pair<Pair<String, String>, Boolean>(new Pair<String, String>(userId.getText(), pw.getText()), true);
         } else {
            return new Pair<Pair<String, String>, Boolean>(new Pair<String, String>(userId.getText(), pw.getText()), false);
         }

      });

      return loginDialog;
   }


}
