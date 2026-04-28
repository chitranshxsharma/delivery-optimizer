-- =====================================================================
--  SMART DELIVERY ROUTE OPTIMIZER — DATABASE SCHEMA
--  Run this manually ONLY if you want to inspect the structure.
--  Hibernate auto-creates all tables via spring.jpa.hibernate.ddl-auto=update
-- =====================================================================

CREATE DATABASE IF NOT EXISTS delivery_optimizer_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE delivery_optimizer_db;

-- ── Users ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     ENUM('ROLE_ADMIN','ROLE_DISPATCHER','ROLE_DRIVER') NOT NULL DEFAULT 'ROLE_DRIVER'
);

-- ── Delivery Nodes (graph vertices) ───────────────────────────────────
CREATE TABLE IF NOT EXISTS delivery_nodes (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    code      VARCHAR(20)  NOT NULL UNIQUE,   -- e.g. WH-01, HUB-03, DP-07
    name      VARCHAR(200) NOT NULL,
    type      ENUM('WAREHOUSE','HUB','DELIVERY_POINT') NOT NULL,
    latitude  DOUBLE,
    longitude DOUBLE,
    city      VARCHAR(100),
    address   TEXT,
    active    TINYINT(1) DEFAULT 1
);

-- ── Route Edges (graph edges with weights) ────────────────────────────
CREATE TABLE IF NOT EXISTS route_edges (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_node_id       BIGINT NOT NULL,
    destination_node_id  BIGINT NOT NULL,
    distance_km          DOUBLE NOT NULL,
    estimated_minutes    INT    NOT NULL,
    active               TINYINT(1) DEFAULT 1,
    bidirectional        TINYINT(1) DEFAULT 1,
    FOREIGN KEY (source_node_id)      REFERENCES delivery_nodes(id),
    FOREIGN KEY (destination_node_id) REFERENCES delivery_nodes(id)
);

-- ── Orders ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS orders (
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tracking_id                VARCHAR(50)  NOT NULL UNIQUE,
    customer_name              VARCHAR(100) NOT NULL,
    customer_phone             VARCHAR(20)  NOT NULL,
    source_node_id             BIGINT,
    destination_node_id        BIGINT,
    assigned_driver_id         BIGINT,
    status                     ENUM('PENDING','ASSIGNED','DISPATCHED','IN_TRANSIT','DELIVERED','FAILED') DEFAULT 'PENDING',
    optimised_route_path       TEXT,
    total_distance_km          DOUBLE,
    estimated_delivery_minutes INT,
    sla_deadline               DATETIME,
    created_at                 DATETIME,
    dispatched_at              DATETIME,
    delivered_at               DATETIME,
    notes                      TEXT,
    FOREIGN KEY (source_node_id)      REFERENCES delivery_nodes(id),
    FOREIGN KEY (destination_node_id) REFERENCES delivery_nodes(id),
    FOREIGN KEY (assigned_driver_id)  REFERENCES users(id)
);

-- ── Tracking Events (live timeline) ───────────────────────────────────
CREATE TABLE IF NOT EXISTS tracking_events (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    status      ENUM('PENDING','ASSIGNED','DISPATCHED','IN_TRANSIT','DELIVERED','FAILED'),
    description TEXT,
    location    VARCHAR(200),
    event_time  DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- ── Useful indexes ────────────────────────────────────────────────────
CREATE INDEX idx_orders_tracking    ON orders(tracking_id);
CREATE INDEX idx_orders_status      ON orders(status);
CREATE INDEX idx_orders_driver      ON orders(assigned_driver_id);
CREATE INDEX idx_tracking_order     ON tracking_events(order_id);
CREATE INDEX idx_edges_source       ON route_edges(source_node_id);
