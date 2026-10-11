package com.example.delivery.menu.infrastructure;

import com.example.delivery.global.exception.GlobalException;
import com.example.delivery.menu.domain.Menu;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

// "삭제된 메뉴는 없는 메뉴로 본다"는 조회 규칙이 서비스에서 이 클래스로 옮겨와서, 규칙을 여기서 검증한다
@ExtendWith(MockitoExtension.class)
class MenuRepositoryImplTest {

    @Mock MenuJpaRepository menuJpaRepository;
    @InjectMocks MenuRepositoryImpl menuRepository;

    private Menu menu(Long id) {
        Menu menu = Menu.create("메뉴" + id, 1000, null, null);
        ReflectionTestUtils.setField(menu, "id", id);
        return menu;
    }

    @Test
    void 모두_있는_메뉴면_그대로_돌려준다() {
        given(menuJpaRepository.findAllByIdIn(Set.of(1L, 2L))).willReturn(List.of(menu(1L), menu(2L)));

        assertThat(menuRepository.getActiveMenus(Set.of(1L, 2L))).hasSize(2);
    }

    @Test
    void 없는_메뉴가_섞여_있으면_404() {
        given(menuJpaRepository.findAllByIdIn(Set.of(1L, 2L))).willReturn(List.of(menu(1L)));

        assertThatThrownBy(() -> menuRepository.getActiveMenus(Set.of(1L, 2L)))
                .isInstanceOf(GlobalException.NotFound.class);
    }

    @Test
    void 삭제된_메뉴가_섞여_있으면_404() {
        Menu deleted = menu(2L);
        deleted.deleted();
        given(menuJpaRepository.findAllByIdIn(Set.of(1L, 2L))).willReturn(List.of(menu(1L), deleted));

        assertThatThrownBy(() -> menuRepository.getActiveMenus(Set.of(1L, 2L)))
                .isInstanceOf(GlobalException.NotFound.class);
    }

    @Test
    void 없거나_삭제된_메뉴를_수정_삭제용으로_가져오면_404() {
        given(menuJpaRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> menuRepository.getActive(1L))
                .isInstanceOf(GlobalException.NotFound.class);
    }
}
