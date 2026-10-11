package com.example.delivery.menu.infrastructure;

import com.example.delivery.global.exception.GlobalException.NotFound;
import com.example.delivery.menu.domain.Menu;
import java.util.Collection;
import java.util.List;
import com.example.delivery.menu.domain.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// MenuRepository 의 JPA 구현. 삭제된 메뉴를 "없는 메뉴"로 취급하는 규칙이 여기 있다
@Repository
@RequiredArgsConstructor
class MenuRepositoryImpl implements MenuRepository {

    private final MenuJpaRepository menuJpaRepository;

    @Override
    public Menu save(Menu menu) {
        return menuJpaRepository.save(menu);
    }

    // 수정/삭제용 엔티티 조회. 없거나 삭제된 메뉴면 404
    @Override
    public Menu getActive(Long menuId) {
        return menuJpaRepository.findByIdAndDeletedFalse(menuId)
                .orElseThrow(() -> new NotFound("존재하는 메뉴가 없습니다."));
    }

    // 주문용 엔티티 조회 (사장님까지 함께). 없거나 삭제된 메뉴가 하나라도 있으면 404
    @Override
    public List<Menu> getActiveMenus(Collection<Long> menuIds) {
        List<Menu> menus = menuJpaRepository.findAllByIdIn(menuIds);
        if (menus.size() != menuIds.size() || menus.stream().anyMatch(Menu::isDeleted)) {
            throw new NotFound("존재하지 않거나 삭제된 메뉴가 있습니다.");
        }
        return menus;
    }
}
