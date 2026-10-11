package com.example.delivery.menu.infrastructure;

import static com.example.delivery.menu.domain.QMenu.menu;

import com.example.delivery.global.exception.GlobalException.NotFound;
import com.example.delivery.menu.dto.request.MenuRequest;
import com.example.delivery.menu.dto.response.MenuResponse;
import com.example.delivery.menu.dto.response.QMenuResponse_Detail;
import com.example.delivery.menu.dto.response.QMenuResponse_Summary;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 메뉴 조회 전용. 엔티티를 거치지 않고 QueryDSL 로 화면용 DTO 를 바로 만든다.
 * 삭제된 메뉴는 모든 조회에서 제외한다.
 */
@Repository
@RequiredArgsConstructor
public class MenuQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<MenuResponse.Summary> search(MenuRequest.Search cond, Pageable pageable) {
        List<MenuResponse.Summary> content = queryFactory
                .select(new QMenuResponse_Summary(
                        menu.id, menu.menuName, menu.price, menu.description))
                .from(menu)
                .where(
                        menu.deleted.isFalse(),
                        nameContains(cond.getKeyword()),
                        priceGoe(cond.getMinPrice()),
                        priceLoe(cond.getMaxPrice()))
                .orderBy(menu.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(menu.count())
                .from(menu)
                .where(
                        menu.deleted.isFalse(),
                        nameContains(cond.getKeyword()),
                        priceGoe(cond.getMinPrice()),
                        priceLoe(cond.getMaxPrice()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    // 단건 상세 조회. 없거나 삭제된 메뉴면 404
    public MenuResponse.Detail getDetail(Long menuId) {
        MenuResponse.Detail detail = queryFactory
                .select(new QMenuResponse_Detail(
                        menu.id, menu.menuName, menu.price, menu.description,
                        menu.createdAt, menu.updatedAt))
                .from(menu)
                .where(menu.id.eq(menuId), menu.deleted.isFalse())
                .fetchOne();

        if (detail == null) {
            throw new NotFound("존재하는 메뉴가 없습니다.");
        }
        return detail;
    }

    // where에 null이 들어가면 그 조건은 무시된다 (선택 조건)
    private BooleanExpression nameContains(String keyword) {
        return StringUtils.hasText(keyword) ? menu.menuName.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression priceGoe(Integer minPrice) {
        return minPrice != null ? menu.price.goe(minPrice) : null;
    }

    private BooleanExpression priceLoe(Integer maxPrice) {
        return maxPrice != null ? menu.price.loe(maxPrice) : null;
    }
}
