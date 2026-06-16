CREATE TABLE `flowmeter` (
  `moment` datetime DEFAULT NULL,
  `pid` varchar(20) DEFAULT NULL,
  `father_pid` varchar(20) DEFAULT NULL,
  `root_pid` varchar(20) DEFAULT NULL,
  `system_pid` bigint(8) DEFAULT NULL,
  `project` varchar(50) DEFAULT NULL,
  `job` varchar(255) DEFAULT NULL,
  `job_repository_id` varchar(255) DEFAULT NULL,
  `job_version` varchar(255) DEFAULT NULL,
  `context` varchar(50) DEFAULT NULL,
  `origin` varchar(255) DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `count` int(3) DEFAULT NULL,
  `reference` int(3) DEFAULT NULL,
  `thresholds` varchar(255) DEFAULT NULL
)

CREATE TABLE `logcatcher` (
  `moment` datetime DEFAULT NULL,
  `pid` varchar(20) DEFAULT NULL,
  `root_pid` varchar(20) DEFAULT NULL,
  `father_pid` varchar(20) DEFAULT NULL,
  `project` varchar(50) DEFAULT NULL,
  `job` varchar(255) DEFAULT NULL,
  `context` varchar(50) DEFAULT NULL,
  `priority` int(3) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `origin` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `code` int(3) DEFAULT NULL
) 

CREATE TABLE `statcatcher` (
  `moment` datetime DEFAULT NULL,
  `pid` varchar(20) DEFAULT NULL,
  `father_pid` varchar(20) DEFAULT NULL,
  `root_pid` varchar(20) DEFAULT NULL,
  `system_pid` bigint(8) DEFAULT NULL,
  `project` varchar(50) DEFAULT NULL,
  `job` varchar(255) DEFAULT NULL,
  `job_repository_id` varchar(255) DEFAULT NULL,
  `job_version` varchar(255) DEFAULT NULL,
  `context` varchar(50) DEFAULT NULL,
  `origin` varchar(255) DEFAULT NULL,
  `message_type` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `duration` bigint(8) DEFAULT NULL
)