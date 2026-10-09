-- orders (주문) : 사장님별로 독립된 주문 1건
CREATE TABLE orders (
    id             BIGSERIAL    PRIMARY KEY,
    customer_id    BIGINT       NOT NULL,  -- users.id (FK 제약 없음)
    owner_id       BIGINT       NOT NULL,  -- users.id (FK 제약 없음)
    total_price    INTEGER,
    total_quantity INTEGER,
    address        VARCHAR(255),
    order_state    VARCHAR(255),  -- PENDING_PAYMENT, PAID, ACCEPTED, DELIVERED, CANCELED
    reordered      BOOLEAN      NOT NULL DEFAULT false,  -- 취소된 주문으로 재주문했는지 (1회만 허용)
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

-- 이미 orders 테이블이 있는 경우 (BaseEntity 상속으로 추가된 컬럼)
-- ALTER TABLE orders ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
-- ALTER TABLE orders ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();

-- 이미 orders 테이블이 있는 경우 (재주문 여부 컬럼 추가) - 애플리케이션 실행 전에 먼저 실행한다
-- ALTER TABLE orders ADD COLUMN reordered BOOLEAN NOT NULL DEFAULT false;

-- 주문 목록 조회용 인덱스 (내 주문 + 상태 + 최신순)
CREATE INDEX idx_orders_customer_state ON orders (customer_id, order_state, id DESC);
CREATE INDEX idx_orders_owner_state    ON orders (owner_id, order_state, id DESC);

-- 이미 FK 가 만들어진 DB 라면 제거한다 (제약 이름은 DB 마다 달라서 먼저 조회)
-- SELECT conname FROM pg_constraint WHERE contype = 'f' AND conrelid = 'orders'::regclass;
-- ALTER TABLE orders DROP CONSTRAINT {조회한 customer_id 제약 이름};
-- ALTER TABLE orders DROP CONSTRAINT {조회한 owner_id 제약 이름};
