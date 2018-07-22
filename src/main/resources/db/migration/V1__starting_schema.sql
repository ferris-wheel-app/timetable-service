create table message (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  sender VARCHAR(256) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table routine (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  name VARCHAR(256) NOT NULL,
  is_current TINYINT(1) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table time_block (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  start_time TIME NOT NULL,
  finish_time TIME NOT NULL,
  task_type VARCHAR(36) NOT NULL check (task_type in ('THREAD', 'WEAVE', 'LASER_DONUT', 'HOBBY')),
  task_id VARCHAR(36),
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table routine_time_block (
  id BIGINT NOT NULL AUTO_INCREMENT,
  routine_id BIGINT NOT NULL,
  time_block_id BIGINT NOT NULL,
  day_of_week VARCHAR(36) NOT NULL check (day_of_week in ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
  PRIMARY KEY (id),
  CONSTRAINT routine_fk FOREIGN KEY (routine_id) REFERENCES routine (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT time_block_fk_1 FOREIGN KEY (time_block_id) REFERENCES time_block (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

create table scheduled_time_block (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  date DATE NOT NULL,
  start_time TIME NOT NULL,
  finish_time TIME NOT NULL,
  task_type VARCHAR(36) NOT NULL check (task_type in ('THREAD', 'WEAVE', 'LASER_DONUT', 'HOBBY')),
  task_id VARCHAR(36) NOT NULL,
  temporal_status VARCHAR(36) NOT NULL check (temporal_status in ('PREVIOUSLY', 'RIGHT_NOW', 'UPCOMING')),
  is_done TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;
