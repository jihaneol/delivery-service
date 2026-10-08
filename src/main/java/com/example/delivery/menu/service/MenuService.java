package com.example.delivery.menu.service;

import com.example.delivery.global.exception.GlobalException.Forbidden;
import com.example.delivery.menu.dto.request.MenuRequest;
import com.example.delivery.menu.dto.response.MenuResponse;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.delivery.global.exception.GlobalException.NotFound;

@Service
@Transactional
@RequiredArgsConstructor
public class MenuService {
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    public MenuResponse.Create createMenu(String loginId, MenuRequest.Create createRequest) {

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFound("사용자를 찾을 수 없습니다."));

        Menu menu = Menu.create(
                createRequest.getMenuName(),
                createRequest.getPrice(),
                createRequest.getDescription(),
                user);

        menuRepository.save(menu);

        return new MenuResponse.Create(menu.getId());
    }

    public MenuResponse.Update updateMenu(Long menuId, String loginId, MenuRequest.Update updateRequest) {
        Menu menu = menuRepository.findByIdAndDeletedFalse(menuId)
                .orElseThrow(() -> new NotFound("존재하는 메뉴가 없습니다."));

        if (!menu.getUser().getLoginId().equals(loginId)) {
            throw new Forbidden("사장님이 등록한 메뉴만 수정이 가능합니다.");
        }

        menu.update(updateRequest.getMenuName(), updateRequest.getPrice(), updateRequest.getDescription());

        return new MenuResponse.Update(menu.getId());
    }

    @Transactional(readOnly = true)
    public Page<MenuResponse.Summary> search(MenuRequest.Search condition, Pageable pageable) {
        return menuRepository.search(condition, pageable);
    }

    @Transactional(readOnly = true)
    public MenuResponse.Detail findDetail(Long menuId) {
        return menuRepository.findDetailById(menuId)
                .orElseThrow(() -> new NotFound("존재하는 메뉴가 없습니다."));
    }

    public void deleteMenu(String loginId, Long menuId) {

        Menu menu = menuRepository.findByIdAndDeletedFalse(menuId)
                .orElseThrow(() -> new NotFound("존재하는 메뉴가 없습니다."));

        if (!menu.getUser().getLoginId().equals(loginId)) {
            throw new Forbidden("사장님이 등록한 메뉴만 삭제가 가능합니다.");
        }

        menu.deleted();
    }
}
