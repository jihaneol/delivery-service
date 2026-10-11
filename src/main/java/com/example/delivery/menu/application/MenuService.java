package com.example.delivery.menu.application;

import com.example.delivery.global.exception.GlobalException.Forbidden;
import com.example.delivery.menu.dto.request.MenuRequest;
import com.example.delivery.menu.dto.response.MenuResponse;
import com.example.delivery.menu.domain.Menu;
import com.example.delivery.menu.domain.MenuRepository;
import com.example.delivery.user.domain.User;
import com.example.delivery.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메뉴 변경 - 등록, 수정, 삭제. 조회는 MenuQueryService 가 맡는다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    public MenuResponse.Create createMenu(String loginId, MenuRequest.Create createRequest) {

        User user = userRepository.getByLoginId(loginId);

        Menu menu = Menu.create(
                createRequest.getMenuName(),
                createRequest.getPrice(),
                createRequest.getDescription(),
                user);

        menuRepository.save(menu);

        return new MenuResponse.Create(menu.getId());
    }

    public MenuResponse.Update updateMenu(Long menuId, String loginId, MenuRequest.Update updateRequest) {
        Menu menu = menuRepository.getActive(menuId);

        if (!menu.getUser().getLoginId().equals(loginId)) {
            throw new Forbidden("사장님이 등록한 메뉴만 수정이 가능합니다.");
        }

        menu.update(updateRequest.getMenuName(), updateRequest.getPrice(), updateRequest.getDescription());

        return new MenuResponse.Update(menu.getId());
    }

    public void deleteMenu(String loginId, Long menuId) {

        Menu menu = menuRepository.getActive(menuId);

        if (!menu.getUser().getLoginId().equals(loginId)) {
            throw new Forbidden("사장님이 등록한 메뉴만 삭제가 가능합니다.");
        }

        menu.deleted();
    }
}
