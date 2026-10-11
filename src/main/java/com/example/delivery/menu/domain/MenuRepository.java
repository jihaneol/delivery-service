package com.example.delivery.menu.domain;

import java.util.Collection;
import java.util.List;

/**
 * 메뉴 저장소 (변경용). 삭제된 메뉴는 "없는 메뉴"로 취급한다.
 * 화면용 조회는 infrastructure.MenuQueryRepository 가 맡는다.
 */
public interface MenuRepository {

    Menu save(Menu menu);

    // 수정/삭제용. 없거나 삭제된 메뉴면 404(NotFound)
    Menu getActive(Long menuId);

    // 주문용 (사장님까지 함께). 없거나 삭제된 메뉴가 하나라도 있으면 404(NotFound)
    List<Menu> getActiveMenus(Collection<Long> menuIds);
}
