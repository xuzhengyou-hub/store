DROP TABLE IF EXISTS ums_user;

CREATE TABLE ums_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(20) DEFAULT NULL,
  nickname VARCHAR(64) DEFAULT NULL,
  avatar VARCHAR(255) DEFAULT NULL,
  status TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_deleted TINYINT DEFAULT 0
);

CREATE UNIQUE INDEX idx_username ON ums_user (username);
CREATE UNIQUE INDEX idx_phone ON ums_user (phone);

DROP TABLE IF EXISTS pms_sku;
DROP TABLE IF EXISTS pms_spu;

CREATE TABLE pms_spu (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL,
  description VARCHAR(255),
  main_image VARCHAR(255) NOT NULL,
  detail_html CLOB,
  publish_status TINYINT DEFAULT 1,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pms_sku (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  spu_id BIGINT NOT NULL,
  sku_name VARCHAR(255) NOT NULL,
  sku_code VARCHAR(64) NOT NULL,
  price DECIMAL(10, 2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  lock_stock INT NOT NULL DEFAULT 0,
  image VARCHAR(255),
  attributes VARCHAR(1000),
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_spu_id ON pms_sku (spu_id);

DROP TABLE IF EXISTS oms_cart_item;

CREATE TABLE oms_cart_item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  spu_id BIGINT NOT NULL,
  sku_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  price DECIMAL(10, 2) NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cart_user_id ON oms_cart_item (user_id);

DROP TABLE IF EXISTS ums_user_address;

CREATE TABLE ums_user_address (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  receiver_name VARCHAR(64) NOT NULL,
  receiver_phone VARCHAR(32) NOT NULL,
  province VARCHAR(64),
  city VARCHAR(64),
  detail_address VARCHAR(255) NOT NULL,
  is_default TINYINT DEFAULT 0,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_address_user_id ON ums_user_address (user_id);
