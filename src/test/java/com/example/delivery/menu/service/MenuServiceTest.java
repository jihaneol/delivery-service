package com.example.delivery.menu.service;

import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.menu.entity.Menu;
import com.example.delivery.menu.repository.MenuRepository;
import com.example.delivery.user.entity.User;
import com.example.delivery.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock UserRepository userRepository;
    @Mock MenuRepository menuRepository;
    @InjectMocks MenuService menuService;

    private Menu menu(String ownerLoginId) {
        return Menu.create("짜장면", 7000, "맛있음", User.builder().loginId(ownerLoginId).build());
    }

    @Test
    void 본인_메뉴를_삭제하면_삭제됨으로_표시된다() {
        Menu menu = menu("owner1");
        given(menuRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(menu));

        menuService.deleteMenu("owner1", 1L);

        assertThat(menu.isDeleted()).isTrue();
    }

    @Test
    void 다른_사장님의_메뉴는_삭제할_수_없다() {
        given(menuRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(menu("owner1")));

        assertThatThrownBy(() -> menuService.deleteMenu("owner2", 1L))
                .isInstanceOf(GlobalException.Forbidden.class);
    }

    @Test
    void 없거나_삭제된_메뉴는_삭제할_수_없다() {
        given(menuRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.deleteMenu("owner1", 1L))
                .isInstanceOf(GlobalException.NotFound.class);
    }
}
