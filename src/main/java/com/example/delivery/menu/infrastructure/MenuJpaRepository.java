package com.example.delivery.menu.infrastructure;

import com.example.delivery.menu.domain.Menu;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

// 패키지 밖에서는 보이지 않는다. 같은 패키지의 MenuRepositoryImpl 만 사용한다.
interface MenuJpaRepository extends JpaRepository<Menu, Long> {

    Optional<Menu> findByIdAndDeletedFalse(Long menuId);

    @EntityGraph(attributePaths = "user")
    List<Menu> findAllByIdIn(Collection<Long> ids);
}
