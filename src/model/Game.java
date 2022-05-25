package model;

import java.util.List;

public class Game {
   private Long gameId;
   private Long duration;
   private String userId;
   private int pegs;
   private int discs;
   private Boolean finish;
   private List<GameStatus> gameStatuses;

   public Game(Long gameId, String userId, int pegs, int discs, Long duration, Boolean finish) {
      this.gameId = gameId;
      this.userId = userId;
      this.pegs = pegs;
      this.discs = discs;
      this.duration = duration;
      this.finish = finish;

   }

   public Long getGameId() {
      return gameId;
   }

   public void setGameId(Long gameId) {
      this.gameId = gameId;
   }

   public Long getDuration() {
      return duration;
   }

   public void setDuration(Long duration) {
      this.duration = duration;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public int getPegs() {
      return pegs;
   }

   public void setPegs(int pegs) {
      this.pegs = pegs;
   }

   public int getDiscs() {
      return discs;
   }

   public void setDiscs(int discs) {
      this.discs = discs;
   }

   public List<GameStatus> getGameStatuses() {
      return gameStatuses;
   }

   public Boolean getFinish() {
      return finish;
   }

   public void setFinish(Boolean finish) {
      this.finish = finish;
   }

   public void setGameStatuses(List<GameStatus> gameStatuses) {
      this.gameStatuses = gameStatuses;
   }
}
