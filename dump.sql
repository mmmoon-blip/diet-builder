-- MySQL dump 10.13  Distrib 8.0.45, for Linux (aarch64)
--
-- Host: localhost    Database: diet_butler
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `body_measurements`
--

DROP TABLE IF EXISTS `body_measurements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `body_measurements` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID，自增主键',
  `arm` double DEFAULT NULL,
  `chest` double DEFAULT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '记录创建时间',
  `hip` double DEFAULT NULL,
  `record_date` date NOT NULL COMMENT '记录日期',
  `thigh` double DEFAULT NULL,
  `user_id` bigint NOT NULL COMMENT '用户ID，关联users.id',
  `waist` double DEFAULT NULL,
  `calf` double DEFAULT NULL,
  `forearm` double DEFAULT NULL,
  `upper_arm` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身体维度记录表，记录腰围/臀围/胸围/上臂围/大腿围';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `body_measurements`
--

LOCK TABLES `body_measurements` WRITE;
/*!40000 ALTER TABLE `body_measurements` DISABLE KEYS */;
INSERT INTO `body_measurements` VALUES (4,NULL,NULL,'2026-05-09 10:38:41.432674',NULL,'2025-06-17',54,1,80,40,21,27),(6,NULL,NULL,'2026-05-09 10:40:05.650389',NULL,'2026-04-16',56,1,76,40,23,31),(7,NULL,NULL,'2026-05-09 10:40:55.893555',NULL,'2025-07-17',52,1,74,39,22,27);
/*!40000 ALTER TABLE `body_measurements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID，自增主键',
  `content` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容，最大2000字符',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '消息创建时间',
  `intent` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '意图分类: greeting/profile_query/weight_curve/profile_update/weight_record/chat',
  `role` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色: user=用户, assistant=助手',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联users.id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=210 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话消息记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diet_record`
--

