CREATE DATABASE orderbook;
USE orderbook;
CREATE TABLE orderbook (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    market VARCHAR(20),
    timestamp BIGINT,
    total_ask_size DOUBLE,
    total_bid_size DOUBLE,
    level DOUBLE
);

CREATE TABLE orderbook_unit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    orderbook_id BIGINT,
    ask_price DOUBLE,
    bid_price DOUBLE,
    ask_size DOUBLE,
    bid_size DOUBLE,
    level DOUBLE,
    position INT,
    FOREIGN KEY (orderbook_id) REFERENCES orderbook(id)
);

업비트 JSON(?) 형식 기준
