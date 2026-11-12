CREATE DATABASE `epg` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

use epg;

CREATE TABLE `Channel` (
  `channel_id` varchar(16) NOT NULL COMMENT '채널 ID',
  `channel_name` varchar(256) NOT NULL COMMENT '채널명',
  `channel_group` varchar(256) DEFAULT NULL COMMENT '채널 그룹',
  PRIMARY KEY (`channel_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `favorite_channels` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(100) NOT NULL,
  `channel_id` varchar(50) NOT NULL,
  `added_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_channel` (`user_id`,`channel_id`),
  KEY `channel_id` (`channel_id`),
  CONSTRAINT `favorite_channels_ibfk_1` FOREIGN KEY (`channel_id`) REFERENCES `Channel` (`channel_id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `LineUp` (
  `product_id` varchar(16) NOT NULL COMMENT '상품 ID',
  `channel_number` int NOT NULL COMMENT '채널 번호',
  `channel_id` varchar(16) NOT NULL COMMENT '채널 ID',
  PRIMARY KEY (`product_id`,`channel_number`),
  KEY `channel_id` (`channel_id`),
  CONSTRAINT `LineUp_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `Product` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `LineUp_ibfk_2` FOREIGN KEY (`channel_id`) REFERENCES `Channel` (`channel_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chan_number_over_0` CHECK ((`channel_number` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `Product` (
  `product_id` varchar(16) NOT NULL COMMENT '상품 ID',
  `product_name` varchar(256) NOT NULL COMMENT '상품명',
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `Program` (
  `program_id` char(10) NOT NULL COMMENT '프로그램 ID',
  `title` varchar(1024) NOT NULL COMMENT '프로그램 제목',
  `genre` varchar(256) DEFAULT NULL COMMENT '장르. 여러 개일 경우 |로 구분',
  `rating` varchar(64) DEFAULT NULL COMMENT '시청 등급',
  `summary` mediumtext COMMENT '프로그램 요약 설명',
  `score` double DEFAULT NULL COMMENT '인기도 점수. [0, 1]',
  PRIMARY KEY (`program_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `Schedule` (
  `schedule_id` varchar(32) NOT NULL COMMENT '스케줄 ID',
  `channel_id` varchar(16) NOT NULL COMMENT '방송 채널 ID',
  `program_id` char(10) NOT NULL COMMENT '방송할 프로그램 ID',
  `sub_title` mediumtext,
  `start_time` timestamp NOT NULL COMMENT '스케줄 시작 시간',
  `end_time` timestamp NOT NULL COMMENT '스케줄 종료 시간',
  `seq_num` int DEFAULT NULL COMMENT '스케줄 선후 관계를 구분할 순번',
  `summary` mediumtext COMMENT '프로그램 설명',
  PRIMARY KEY (`schedule_id`),
  KEY `program_id` (`program_id`),
  KEY `schedule_fk_01` (`channel_id`),
  CONSTRAINT `schedule_fk_01` FOREIGN KEY (`channel_id`) REFERENCES `Channel` (`channel_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Schedule_ibfk_1` FOREIGN KEY (`channel_id`) REFERENCES `Channel` (`channel_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `Schedule_ibfk_2` FOREIGN KEY (`program_id`) REFERENCES `Program` (`program_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
