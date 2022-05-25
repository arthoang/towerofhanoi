package model;

import javafx.scene.paint.Color;

public class Disc {
   //predefine 10 colors of the discs
   private final Color[] colors = new Color[] { null, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.DARKGREEN, Color.OLIVE, Color.CYAN, Color.AZURE, Color.BLUE, Color.VIOLET};
   private final String DISC_ID_PREFIX = "DISC_";
   private int discSize;
   private String discId;

   public Disc(int discSize) {
      this.discSize = discSize;
      this.discId = this.DISC_ID_PREFIX + discSize;
   }

   public int getDiscSize() {
      return discSize;
   }

   public Color getColor() {
      return colors[discSize];
   }

   public String getDiscId() {
      return this.discId;
   }

   @Override
   public String toString() {
      return ""+ this.discSize;
   }

}
