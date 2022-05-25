package model;

public class GameStatus {
   private Long statusId;
   private Long gameId;
   private String discId;
   private int discSize;
   private String pegId;

   public GameStatus(Long statusId, Long gameId, String discId, int discSize, String pegId) {
      this.statusId = statusId;
      this.gameId = gameId;
      this.discId = discId;
      this.discSize = discSize;
      this.pegId = pegId;
   }

   public Long getStatusId() {
      return statusId;
   }

   public void setStatusId(Long statusId) {
      this.statusId = statusId;
   }

   public Long getGameId() {
      return gameId;
   }

   public void setGameId(Long gameId) {
      this.gameId = gameId;
   }

   public String getDiscId() {
      return discId;
   }

   public void setDiscId(String discId) {
      this.discId = discId;
   }

   public int getDiscSize() {
      return discSize;
   }

   public void setDiscSize(int discSize) {
      this.discSize = discSize;
   }

   public String getPegId() {
      return pegId;
   }

   public void setPegId(String pegId) {
      this.pegId = pegId;
   }
}
