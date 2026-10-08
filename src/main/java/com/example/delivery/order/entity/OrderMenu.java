package com.example.delivery.order.entity;

import com.example.delivery.global.entity.BaseEntity;
import com.example.delivery.menu.entity.Menu;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderMenu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id",
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Order order;

    private Integer quantity;

    // 주문 시점의 메뉴 단가 (이후 메뉴 가격이 바뀌어도 주문 기록은 그대로)
    private Integer price;

    static OrderMenu create(Order order, Menu menu, int quantity) {
        OrderMenu orderMenu = new OrderMenu();
        orderMenu.order = order;
        orderMenu.menu = menu;
        orderMenu.quantity = quantity;
        orderMenu.price = menu.getPrice();
        return orderMenu;
    }
}
