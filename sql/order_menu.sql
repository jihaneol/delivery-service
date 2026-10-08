-- order_menu (주문메뉴) : 주문에 담긴 메뉴와 수량
CREATE TABLE order_menu (
    id         BIGSERIAL PRIMARY KEY,
    menu_id    BIGINT    NOT NULL REFERENCES menu (id),
    order_id   BIGINT    NOT NULL,  -- 의도적으로 FK 제약 없음 (NO_CONSTRAINT)
    quantity   INTEGER,
    price      INTEGER,             -- 주문 시점의 메뉴 단가
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 이미 order_menu 테이블이 있는 경우 (price 추가 + BaseEntity 상속으로 추가된 컬럼)
-- ALTER TABLE order_menu ADD COLUMN price INTEGER;
-- UPDATE order_menu om SET price = m.price FROM menu m WHERE om.menu_id = m.id;
-- ALTER TABLE order_menu ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();
-- ALTER TABLE order_menu ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT now();
