package com.example.delivery.menu.application;

import com.example.delivery.menu.dto.request.MenuRequest;
import com.example.delivery.menu.dto.response.MenuResponse;
import com.example.delivery.menu.infrastructure.MenuQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메뉴 조회. 상태를 바꾸지 않고 DTO 만 돌려준다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MenuQueryService {

    private final MenuQueryRepository menuQueryRepository;

    // 메뉴 목록 - 삭제된 메뉴 제외
    public Page<MenuResponse.Summary> search(MenuRequest.Search condition, Pageable pageable) {
        return menuQueryRepository.search(condition, pageable);
    }

    // 메뉴 단건 - 없거나 삭제된 메뉴면 404
    public MenuResponse.Detail findDetail(Long menuId) {
        return menuQueryRepository.getDetail(menuId);
    }
}
