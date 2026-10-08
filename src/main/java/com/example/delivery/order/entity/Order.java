package com.example.delivery.order.entity;

import com.example.delivery.global.entity.BaseEntity;
import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "orders")
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customer_state", columnList = "customer_id, order_state, id"),
        @Index(name = "idx_orders_owner_state", columnList = "owner_id, order_state, id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderMenu> orderMenus = new ArrayList<>();

    private Integer totalPrice;

    private Integer totalQuantity;

    private String address;

    @Enumerated(EnumType.STRING)
    private OrderState orderState;

    public static Order create(User customer, User owner, String address) {
        Order order = new Order();
        order.customer = customer;
        order.owner = owner;
        order.address = address;
        order.orderState = OrderState.PENDING_PAYMENT;
        order.totalPrice = 0;
        order.totalQuantity = 0;
        return order;
    }

    // 메뉴를 담으면서 총액과 총 수량도 같이 계산한다
    public void addOrderMenu(Menu menu, int quantity) {
        orderMenus.add(OrderMenu.create(this, menu, quantity));
        totalPrice += menu.getPrice() * quantity;
        totalQuantity += quantity;
    }

    // 주문요청(결제 전) 상태에서만 취소할 수 있다
    public void cancel() {
        if (orderState != OrderState.PENDING_PAYMENT) {
            throw new GlobalException.Conflict("주문요청 상태의 주문만 취소할 수 있습니다.");
        }
        orderState = OrderState.CANCELED;
    }

    // 주문요청(결제 전) 상태에서만 결제할 수 있다
    public void pay() {
        if (orderState != OrderState.PENDING_PAYMENT) {
            throw new GlobalException.Conflict("주문요청 상태의 주문만 결제할 수 있습니다.");
        }
        orderState = OrderState.PAID;
    }

    // 결제완료 -> 주문수락, 주문수락 -> 배달완료 두 가지만 허용한다
    public void changeState(OrderState next) {
        boolean allowedAccepte = (orderState == OrderState.PAID && next == OrderState.ACCEPTED);
        boolean allowedDelivered = (orderState == OrderState.ACCEPTED && next == OrderState.DELIVERED);

        if(allowedAccepte || allowedDelivered) {
            this.orderState = next;
            return;
        }

        throw new GlobalException.Conflict("허용되지 않는 상태 변경입니다.");
    }
}
