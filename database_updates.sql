-- MySQL dump 10.13  Distrib 5.5.25a, for Linux (x86_64)
--
-- Host: localhost    Database: quantimodo
-- ------------------------------------------------------
-- Server version       5.5.25a-log

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
-- Table structure for table `qm_filling_type`
--

DROP TABLE IF EXISTS `qm_filling_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `qm_filling_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `description` varchar(100) DEFAULT NULL,
  `type` int(10) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `type_id` (`type`),
  UNIQUE KEY `type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `qm_filling_type`
--

LOCK TABLES `qm_filling_type` WRITE;
/*!40000 ALTER TABLE `qm_filling_type` DISABLE KEYS */;
INSERT INTO `qm_filling_type` VALUES (1,'Assuming data is missing',0),(2,'Assume {0} for that {1}',1),(3,'Interpolate the surrounding {0} days',2),(4,'Use a typical value (median of all {0})',3),(5,'Use the average value (mean of all {0})',4);
/*!40000 ALTER TABLE `qm_filling_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2013-04-17 16:17:54

alter table qm_variable_categories add column data_owner bigint(20) not null default 0;
alter table qm_variable_categories drop index `Avoid duplicate names`;
alter table qm_variable_categories add unique index name(name,data_owner);

create table qm_variable_categorizing (id int unsigned not null auto_increment, category_id int unsigned not null, variable_id int unsigned not null, guest_id bigint(20), primary key (id), constraint category_fk foreign key(category_id) references qm_variable_categories(id), constraint variable_fk foreign key(variable_id) references qm_variables(id), constraint guest_id_fk foreign key(guest_id) references Guest(id));

alter table qm_variables add column type tinyint(1) not null default 0 comment '0 - single, 1 - aggregated';
create table qm_variable_remaining (id bigint(20) not null auto_increment, aggregated_variable_id int(10) unsigned not null, variable_id int(10) unsigned not null, primary key(id), constraint aggregated_variable_fk foreign key(aggregated_variable_id) references qm_variables(id), constraint variable_fk foreign key(variable_id) references qm_variables(id));

alter table qm_variables change column min_value min_value float default null;
alter table qm_variables change column max_value max_value float default null;
alter table qm_variables change column filling_value filling_value float default null;
alter table qm_variables change column time_shift time_shift float default null;

create table qm_variable_prioritized (id bigint(20) not null auto_increment, aggregated_variable_id int(10) unsigned not null, variable_id int(10) unsigned not null, priority int(10) unsigned not null default 0, primary key(id), constraint prioritized_aggregated_variable_fk foreign key(aggregated_variable_id) references qm_variables(id), constraint prioritized_variable_fk foreign key(variable_id) references qm_variables(id));
alter table qm_variables add column remaining_type tinyint(1) default null comment '0 - summ of values, 1 - mean of values';

alter table qm_variables drop foreign key non_empty_period_unit_type_fk;
alter table qm_variables drop column non_empty_period_unit;
alter table qm_variables add column non_empty_period int(1) default null comment '0 - minutely, 1 - hourly, 2 - daily, 3 - weekly, 4 - monthly';

delete from qm_filling_type where type=4;
update qm_filling_type set description='Use the average value (mean of all {0})' where type=3;

alter table qm_variables drop column summable_variable;