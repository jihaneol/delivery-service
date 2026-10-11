package com.example.delivery.order.domain;

import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.menu.domain.Menu;
import com.example.delivery.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private Order order(OrderState state) {
        Order order = Order.create(User.builder().build(), User.builder().build(), "서울");
        ReflectionTestUtils.setField(order, "orderState", state);
        return order;
    }

    @Test
    void 메뉴를_담으면_총액과_총수량이_계산된다() {
        Order order = order(OrderState.PENDING_PAYMENT);

        order.addOrderMenu(Menu.create("짜장면", 7000, "맛있음", null), 2);
        order.addOrderMenu(Menu.create("탕수육", 15000, "바삭", null), 1);

        assertThat(order.getTotalPrice()).isEqualTo(29000);
        assertThat(order.getTotalQuantity()).isEqualTo(3);
        assertThat(order.getOrderMenus()).hasSize(2);
    }

    @Test
    void 고객은_결제_전과_결제_완료_주문을_취소할_수_있다() {
        Order pending = order(OrderState.PENDING_PAYMENT);
        Order paid = order(OrderState.PAID);

        pending.cancelByCustomer();
        paid.cancelByCustomer();

        assertThat(pending.getOrderState()).isEqualTo(OrderState.CANCELED);
        assertThat(paid.getOrderState()).isEqualTo(OrderState.CANCELED);
    }

    @Test
    void 고객은_수락됐거나_배달됐거나_이미_취소된_주문을_취소할_수_없다() {
        assertThatThrownBy(order(OrderState.ACCEPTED)::cancelByCustomer).isInstanceOf(GlobalException.Conflict.class);
        assertThatThrownBy(order(OrderState.DELIVERED)::cancelByCustomer).isInstanceOf(GlobalException.Conflict.class);
        assertThatThrownBy(order(OrderState.CANCELED)::cancelByCustomer).isInstanceOf(GlobalException.Conflict.class);
    }

    @Test
    void 사장님은_결제_완료와_주문_수락_주문을_취소할_수_있다() {
        Order paid = order(OrderState.PAID);
        Order accepted = order(OrderState.ACCEPTED);

        paid.cancelByOwner();
        accepted.cancelByOwner();

        assertThat(paid.getOrderState()).isEqualTo(OrderState.CANCELED);
        assertThat(accepted.getOrderState()).isEqualTo(OrderState.CANCELED);
    }

    @Test
    void 사장님은_결제_전이나_배달된_주문을_취소할_수_없다() {
        assertThatThrownBy(order(OrderState.PENDING_PAYMENT)::cancelByOwner).isInstanceOf(GlobalException.Conflict.class);
        assertThatThrownBy(order(OrderState.DELIVERED)::cancelByOwner).isInstanceOf(GlobalException.Conflict.class);
    }

    @Test
    void 취소된_주문만_한_번_재주문할_수_있다() {
        Order canceled = order(OrderState.CANCELED);

        canceled.markReordered();

        assertThat(canceled.isReordered()).isTrue();
        assertThatThrownBy(canceled::markReordered).isInstanceOf(GlobalException.Conflict.class);
        assertThatThrownBy(order(OrderState.PAID)::markReordered).isInstanceOf(GlobalException.Conflict.class);
    }

    @Test
    void 결제_전_주문은_결제되면_결제완료가_된다() {
        Order order = order(OrderState.PENDING_PAYMENT);

        order.pay();

        assertThat(order.getOrderState()).isEqualTo(OrderState.PAID);
    }

    @Test
    void 이미_결제됐거나_취소된_주문은_결제할_수_없다() {
        assertThatThrownBy(order(OrderState.PAID)::pay).isInstanceOf(GlobalException.Conflict.class);
        assertThatThrownBy(order(OrderState.CANCELED)::pay).isInstanceOf(GlobalException.Conflict.class);
    }

    @Test
    void 결제완료에서_주문수락_주문수락에서_배달완료로_변경된다() {
        Order order = order(OrderState.PAID);

        order.changeState(OrderState.ACCEPTED);
        assertThat(order.getOrderState()).isEqualTo(OrderState.ACCEPTED);

        order.changeState(OrderState.DELIVERED);
        assertThat(order.getOrderState()).isEqualTo(OrderState.DELIVERED);
    }

    @Test
    void 허용되지_않은_상태_변경은_실패한다() {
        assertThatThrownBy(() -> order(OrderState.PENDING_PAYMENT).changeState(OrderState.ACCEPTED))
                .isInstanceOf(GlobalException.Conflict.class);
        assertThatThrownBy(() -> order(OrderState.PAID).changeState(OrderState.DELIVERED))
                .isInstanceOf(GlobalException.Conflict.class);
    }
}
