package com.example.delivery.order.entity;

import com.example.delivery.global.entity.BaseEntity;
import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User owner;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    private List<OrderMenu> orderMenus = new ArrayList<>();

    private Integer totalPrice;

    private Integer totalQuantity;

    private String address;

    @Enumerated(EnumType.STRING)
    private OrderState orderState;

    // 취소된 이 주문으로 이미 재주문했는지 (재주문은 한 번만 허용)
    @Column(nullable = false)
    private boolean reordered;

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

    // 결제가 끝난 주문인지 (취소 시 결제도 함께 취소해야 하는지 판단)
    public boolean isPaid() {
        return orderState == OrderState.PAID || orderState == OrderState.ACCEPTED;
    }

    // 고객 취소: 사장님이 수락하기 전(결제 전, 결제 완료)까지만 취소할 수 있다
    public void cancelByCustomer() {
        if (orderState != OrderState.PENDING_PAYMENT && orderState != OrderState.PAID) {
            throw new GlobalException.Conflict("결제 전 또는 결제 완료 상태의 주문만 취소할 수 있습니다.");
        }
        orderState = OrderState.CANCELED;
    }

    // 사장님 취소(주문 거절): 결제 완료, 주문 수락 상태에서만 취소할 수 있다
    public void cancelByOwner() {
        if (orderState != OrderState.PAID && orderState != OrderState.ACCEPTED) {
            throw new GlobalException.Conflict("결제 완료 또는 주문 수락 상태의 주문만 취소할 수 있습니다.");
        }
        orderState = OrderState.CANCELED;
    }

    // 취소된 주문만, 한 번만 재주문할 수 있다
    public void markReordered() {
        if (orderState != OrderState.CANCELED) {
            throw new GlobalException.Conflict("취소된 주문만 다시 주문할 수 있습니다.");
        }
        if (reordered) {
            throw new GlobalException.Conflict("이미 다시 주문한 주문입니다.");
        }
        reordered = true;
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
