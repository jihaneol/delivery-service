package com.example.delivery.menu.repository;

import com.example.delivery.menu.dto.request.MenuRequest;
import com.example.delivery.menu.dto.response.MenuResponse;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// 조회 전용 (QueryDSL)
public interface MenuRepositoryCustom {
    Page<MenuResponse.Summary> search(MenuRequest.Search condition, Pageable pageable);

    Optional<MenuResponse.Detail> findDetailById(Long menuId);
}
