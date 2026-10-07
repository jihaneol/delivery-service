package com.example.delivery.menu.repository;

import static com.example.delivery.menu.entity.QMenu.menu;

import com.example.delivery.menu.dto.request.MenuRequest;
import com.example.delivery.menu.dto.response.MenuResponse;
import com.example.delivery.menu.dto.response.QMenuResponse_Detail;
import com.example.delivery.menu.dto.response.QMenuResponse_Summary;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class MenuRepositoryCustomImpl implements MenuRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
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

    @Override
    public Optional<MenuResponse.Detail> findDetailById(Long menuId) {
        MenuResponse.Detail detail = queryFactory
                .select(new QMenuResponse_Detail(
                        menu.id, menu.menuName, menu.price, menu.description,
                        menu.createdAt, menu.updatedAt))
                .from(menu)
                .where(menu.id.eq(menuId), menu.deleted.isFalse())
                .fetchOne();

        return Optional.ofNullable(detail);
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
