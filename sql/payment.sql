-- payment (결제) : 주문 결제 내역
CREATE TABLE payment (
    id             BIGSERIAL    PRIMARY KEY,
    order_id       BIGINT       NOT NULL,
    amount         INTEGER      NOT NULL,  -- 주문 총액 (서버 계산)
    payment_method VARCHAR(255) NOT NULL,  -- CARD
    payment_state  VARCHAR(255) NOT NULL DEFAULT 'PAID',  -- PAID, CANCELED
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

-- 이미 payment 테이블이 있는 경우 (결제 상태 컬럼 추가) - 애플리케이션 실행 전에 먼저 실행한다
-- ALTER TABLE payment ADD COLUMN payment_state VARCHAR(255) NOT NULL DEFAULT 'PAID';
-- 기존에 취소된 주문의 결제가 있다면 취소로 맞춘다
-- UPDATE payment p SET payment_state = 'CANCELED' FROM orders o WHERE p.order_id = o.id AND o.order_state = 'CANCELED';
