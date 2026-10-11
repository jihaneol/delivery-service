package com.example.delivery.order.infrastructure;

import static com.example.delivery.order.domain.QOrder.order;

import com.example.delivery.order.dto.request.OrderRequest;
import com.example.delivery.order.dto.response.OrderResponse;
import com.example.delivery.order.dto.response.QOrderResponse_Summary;
import com.example.delivery.order.domain.OrderState;
import com.example.delivery.user.domain.Role;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

/**
 * 주문 조회 전용 (Query). 엔티티를 거치지 않고 QueryDSL 로 화면용 DTO 를 바로 만든다.
 * 상태를 바꾸는 쪽(Command)은 OrderRepository 를 쓴다.
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;

    // 사장님은 받은 주문, 고객은 본인이 한 주문 (상태 조건 + 페이징)
    public Page<OrderResponse.Summary> findAllByUser(
            Long userId, Role role, OrderRequest.Search cond, Pageable pageable) {

        List<OrderResponse.Summary> content = queryFactory
                .select(new QOrderResponse_Summary(
                        order.id, order.totalPrice, order.totalQuantity,
                        order.address, order.orderState, order.createdAt))
                .from(order)
                .where(userEq(userId, role), statesIn(cond.getStates()))
                .orderBy(order.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .where(userEq(userId, role), statesIn(cond.getStates()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // 사장님은 받은 주문, 고객은 본인이 한 주문
    private BooleanExpression userEq(Long userId, Role role) {
        return role == Role.OWNER ? order.owner.id.eq(userId) : order.customer.id.eq(userId);
    }

    // where에 null이 들어가면 그 조건은 무시된다 (선택 조건)
    private BooleanExpression statesIn(List<OrderState> states) {
        return CollectionUtils.isEmpty(states) ? null : order.orderState.in(states);
    }
}
