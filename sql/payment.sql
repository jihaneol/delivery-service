-- payment (결제) : 주문 결제 내역
CREATE TABLE payment (
    id             BIGSERIAL    PRIMARY KEY,
    order_id       BIGINT       NOT NULL,
    amount         INTEGER      NOT NULL,  -- 주문 총액 (서버 계산)
    payment_method VARCHAR(255) NOT NULL,  -- CARD
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);
