DROP TABLE IF EXISTS Game_Status ;
DROP TABLE IF EXISTS Game;
DROP TABLE IF EXISTS Tower_User;

CREATE TABLE Tower_User (
  UserId VARCHAR(50) PRIMARY KEY,
  Pword VARCHAR(50)
);

CREATE TABLE Game (
  GameId BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  UserId VARCHAR(50) NOT NULL,
  Pegs TINYINT NOT NULL,
  Discs TINYINT NOT NULL,
  Duration BIGINT,
  Finish BOOLEAN
);

CREATE TABLE Game_Status (
  StatusId BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  GameId BIGINT NOT NULL,
  DiscId VARCHAR(10) NOT NULL,
  DiscSize TINYINT NOT NULL,
  PegId VARCHAR(10) NOT NULL
);