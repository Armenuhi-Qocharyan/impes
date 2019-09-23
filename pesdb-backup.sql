-- MySQL dump 10.13  Distrib 5.7.27, for Linux (x86_64)
--
-- Host: localhost    Database: pes
-- ------------------------------------------------------
-- Server version	5.7.27-0ubuntu0.16.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `active_games`
--

DROP TABLE IF EXISTS `active_games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `active_games` (
  `id` int(11) NOT NULL,
  `championship` varchar(100) NOT NULL,
  `championshipState` varchar(100) NOT NULL,
  `startTimestamp` mediumtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `active_games_players_activities`
--

DROP TABLE IF EXISTS `active_games_players_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `active_games_players_activities` (
  `id` int(11) NOT NULL,
  `gameId` int(11) NOT NULL,
  `playerId` int(11) NOT NULL,
  `activityType` varchar(100) NOT NULL,
  `angle` int(11) DEFAULT NULL,
  `x` int(11) DEFAULT NULL,
  `y` int(11) DEFAULT NULL,
  `firstAngle` int(11) DEFAULT NULL,
  `secondAngle` int(11) DEFAULT NULL,
  `power` int(11) DEFAULT NULL,
  `timestamp` mediumtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `active_games_players_data`
--

DROP TABLE IF EXISTS `active_games_players_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `active_games_players_data` (
  `id` int(11) NOT NULL,
  `gameId` int(11) NOT NULL,
  `playerId` int(11) NOT NULL,
  `active` tinyint(1) NOT NULL,
  `summary` json NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `active_games_reserve_players`
--

DROP TABLE IF EXISTS `active_games_reserve_players`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `active_games_reserve_players` (
  `id` int(11) NOT NULL,
  `gameId` int(11) NOT NULL,
  `playerId` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `active_games_teams_data`
--

DROP TABLE IF EXISTS `active_games_teams_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `active_games_teams_data` (
  `id` int(11) NOT NULL,
  `gameId` int(11) NOT NULL,
  `teamId` int(11) NOT NULL,
  `isReady` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `comming_games`
--

DROP TABLE IF EXISTS `comming_games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `comming_games` (
  `id` bigint(20) DEFAULT NULL,
  `team_one` int(11) DEFAULT NULL,
  `team_two` int(11) DEFAULT NULL,
  `championship` text,
  `championship_state` text,
  `date` text
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `done_games`
--

DROP TABLE IF EXISTS `done_games`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `done_games` (
  `id` int(11) NOT NULL,
  `team_one` int(11) NOT NULL,
  `team_two` int(11) NOT NULL,
  `firstTeamGoals` int(11) NOT NULL,
  `secondTeamGoals` int(11) NOT NULL,
  `championship` varchar(100) NOT NULL,
  `championship_state` varchar(100) NOT NULL,
  `date` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lobbies`
--

DROP TABLE IF EXISTS `lobbies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lobbies` (
  `id` int(11) NOT NULL,
  `owner` int(11) NOT NULL,
  `gameId` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `lobbies_teams`
--

DROP TABLE IF EXISTS `lobbies_teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `lobbies_teams` (
  `id` int(11) NOT NULL,
  `lobbyId` int(11) NOT NULL,
  `teamId` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `players`
--

DROP TABLE IF EXISTS `players`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `players` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `team` int(11) DEFAULT NULL,
  `position` enum('goalkeeper','full_back','wing_back','centre_back','sweeper','central_midfield','winger','striker','behind_the_striker','substitute') DEFAULT NULL,
  `cost` int(11) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `height` int(11) DEFAULT NULL,
  `weight` int(11) DEFAULT NULL,
  `skills` int(11) DEFAULT NULL,
  `game_intelligence` int(11) DEFAULT NULL,
  `team_player` int(11) DEFAULT NULL,
  `physique` int(11) DEFAULT NULL,
  `isDefault` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `team` (`team`),
  CONSTRAINT `players_ibfk_1` FOREIGN KEY (`team`) REFERENCES `teams` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `players_positions`
--

DROP TABLE IF EXISTS `players_positions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `players_positions` (
  `id` int(11) NOT NULL,
  `position` varchar(100) NOT NULL,
  `x` int(11) NOT NULL,
  `y` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `players_statistics`
--

DROP TABLE IF EXISTS `players_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `players_statistics` (
  `id` int(11) NOT NULL,
  `playerId` int(11) NOT NULL,
  `teamId` int(11) NOT NULL,
  `doneGameId` int(11) NOT NULL,
  `goals` int(11) NOT NULL,
  `donePasses` int(11) NOT NULL,
  `smartPasses` int(11) NOT NULL,
  `passes` int(11) NOT NULL,
  `doneShots` int(11) NOT NULL,
  `shots` int(11) NOT NULL,
  `doneTackles` int(11) NOT NULL,
  `tackles` int(11) NOT NULL,
  `dribblingCount` int(11) NOT NULL,
  `hooks` int(11) NOT NULL,
  `ballLosses` int(11) NOT NULL,
  `aerialsWon` int(11) NOT NULL,
  `assists` int(11) NOT NULL,
  `falls` int(11) NOT NULL,
  `mileage` int(11) NOT NULL,
  `yellowCards` int(11) NOT NULL,
  `redCard` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `players_transactions`
--

DROP TABLE IF EXISTS `players_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `players_transactions` (
  `id` int(11) NOT NULL,
  `playerId` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `players_transactions_history`
--

DROP TABLE IF EXISTS `players_transactions_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `players_transactions_history` (
  `id` int(11) NOT NULL,
  `playerId` int(11) NOT NULL,
  `sTeamId` int(11) NOT NULL,
  `bTeamId` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `date` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sessions`
--

DROP TABLE IF EXISTS `sessions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sessions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userId` int(11) NOT NULL,
  `token` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teams`
--

DROP TABLE IF EXISTS `teams`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teams` (
  `id` int(11) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `budget` int(11) DEFAULT NULL,
  `championship` enum('la_liga','seria_a','premier_liga') DEFAULT NULL,
  `champions_league` tinyint(1) DEFAULT NULL,
  `is_used` tinyint(1) DEFAULT NULL,
  `standard_staff` int(11) DEFAULT NULL,
  `owner` int(11) DEFAULT NULL,
  `isDefault` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `standard_staff` (`standard_staff`),
  KEY `owner` (`owner`),
  CONSTRAINT `teams_ibfk_1` FOREIGN KEY (`standard_staff`) REFERENCES `players` (`id`),
  CONSTRAINT `teams_ibfk_2` FOREIGN KEY (`owner`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teams_statistics`
--

DROP TABLE IF EXISTS `teams_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teams_statistics` (
  `id` int(11) NOT NULL,
  `teamId` int(11) NOT NULL,
  `doneGameId` int(11) NOT NULL,
  `goals` int(11) NOT NULL,
  `possession` int(11) NOT NULL,
  `yellowCards` int(11) NOT NULL,
  `redCards` int(11) NOT NULL,
  `falls` int(11) NOT NULL,
  `shots` int(11) NOT NULL,
  `aerialsWon` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teams_transactions`
--

DROP TABLE IF EXISTS `teams_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teams_transactions` (
  `id` int(11) NOT NULL,
  `teamId` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teams_transactions_history`
--

DROP TABLE IF EXISTS `teams_transactions_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teams_transactions_history` (
  `id` int(11) NOT NULL,
  `teamId` int(11) NOT NULL,
  `sUserId` int(11) NOT NULL,
  `bUserId` int(11) NOT NULL,
  `price` int(11) NOT NULL,
  `date` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `role` varchar(100) NOT NULL,
  `budget` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-09-23 18:23:57
