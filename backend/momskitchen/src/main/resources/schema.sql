-- =========================================================
-- Mom's Kitchen — Database Schema (MySQL 8 / InnoDB / utf8mb4)
-- =========================================================
SET NAMES utf8mb4;

-- ---------- Drop in dependency order (safe for local dev) ----------
DROP TABLE IF EXISTS order_item_addon;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS menu_item_addon;
DROP TABLE IF EXISTS menu_item;
DROP TABLE IF EXISTS menu_category;
DROP TABLE IF EXISTS addon;
DROP TABLE IF EXISTS pickup_slot;
DROP TABLE IF EXISTS menu;

-- =========================
-- Core Catalog
-- =========================

CREATE TABLE menu (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(120) NOT NULL,
  description   VARCHAR(500),
  is_active     TINYINT(1) NOT NULL DEFAULT 1,
  created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE menu_category (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  menu_id        BIGINT NOT NULL,
  name           VARCHAR(120) NOT NULL,
  description    VARCHAR(500),
  display_order  INT NOT NULL DEFAULT 0,
  is_active      TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT fk_category_menu
    FOREIGN KEY (menu_id) REFERENCES menu(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_category_menu
  ON menu_category (menu_id, is_active, display_order);

CREATE TABLE menu_item (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id    BIGINT NOT NULL,
  name           VARCHAR(160) NOT NULL,
  description    VARCHAR(1000),
  price          DECIMAL(10,2) NOT NULL,
  is_available   TINYINT(1) NOT NULL DEFAULT 1,
  image_url      VARCHAR(600),
  display_order  INT NOT NULL DEFAULT 0,
  CONSTRAINT fk_item_category
    FOREIGN KEY (category_id) REFERENCES menu_category(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_item_category
  ON menu_item (category_id, is_available, display_order);

CREATE TABLE addon (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(160) NOT NULL,
  description   VARCHAR(500),
  price_delta   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  is_active     TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Join table: which add-ons are allowed for each catalog item
CREATE TABLE menu_item_addon (
  menu_item_id  BIGINT NOT NULL,
  addon_id      BIGINT NOT NULL,
  PRIMARY KEY (menu_item_id, addon_id),
  CONSTRAINT fk_mia_item
    FOREIGN KEY (menu_item_id) REFERENCES menu_item(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_mia_addon
    FOREIGN KEY (addon_id) REFERENCES addon(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- Pickup configuration
-- =========================

CREATE TABLE pickup_slot (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  day_of_week  TINYINT NOT NULL,                 -- 0=Sun ... 6=Sat
  start_time   TIME NOT NULL,
  end_time     TIME NOT NULL,
  is_active    TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_slot_active
  ON pickup_slot (day_of_week, is_active);

-- =========================
-- Orders (note: ORDER is reserved  use backticks)
-- =========================

CREATE TABLE `order` (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_code      VARCHAR(12) NOT NULL UNIQUE,
  status          VARCHAR(40) NOT NULL,          -- PENDING/CONFIRMED/READY/COMPLETED/CANCELED
  pickup_at       DATETIME NOT NULL,
  pickup_slot_id  BIGINT NULL,
  customer_name   VARCHAR(160) NOT NULL,
  customer_email  VARCHAR(200) NOT NULL,
  customer_phone  VARCHAR(40) NOT NULL,
  notes           VARCHAR(500),
  subtotal        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total_amount    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  payment_status  VARCHAR(40) NOT NULL,          -- UNPAID/PAID/REFUNDED
  created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_order_slot
    FOREIGN KEY (pickup_slot_id) REFERENCES pickup_slot(id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_order_lookup
  ON `order` (customer_phone, order_code);

CREATE INDEX ix_order_created
  ON `order` (created_at);

-- Order lines (snapshots of catalog items)
CREATE TABLE order_item (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id       BIGINT NOT NULL,
  menu_item_id   BIGINT NULL,
  item_name      VARCHAR(160) NOT NULL,          -- snapshot of the name
  unit_price     DECIMAL(10,2) NOT NULL,         -- snapshot of price
  quantity       INT NOT NULL,
  line_subtotal  DECIMAL(10,2) NOT NULL,
  CONSTRAINT fk_oi_order
    FOREIGN KEY (order_id) REFERENCES `order`(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_oi_menu_item
    FOREIGN KEY (menu_item_id) REFERENCES menu_item(id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_oi_order
  ON order_item (order_id);

-- Add-ons applied to an order line (snapshots)
CREATE TABLE order_item_addon (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_item_id BIGINT NOT NULL,
  addon_id      BIGINT NULL,
  addon_name    VARCHAR(160) NOT NULL,           -- snapshot of the name
  price_delta   DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  CONSTRAINT fk_oia_oi
    FOREIGN KEY (order_item_id) REFERENCES order_item(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_oia_addon
    FOREIGN KEY (addon_id) REFERENCES addon(id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_oia_oi
  ON order_item_addon (order_item_id);