DROP TABLE IF EXISTS `diet_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diet_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `calories` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `foods` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `meal_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `end_time` time(6) DEFAULT NULL,
  `start_time` time(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diet_record`
--

LOCK TABLES `diet_record` WRITE;
/*!40000 ALTER TABLE `diet_record` DISABLE KEYS */;
INSERT INTO `diet_record` VALUES (1,270,'2026-05-09 15:56:10.777507','无糖红枣饼',NULL,'早餐','','2026-05-09',1,NULL,NULL),(2,300,'2026-05-09 15:56:24.326777','西瓜',NULL,'加餐','','2026-05-09',1,NULL,NULL),(3,280,'2026-05-09 15:56:41.862981','无糖红枣饼',NULL,'午餐','','2026-05-09',1,'16:50:00.000000','16:27:00.000000'),(5,350,'2026-05-13 14:42:26.883857','豆浆 煎蛋  茴香饼',NULL,'早餐','','2026-05-13',1,NULL,NULL),(6,450,'2026-05-13 14:42:56.348086','卤牛肉 西红柿 卤菜 发面饼',NULL,'午餐','','2026-05-13',1,NULL,NULL),(7,240,'2026-05-14 14:39:14.998520','饼   无油煎蛋 ',NULL,'早餐','','2026-05-14',1,NULL,NULL),(8,470,'2026-05-14 14:39:50.317895','卤牛肉 卤菜 洋葱',NULL,'午餐','','2026-05-14',1,NULL,NULL),(9,470,'2026-05-14 19:28:54.074735','饼子 卤牛肉。卤菜。洋葱',NULL,'晚餐','','2026-05-14',1,NULL,NULL),(10,340,'2026-05-15 16:04:25.623380','煎蛋2 饼子1',NULL,'早餐','','2026-05-15',1,NULL,NULL),(11,880,'2026-05-15 16:05:02.647097','火鸡面 生菜 豆芽 卤牛肉  凤梨 车厘子',NULL,'午餐','','2026-05-15',1,NULL,NULL),(12,430,'2026-05-18 16:16:49.624788','煎蛋2  一块自制面包 2碗酸奶',NULL,'早餐','','2026-05-18',1,NULL,NULL),(13,430,'2026-05-18 16:17:35.630413','海带苗  金针菇 西兰花  自制面包',NULL,'午餐','','2026-05-18',1,NULL,NULL);
/*!40000 ALTER TABLE `diet_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `emotion_log`
--

DROP TABLE IF EXISTS `emotion_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `emotion_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ai_intervened` bit(1) DEFAULT NULL,
  `ai_response` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `emotion` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `emotion_trigger` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `level` int DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `emotion_log`
--

LOCK TABLES `emotion_log` WRITE;
/*!40000 ALTER TABLE `emotion_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `emotion_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `exercise_record`
--

DROP TABLE IF EXISTS `exercise_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `exercise_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `calories` int DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `duration` int DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `exercise_time` time(6) DEFAULT NULL,
  `end_time` time(6) DEFAULT NULL,
  `start_time` time(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `exercise_record`
--

LOCK TABLES `exercise_record` WRITE;
/*!40000 ALTER TABLE `exercise_record` DISABLE KEYS */;
INSERT INTO `exercise_record` VALUES (1,200,'2026-05-09 15:55:50.185529',30,'','2026-05-09','快走',1,'15:30:00.000000',NULL,NULL),(3,400,'2026-05-13 14:41:47.560473',50,'感觉良好','2026-05-13','快走',1,NULL,'13:00:00.000000','12:00:00.000000'),(4,400,'2026-05-14 19:28:02.861840',45,'','2026-05-14','快走',1,NULL,NULL,NULL),(5,170,'2026-05-14 19:28:22.514916',30,'','2026-05-14','健身训练',1,NULL,NULL,NULL),(6,230,'2026-05-15 16:03:57.347013',30,'','2026-05-15','快走',1,NULL,NULL,NULL),(7,400,'2026-05-18 16:15:33.771501',45,'','2026-05-18','爬坡',1,NULL,NULL,NULL);
/*!40000 ALTER TABLE `exercise_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `knowledge_article`
--

DROP TABLE IF EXISTS `knowledge_article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_article` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `status` int NOT NULL,
  `summary` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `view_count` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge_article`
--

LOCK TABLES `knowledge_article` WRITE;
/*!40000 ALTER TABLE `knowledge_article` DISABLE KEYS */;
/*!40000 ALTER TABLE `knowledge_article` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menstrual_records`
--

DROP TABLE IF EXISTS `menstrual_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menstrual_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID，自增主键',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '记录创建时间',
  `cycle_end_date` date DEFAULT NULL COMMENT '本次经期结束日期',
  `cycle_length` int DEFAULT NULL COMMENT '本次周期天数',
  `cycle_start_date` date NOT NULL COMMENT '本次经期开始日期',
  `flow_level` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '经量: light=少/medium=中/heavy=多',
  `symptoms` text COLLATE utf8mb4_unicode_ci COMMENT '症状，逗号分隔: bloating=腹胀/cramps=痛经/fatigue=疲倦/headache=头痛/mood_swings=情绪波动/acne=长痘',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联users.id',
  `has_pain` bit(1) DEFAULT NULL,
  `is_in_period` bit(1) DEFAULT NULL,
  `other_info` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='经期记录表，支持历史查询和周期分析';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menstrual_records`
--

LOCK TABLES `menstrual_records` WRITE;
/*!40000 ALTER TABLE `menstrual_records` DISABLE KEYS */;
INSERT INTO `menstrual_records` VALUES (1,'2026-05-07 17:48:32.287106','2026-05-05',5,'2026-05-01','medium','bloating,cramps,fatigue',2,NULL,NULL,NULL),(7,'2026-05-09 11:10:30.912711','2026-04-11',5,'2026-04-07','medium',NULL,1,_binary '\0',_binary '\0',NULL),(8,'2026-05-09 11:14:41.651056','2026-03-09',5,'2026-03-05','medium',NULL,1,_binary '\0',_binary '\0',NULL),(9,'2026-05-09 11:24:06.528998','2025-12-12',5,'2025-12-08','medium',NULL,1,_binary '\0',_binary '\0',NULL),(10,'2026-05-09 11:28:40.921806','2026-01-12',5,'2026-01-08','medium',NULL,1,_binary '\0',_binary '\0',NULL),(11,'2026-05-09 11:28:51.961651','2025-11-06',5,'2025-11-02','medium',NULL,1,_binary '\0',_binary '\0',NULL),(12,'2026-05-09 11:29:01.032887','2025-10-10',5,'2025-10-06','medium',NULL,1,_binary '\0',_binary '\0',NULL),(14,'2026-05-09 11:29:25.511641','2025-09-11',5,'2025-09-07','medium',NULL,1,_binary '\0',_binary '\0',NULL),(15,'2026-05-09 11:33:42.249803','2025-08-12',5,'2025-08-08','medium',NULL,1,_binary '\0',_binary '\0',NULL),(16,'2026-05-09 11:33:51.084731','2025-06-12',5,'2025-06-08','medium',NULL,1,_binary '\0',_binary '\0',NULL),(17,'2026-05-09 11:33:56.866372','2025-05-15',5,'2025-05-11','medium',NULL,1,_binary '\0',_binary '\0',NULL),(20,'2026-05-09 11:34:29.587843','2025-04-17',5,'2025-04-13','medium',NULL,1,_binary '\0',_binary '\0',NULL),(22,'2026-05-09 11:34:44.123994','2025-03-17',5,'2025-03-13','medium',NULL,1,_binary '\0',_binary '\0',NULL),(23,'2026-05-09 11:34:51.663131','2025-02-19',5,'2025-02-15','medium',NULL,1,_binary '\0',_binary '\0',NULL),(24,'2026-05-09 11:34:57.044835','2025-01-16',5,'2025-01-12','medium',NULL,1,_binary '\0',_binary '\0',NULL),(25,'2026-05-09 11:35:02.822653','2024-12-23',5,'2024-12-19','medium',NULL,1,_binary '\0',_binary '\0',NULL),(26,'2026-05-09 11:35:08.557906','2024-11-18',5,'2024-11-14','medium',NULL,1,_binary '\0',_binary '\0',NULL),(27,'2026-05-09 11:35:13.209066','2024-10-23',7,'2024-10-17','medium',NULL,1,_binary '\0',_binary '\0',NULL),(28,'2026-05-09 11:35:17.673293','2024-09-19',5,'2024-09-15','medium',NULL,1,_binary '\0',_binary '\0',NULL),(29,'2026-05-09 11:35:23.834797','2024-08-18',5,'2024-08-14','medium',NULL,1,_binary '\0',_binary '\0',NULL),(30,'2026-05-09 11:35:28.848441','2024-07-20',4,'2024-07-17','medium',NULL,1,_binary '\0',_binary '\0',NULL),(31,'2026-05-09 11:35:33.041006','2024-06-24',5,'2024-06-20','medium',NULL,1,_binary '\0',_binary '\0',NULL),(32,'2026-05-09 11:35:37.065361','2024-05-25',6,'2024-05-20','medium',NULL,1,_binary '\0',_binary '\0',NULL),(33,'2026-05-09 11:35:41.521921','2024-04-20',5,'2024-04-16','medium',NULL,1,_binary '\0',_binary '\0',NULL),(34,'2026-05-09 11:35:46.838870','2024-03-05',5,'2024-03-01','medium',NULL,1,_binary '\0',_binary '\0',NULL),(35,'2026-05-09 15:10:33.434093','2026-02-10',5,'2026-02-06','medium',NULL,1,_binary '\0',_binary '\0',NULL),(45,'2026-05-18 14:55:50.839071','2026-05-15',5,'2026-05-11',NULL,NULL,1,_binary '\0',_binary '\0',NULL);
/*!40000 ALTER TABLE `menstrual_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sedentary_record`
--

DROP TABLE IF EXISTS `sedentary_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sedentary_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `goal_minutes` int DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `stand_count` int DEFAULT NULL,
  `total_sedentary_minutes` int DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sedentary_record`
--

LOCK TABLES `sedentary_record` WRITE;
/*!40000 ALTER TABLE `sedentary_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `sedentary_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，自增主键',
  `age` int DEFAULT NULL COMMENT '年龄（岁）',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户头像URL',
  `basic_metabolism` double DEFAULT NULL COMMENT '基础代谢率（kcal/天）',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `gender` int DEFAULT NULL COMMENT '性别: 0=未知, 1=男, 2=女',
  `height` double DEFAULT NULL COMMENT '身高（cm）',
  `nickname` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '减减用户' COMMENT '用户昵称',
  `openid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '微信OpenID，唯一标识用户',
  `target_weight` double DEFAULT NULL COMMENT '目标体重（kg）',
  `updated_at` datetime(6) DEFAULT NULL COMMENT '最后更新时间',
  `initial_weight` double DEFAULT NULL COMMENT '初始体重（kg）',
  `reminder_enabled` bit(1) DEFAULT b'1' COMMENT '是否启用体重提醒: true=启用, false=关闭',
  `reminder_interval_hours` int DEFAULT '24' COMMENT '提醒间隔（小时）',
  `weight_loss_period` int DEFAULT NULL COMMENT '减重周期（天）',
  `start_weight_date` date DEFAULT NULL COMMENT '开始减重日期',
  `dietary_taboo` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '饮食忌口，逗号分隔',
  `sleep_end` time(6) DEFAULT NULL,
  `sleep_start` time(6) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `login_type` int DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `session_key` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `token_expire_at` datetime(6) DEFAULT NULL,
  `breakfast_habit` bit(1) DEFAULT NULL,
  `constitution_tags` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diet_preference` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `exercise_frequency` int DEFAULT NULL,
  `exercise_preference` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fitness_level` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `has_knee_issue` bit(1) DEFAULT NULL,
  `meal_times` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `standing_hours` int DEFAULT NULL,
  `target_areas` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_fat` double DEFAULT NULL,
  `user_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `water_intake` int DEFAULT NULL,
  `work_pressure` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_badofxhbq3oi2d4u7fj8w1kt8` (`openid`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,28,'https://thirdwx.qlogo.cn/mmopen/vi_32/POgEwh4mIHO4nibH0KlMECNjjGxQUq24ZEaGT4poC6icRiccVGKSyXwibcPq4BWmiaIGuG1icwxaQX6grC9VemZoJ8rg/132',1399,'2026-05-07 14:52:24.357052',2,160,'小moon','odvt03d7a2IYh4_XJubc6kkEFkoE',55,'2026-05-10 17:52:07.942848',70,_binary '',24,180,'2026-03-07','香菜 蒜 生姜','08:00:00.000000','23:00:00.000000','2026-05-18 17:21:20.272884',1,NULL,NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxIiwib3BlbmlkIjoib2R2dDAzZDdhMklZaDRfWEp1YmM2a2tFRmtvRSIsImlzR3Vlc3QiOmZhbHNlLCJpYXQiOjE3Nzg0MjAyMzEsImV4cCI6MTc3OTAyNTAzMX0.U0bpnUWEOdCQm53vGdgzpg3rl6YnkHdeqMSJjupgsOJpNs62TE1o5Tv7koXrAOQW','2026-05-17 21:37:11.233007',_binary '','易水肿,压力胖','自己做饭',5,'健身房','新手',_binary '\0',NULL,NULL,NULL,NULL,'weight_loss',2000,3),(2,25,NULL,1774,'2026-05-07 16:14:46.272170',1,175,'小明','test_dev',65,'2026-05-07 18:07:15.299206',80,_binary '',12,NULL,'2026-03-06','海鲜','06:00:00.000000','22:00:00.000000',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(4,NULL,NULL,NULL,'2026-05-07 22:16:45.635079',NULL,NULL,'减减用户','test',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(5,NULL,NULL,NULL,'2026-05-09 17:10:34.085222',NULL,NULL,'游客','guest_test_device_123',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-09 17:50:26.456734',0,NULL,NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI1Iiwib3BlbmlkIjoiZ3Vlc3RfdGVzdF9kZXZpY2VfMTIzIiwiaXNHdWVzdCI6dHJ1ZSwiaWF0IjoxNzc4MzIwMjI2LCJleHAiOjE3Nzg5MjUwMjZ9.mpFH3jIpXNnpBMbFdxyIaaIDNNvgPg3L6j537og2EGPuirlFgGd805y3gzE9dpew','2026-05-16 17:50:26.478642',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(6,NULL,NULL,NULL,'2026-05-09 17:12:47.306740',NULL,NULL,'游客','guest_test_device_456',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-09 17:12:47.306758',0,NULL,NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJudWxsIiwib3BlbmlkIjoiZ3Vlc3RfdGVzdF9kZXZpY2VfNDU2IiwiaXNHdWVzdCI6dHJ1ZSwiaWF0IjoxNzc4MzE3OTY3LCJleHAiOjE3Nzg5MjI3Njd9.tWTm8GK3MTFJGmXm1qRY0s1l3dpej6EzCcT_sy9LqjzWIY_UGgNLA4cz2Fj5hiLs','2026-05-16 17:12:47.307022',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(7,NULL,NULL,NULL,'2026-05-09 17:16:27.354212',NULL,NULL,'游客','guest_test_device_final',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-09 17:16:27.354227',0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(8,NULL,NULL,NULL,'2026-05-09 17:23:40.879642',NULL,NULL,'游客','guest_web_1778318620813_d739cy68k',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-09 17:41:15.045246',0,NULL,NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI4Iiwib3BlbmlkIjoiZ3Vlc3Rfd2ViXzE3NzgzMTg2MjA4MTNfZDczOWN5NjhrIiwiaXNHdWVzdCI6dHJ1ZSwiaWF0IjoxNzc4MzE5Njc1LCJleHAiOjE3Nzg5MjQ0NzV9.bZdcM01PzPLb6El9c4T7KoQPVEYzpWNb0WWm-Na3UiaUYtUf9dFUNYN0oMWWDW9b','2026-05-16 17:41:15.046438',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(9,NULL,NULL,NULL,'2026-05-09 17:34:31.436914',NULL,NULL,'测试用户','phone_13800138003',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-09 17:34:31.436934',2,'13800138003',NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI5Iiwib3BlbmlkIjoicGhvbmVfMTM4MDAxMzgwMDMiLCJpc0d1ZXN0IjpmYWxzZSwiaWF0IjoxNzc4MzE5MjcxLCJleHAiOjE3Nzg5MjQwNzF9.5cg3DsoazUH7FXkIa7RrJsRCehev0KcMuLUYR4N-Qt0mEWJrzNtTdjM0-dntW0vH','2026-05-16 17:34:31.496172',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(10,NULL,NULL,NULL,'2026-05-09 17:53:56.588836',NULL,NULL,'模拟','phone_18234034922',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-09 17:57:24.351116',2,'18234034922',NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxMCIsIm9wZW5pZCI6InBob25lXzE4MjM0MDM0OTIyIiwiaXNHdWVzdCI6ZmFsc2UsImlhdCI6MTc3ODMyMDY0NCwiZXhwIjoxNzc4OTI1NDQ0fQ.1J-NdqL2P4wrUt3BpccxdeZJIVa-gDQvKZ2OSurnZ_CezoV8W71RT_F49nVa86tP','2026-05-16 17:57:24.351763',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(11,NULL,NULL,NULL,'2026-05-09 17:54:44.547334',NULL,NULL,'测试','phone_13900000001',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-09 17:54:44.547343',2,'13900000001',NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxMSIsIm9wZW5pZCI6InBob25lXzEzOTAwMDAwMDAxIiwiaXNHdWVzdCI6ZmFsc2UsImlhdCI6MTc3ODMyMDQ4NCwiZXhwIjoxNzc4OTI1Mjg0fQ.IAwPD6-nv2of9txEvcMl9sUkeccepu1Zsmi_ygzeKesF132jPpyz99aUSL4Jtenz','2026-05-16 17:54:44.554354',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(12,NULL,NULL,NULL,'2026-05-09 17:59:41.854830',NULL,NULL,'游客','guest_mini_3bz0o893t2lmoy6a12e',NULL,NULL,NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-09 18:04:57.875837',0,NULL,NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxMiIsIm9wZW5pZCI6Imd1ZXN0X21pbmlfM2J6MG84OTN0Mmxtb3k2YTEyZSIsImlzR3Vlc3QiOnRydWUsImlhdCI6MTc3ODMyMTA5NywiZXhwIjoxNzc4OTI1ODk3fQ.lmxoJepAgYo_xT3aiY2nCIDu5qSfTWAmfsHIKv5wuXr2rbJpEeOKa5lNx7QVIZCK','2026-05-16 18:04:57.876503',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'',NULL,NULL),(13,25,NULL,1345,'2026-05-09 18:00:46.762182',2,165,'测试用户','guest_test123',60,'2026-05-10 21:55:44.378589',NULL,_binary '',24,NULL,NULL,NULL,NULL,NULL,'2026-05-10 21:51:36.598118',0,NULL,NULL,'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiIxMyIsIm9wZW5pZCI6Imd1ZXN0X3Rlc3QxMjMiLCJpc0d1ZXN0Ijp0cnVlLCJpYXQiOjE3Nzg0MjEwOTYsImV4cCI6MTc3OTAyNTg5Nn0.P1Ihdw42YgsI5uG5-URMkmgz-NQhDKV_zdJke0ddKjIS-yeSKzYGCit0OnzugvHu','2026-05-17 21:51:36.623203',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'weight_loss',NULL,NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `water_records`
--

DROP TABLE IF EXISTS `water_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `water_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` int DEFAULT NULL,
  `created_at` bigint DEFAULT NULL,
  `record_date` date DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `water_records`
--

LOCK TABLES `water_records` WRITE;
/*!40000 ALTER TABLE `water_records` DISABLE KEYS */;
INSERT INTO `water_records` VALUES (1,2000,1778757732315,'2026-05-14',1),(2,200,1778832499070,'2026-05-15',1),(3,500,1779074607223,'2026-05-18',1),(4,200,1779076688227,'2026-05-18',1),(5,200,1779076695192,'2026-05-18',1),(6,200,1779092170665,'2026-05-18',1);
/*!40000 ALTER TABLE `water_records` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weight_records`
--

DROP TABLE IF EXISTS `weight_records`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weight_records` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID，自增主键',
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '记录创建时间',
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注，记录当日特殊情况',
  `record_date` date NOT NULL COMMENT '记录日期（自然日期，非创建时间）',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联users.id',
  `weight` double NOT NULL COMMENT '体重数值（kg）',
  `sleep_end` time(6) DEFAULT NULL,
  `sleep_start` time(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='体重记录表，每日体重数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weight_records`
--

LOCK TABLES `weight_records` WRITE;
/*!40000 ALTER TABLE `weight_records` DISABLE KEYS */;
INSERT INTO `weight_records` VALUES (11,'2026-05-07 22:01:19.222697',NULL,'2026-03-06',1,70,NULL,NULL),(13,'2026-05-07 22:33:14.409988',NULL,'2026-05-06',1,65.5,NULL,NULL),(14,'2026-05-08 15:52:04.453734',NULL,'2026-05-08',1,65,NULL,NULL),(15,'2026-05-09 09:20:29.257212',NULL,'2026-05-09',1,65.8,'08:35:00.000000','23:06:00.000000'),(16,'2026-05-09 09:23:00.379619',NULL,'2026-05-07',1,64.5,NULL,NULL),(17,'2026-05-09 09:23:00.381410',NULL,'2026-03-08',1,68.7,NULL,NULL),(18,'2026-05-09 09:23:00.379652',NULL,'2026-03-09',1,67.6,NULL,NULL),(19,'2026-05-09 09:23:00.381410',NULL,'2026-03-10',1,66.6,NULL,NULL),(20,'2026-05-09 09:24:46.540394',NULL,'2026-03-12',1,67.2,NULL,NULL),(21,'2026-05-09 09:24:46.540392',NULL,'2026-03-11',1,68.45,NULL,NULL),(22,'2026-05-09 09:33:16.379594',NULL,'2026-03-22',1,68.05,NULL,NULL),(23,'2026-05-09 09:33:16.379601',NULL,'2026-03-07',1,69.45,NULL,NULL),(24,'2026-05-09 09:33:16.379587',NULL,'2026-03-18',1,67.15,NULL,NULL),(25,'2026-05-09 09:33:16.379587',NULL,'2026-03-14',1,66.95,NULL,NULL),(26,'2026-05-09 09:33:16.379587',NULL,'2026-03-17',1,66.7,NULL,NULL),(27,'2026-05-09 09:33:16.379587',NULL,'2026-03-16',1,67.45,NULL,NULL),(28,'2026-05-09 09:33:16.394546',NULL,'2026-03-19',1,66.65,NULL,NULL),(29,'2026-05-09 09:33:16.394546',NULL,'2026-03-21',1,66.6,NULL,NULL),(30,'2026-05-09 09:33:16.395800',NULL,'2026-03-23',1,66.6,NULL,NULL),(31,'2026-05-09 09:33:16.395632',NULL,'2026-03-20',1,66.35,NULL,NULL),(32,'2026-05-09 09:33:16.395974',NULL,'2026-03-25',1,67.6,NULL,NULL),(33,'2026-05-09 09:33:16.396433',NULL,'2026-03-26',1,66.55,NULL,NULL),(34,'2026-05-09 09:33:16.402943',NULL,'2026-03-24',1,66.8,NULL,NULL),(35,'2026-05-09 09:33:16.403810',NULL,'2026-03-27',1,66,NULL,NULL),(36,'2026-05-09 09:49:07.201280',NULL,'2026-03-31',1,66.1,NULL,NULL),(37,'2026-05-09 09:49:07.201280',NULL,'2026-03-30',1,66.4,NULL,NULL),(38,'2026-05-09 09:49:07.201280',NULL,'2026-03-29',1,66.25,NULL,NULL),(39,'2026-05-09 09:49:07.201976',NULL,'2026-03-28',1,66.45,NULL,NULL),(40,'2026-05-09 09:49:07.201976',NULL,'2026-04-01',1,66,NULL,NULL),(41,'2026-05-09 09:49:07.202801',NULL,'2026-04-02',1,65.65,NULL,NULL),(42,'2026-05-09 09:49:07.215821',NULL,'2026-04-13',1,65.65,NULL,NULL),(43,'2026-05-09 09:49:07.215790',NULL,'2026-04-06',1,66.15,NULL,NULL),(44,'2026-05-09 09:49:07.215864',NULL,'2026-04-03',1,65.15,NULL,NULL),(45,'2026-05-09 09:49:07.215796',NULL,'2026-04-04',1,65.75,NULL,NULL),(46,'2026-05-09 09:49:07.217329',NULL,'2026-04-14',1,65.35,NULL,NULL),(47,'2026-05-09 09:49:07.225930',NULL,'2026-04-17',1,64.9,NULL,NULL),(48,'2026-05-09 09:49:07.226068',NULL,'2026-04-16',1,65,NULL,NULL),(49,'2026-05-09 09:49:07.226115',NULL,'2026-04-22',1,64.35,NULL,NULL),(50,'2026-05-09 09:49:07.226055',NULL,'2026-04-18',1,64.5,NULL,NULL),(51,'2026-05-09 09:49:07.225930',NULL,'2026-04-19',1,65.65,NULL,NULL),(52,'2026-05-09 09:49:07.225930',NULL,'2026-04-15',1,65.15,NULL,NULL),(53,'2026-05-09 09:49:07.234758',NULL,'2026-04-28',1,63.95,NULL,NULL),(54,'2026-05-09 09:49:07.234878',NULL,'2026-04-27',1,64.2,NULL,NULL),(55,'2026-05-09 09:49:07.234758',NULL,'2026-04-24',1,64.1,NULL,NULL),(56,'2026-05-09 09:49:07.234878',NULL,'2026-04-23',1,63.85,NULL,NULL),(57,'2026-05-09 09:49:07.234875',NULL,'2026-04-29',1,63.8,NULL,NULL),(58,'2026-05-09 09:49:07.234867',NULL,'2026-04-30',1,63.6,NULL,NULL),(59,'2026-05-09 09:49:07.246320',NULL,'2026-05-04',1,64.5,NULL,NULL),(60,'2026-05-09 09:49:07.246320',NULL,'2026-05-05',1,64.45,NULL,NULL),(61,'2026-05-09 09:49:07.246320',NULL,'2026-05-02',1,64,NULL,NULL),(62,'2026-05-09 09:49:07.246361',NULL,'2026-05-03',1,64.5,NULL,NULL),(63,'2026-05-10 11:34:59.585852',NULL,'2026-05-10',1,65.3,'10:30:00.000000','23:00:00.000000'),(64,'2026-05-10 21:55:11.510300',NULL,'2026-05-10',13,70.5,NULL,NULL),(65,'2026-05-13 14:38:57.995513',NULL,'2026-05-13',1,64.6,'08:15:00.000000','23:00:00.000000'),(66,'2026-05-14 14:38:32.928559',NULL,'2026-05-14',1,64.5,'08:07:00.000000','23:00:00.000000'),(67,'2026-05-15 16:02:27.699926',NULL,'2026-05-15',1,64.6,'08:10:00.000000','00:10:00.000000'),(68,'2026-05-18 10:25:06.107921',NULL,'2026-05-18',1,64.9,NULL,NULL);
/*!40000 ALTER TABLE `weight_records` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-19 10:03:19
