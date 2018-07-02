create table message (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  sender VARCHAR(256) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table backlog_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  summary VARCHAR(256) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  type VARCHAR(36) NOT NULL check (type in ('IDEA', 'ISSUE')),
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table epoch (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  name VARCHAR(256) NOT NULL,
  totem VARCHAR(256) NOT NULL,
  question VARCHAR(256) NOT NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table year (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  epoch_id VARCHAR(36) NOT NULL,
  start_date DATE NOT NULL,
  finish_date DATE NOT NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table theme (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  year_id VARCHAR(36) NOT NULL,
  name VARCHAR(256) NOT NULL,
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table goal (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  theme_id VARCHAR(36) NOT NULL,
  summary VARCHAR(256) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  level INT(20) NOT NULL,
  priority TINYINT(1) NOT NULL,
  status VARCHAR(36) NOT NULL check (status in ('NOT_ACHIEVED', 'EMPLOYED', 'UNEMPLOYED')),
  graduation VARCHAR(36) NOT NULL check (graduation in ('ABANDONED', 'THREAD', 'WEAVE', 'HOBBY', 'GOAL')),
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table goal_backlog_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  goal_id BIGINT NOT NULL,
  backlog_item_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT goal_fk FOREIGN KEY (goal_id) REFERENCES goal (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT backlog_item_fk FOREIGN KEY (backlog_item_id) REFERENCES backlog_item (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

create table thread (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  goal_id VARCHAR(36),
  summary VARCHAR(256) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  status VARCHAR(36) NOT NULL check (status in ('NOT_ACHIEVED', 'EMPLOYED', 'UNEMPLOYED')),
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  last_performed TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table weave (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  goal_id VARCHAR(36),
  summary VARCHAR(256) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  status VARCHAR(36) NOT NULL check (status in ('PLANNED', 'IN_PROGRESS', 'COMPLETE')),
  type VARCHAR(36) NOT NULL check (type in ('PRIORITY', 'PDR', 'BAU')),
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  last_performed TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table laser_donut (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  goal_id VARCHAR(36) NOT NULL,
  summary VARCHAR(256) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  milestone VARCHAR(256) NOT NULL,
  `order` INT(20) NOT NULL,
  status VARCHAR(36) NOT NULL check (status in ('PLANNED', 'IN_PROGRESS', 'COMPLETE')),
  type VARCHAR(36) NOT NULL check (type in ('PROJECT_FOCUSED', 'SKILL_FOCUSED')),
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  last_performed TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table portion (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  laser_donut_id VARCHAR(36) NOT NULL,
  summary VARCHAR(256) NOT NULL,
  `order` INT(20) NOT NULL,
  status VARCHAR(36) NOT NULL check (status in ('PLANNED', 'IN_PROGRESS', 'COMPLETE')),
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  last_performed TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table todo (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  portion_id VARCHAR(36) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  `order` INT(20) NOT NULL,
  status VARCHAR(36) NOT NULL check (status in ('PLANNED', 'IN_PROGRESS', 'COMPLETE')),
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  last_performed TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table hobby (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  goal_id VARCHAR(36),
  summary VARCHAR(256) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  frequency VARCHAR(36) NOT NULL check (frequency in ('ONE_OFF', 'CONTINUOUS')),
  status VARCHAR(36) NOT NULL check (status in ('PLANNED', 'IN_PROGRESS', 'COMPLETE')),
  type VARCHAR(36) NOT NULL check (type in ('ACTIVE', 'PASSIVE')),
  created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_modified TIMESTAMP,
  last_performed TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;

create table scheduled_laser_donut (
  id BIGINT NOT NULL AUTO_INCREMENT,
  laser_donut_id BIGINT NOT NULL,
  tier INT(20) NOT NULL,
  current TINYINT(1) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (laser_donut_id),
  CONSTRAINT laser_donut_fk FOREIGN KEY (laser_donut_id) REFERENCES laser_donut (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

create table current_activity (
  id BIGINT NOT NULL AUTO_INCREMENT,
  current_laser_donut BIGINT NOT NULL,
  current_portion BIGINT NOT NULL,
  last_daily_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_weekly_update TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY (current_laser_donut, current_portion),
  CONSTRAINT current_laser_donut_fk FOREIGN KEY (current_laser_donut) REFERENCES laser_donut (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT current_portion_fk FOREIGN KEY (current_portion) REFERENCES portion (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;
