package com.example.delivery.menu.repository;

import com.example.delivery.menu.entity.Menu;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long>, MenuRepositoryCustom {
    // 수정/삭제용 (엔티티가 필요한 경우)
    Optional<Menu> findByIdAndDeletedFalse(Long menuId);

    @EntityGraph(attributePaths = "user")
    List<Menu> findAllByIdIn(Collection<Long> ids);
}
