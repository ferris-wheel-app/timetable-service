create table message (
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid VARCHAR(36) NOT NULL,
  sender VARCHAR(256) NOT NULL,
  content VARCHAR(2000) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (uuid)
) ENGINE=InnoDB;
