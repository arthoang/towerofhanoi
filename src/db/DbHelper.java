package db;

import javafx.util.Pair;
import model.Disc;
import model.Game;
import model.GameStatus;
import model.Peg;

import javax.xml.transform.Result;
import java.sql.*;
import java.util.*;

public class DbHelper {
   public static String GET_USER_BY_ID = "GET_USER_BY_ID";
   public static String INSERT_USER = "INSERT_USER";
   public static String AUTHENTICATE_USER = "AUTHENTICATE_USER";
   public static String GET_GAME_BY_USERID = "GET_GAME_BY_USERID";
   public static String INSERT_GAME = "INSERT_GAME";
   public static String GET_GAME_STATUS_BY_GAMEID = "GET_GAME_STATUS_BY_GAMEID";
   public static String INSERT_GAME_STATUS = "INSERT_GAME_STATUS";
   public static String GET_TOP_RECORDS = "GET_TOP_RECORDS";

   private String pathToDb = "~/tmp/hanoitower";
   private String username = "sa";
   private String pw = "";
   private String h2Connection =  "jdbc:h2:";
   private Map<String, String> queries;

   Connection conn;

   public DbHelper() {
      try {
         conn = DriverManager.getConnection(h2Connection + pathToDb, username, pw);
         populateQueries();
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

   private void populateQueries() throws SQLException {

      queries = new HashMap<>();

      String query = "SELECT UserId FROM Tower_User WHERE UserId= ?";
      queries.put(GET_USER_BY_ID, query);

      query = "INSERT INTO Tower_User (UserId, Pword) VALUES (?, ?)";
      queries.put(INSERT_USER, query);

      query = "SELECT UserId FROM Tower_User WHERE UserId = ? AND Pword = ?";
      queries.put(AUTHENTICATE_USER, query);

      query = "SELECT GameId, Pegs, Discs, Duration, Finish FROM Game WHERE UserId = ?";
      queries.put(GET_GAME_BY_USERID, query);

      query = "INSERT INTO Game (UserId, Pegs, Discs, Duration, Finish) VALUES (?, ?, ?, ?, ?)";
      queries.put(INSERT_GAME, query);

      query = "SELECT StatusId, DiscId, DiscSize, PegId FROM Game_Status WHERE GameId = ?";
      queries.put(GET_GAME_STATUS_BY_GAMEID, query);

      query = "INSERT INTO Game_Status (GameId, DiscId, DiscSize, PegId) VALUES (?, ?, ?, ?)";
      queries.put(INSERT_GAME_STATUS, query);

      query = "SELECT UserId, Duration FROM (SELECT UserId, Duration FROM Game WHERE Finish = True AND Pegs = ? AND Discs = ? ORDER BY Duration) LIMIT 3 ";
      queries.put(GET_TOP_RECORDS, query);

   }

   public boolean registerUser(String userId, String password) {

      try(PreparedStatement stmt = conn.prepareStatement(queries.get(GET_USER_BY_ID))) {
         stmt.setString(1, userId);
         try (ResultSet resultSet = stmt.executeQuery()) {
            if (resultSet!=null) {
               resultSet.beforeFirst();
               resultSet.last();
               int size = resultSet.getRow();
               if (size > 0) {
                  //user exists
                  throw new IllegalArgumentException("UserId exists. Try another userId");
               }
            }

         }
      } catch (SQLException e) {
         e.printStackTrace();
         return false;
      }


      //register user
      try (PreparedStatement stmt = conn.prepareStatement(queries.get(INSERT_USER))){
         stmt.setString(1, userId);
         stmt.setString(2, password);
         stmt.execute();
         return true;
      } catch (SQLException e) {
         e.printStackTrace();
         return false;
      }

   }

   public boolean authenticateUser(String userId, String password) {

      try (PreparedStatement stmt = conn.prepareStatement(queries.get(AUTHENTICATE_USER))) {
         stmt.setString(1, userId);
         stmt.setString(2, password);
         try (ResultSet resultSet = stmt.executeQuery()) {
            if (resultSet!=null) {
               resultSet.beforeFirst();
               resultSet.last();
               int size = resultSet.getRow();
               if (size > 0) {
                  //valid user
                  return true;
               } else {
                  return false;
               }
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
         return false;
      }

      return false;
   }

   public void saveGame(String userId, Map<String, Peg> pegs, int numberOfDiscs, int numberOfPegs, long duration, boolean isFinish) {
      //save game, get GameId
      //UserId, Pegs, Discs, Duration, Finish
      String[] returnAttributes = {"GameId"};
      Long gameId = null;
      try(PreparedStatement gameStmt = conn.prepareStatement(queries.get(INSERT_GAME), returnAttributes)) {

         gameStmt.setString(1, userId);
         gameStmt.setString(2, numberOfPegs+"");
         gameStmt.setString(3, numberOfDiscs+"");
         gameStmt.setString(4, duration+"");
         gameStmt.setString(5, isFinish+"");
         //insert
         int rows = gameStmt.executeUpdate();
         if (rows == 0) {
            throw new SQLException("Failed to insert game");
         }
         try (ResultSet rs = gameStmt.getGeneratedKeys()) {
            if (rs.next()) {
               gameId = (Long) rs.getObject("GameId");
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
      }

      if (gameId != null) {
         //insert game_status record
         for (Map.Entry<String, Peg> entry : pegs.entrySet()) {
            String pegId = entry.getKey();
            Peg p = entry.getValue();
            Iterator<Disc> discIt = p.getDiscIterator();
            while(discIt.hasNext()) {
               Disc d = discIt.next();
               //insert game_status
               try (PreparedStatement statusStmt = conn.prepareStatement(queries.get(INSERT_GAME_STATUS))) {
                  //(GameId, DiscId, DiscSize, PegId)
                  statusStmt.setString(1, gameId.toString());
                  statusStmt.setString(2, d.getDiscId());
                  statusStmt.setString(3, d.getDiscSize()+"");
                  statusStmt.setString(4, pegId);
                  statusStmt.execute();
               } catch (SQLException e) {
                  e.printStackTrace();
               }
            }
         }
      }
      System.out.println("Game saved");
   }

   public Game loadGame(String userId) {
      Game game = null;
      try (PreparedStatement gameStmt = conn.prepareStatement(queries.get(GET_GAME_BY_USERID))) {
         gameStmt.setString(1, userId);
         try (ResultSet rs = gameStmt.executeQuery()) {
            //GameId, Pegs, Discs, Duration, Finish
            if (rs.next()) {
               game = new Game(
                  rs.getLong("GameId"),
                  userId,
                  rs.getInt("Pegs"),
                  rs.getInt("Discs"),
                  rs.getLong("Duration"),
                  rs.getBoolean("Finish")

               );
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
         return null;
      }

      if (game != null) {
         //get game Statuses
         List<GameStatus> gameStatuses = new ArrayList<> ();
         try (PreparedStatement statusStmt = conn.prepareStatement(queries.get(GET_GAME_STATUS_BY_GAMEID))) {
            statusStmt.setString(1, game.getGameId().toString());
            try (ResultSet rs = statusStmt.executeQuery()) {
               while (rs.next()) {
                  //StatusId, DiscId, DiscSize, PegId
                  GameStatus status = new GameStatus(
                     rs.getLong("StatusId"),
                     game.getGameId(),
                     rs.getString("DiscId"),
                     rs.getInt("DiscSize"),
                     rs.getString("PegId")
                  );
                  gameStatuses.add(status);
               }
               game.setGameStatuses(gameStatuses);
            }
         } catch (SQLException e) {
            e.printStackTrace();
            return null;
         }
      }
      return game;
   }

   public List<Pair<String, Long>> getLeaderBoard(int discs, int pegs) {
      List<Pair<String, Long>> result;
      try (PreparedStatement stmt = conn.prepareStatement(queries.get(GET_TOP_RECORDS))) {
         stmt.setString(1, discs+"");
         stmt.setString(2, pegs+"");
         result = new ArrayList<>();
         try(ResultSet rs = stmt.executeQuery()) {
            while(rs.next()) {
               Pair<String, Long> record = new Pair<>(
                 rs.getString("UserId"),
                 rs.getLong("Duration")
               );
               result.add(record);
            }
         }
      } catch (SQLException e) {
         e.printStackTrace();
         return null;
      }
      return result;
   }

   public void close() {
      try {
         conn.close();
      } catch (SQLException sqlException) {
         sqlException.printStackTrace();
      }
   }



}
