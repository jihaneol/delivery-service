package com.example.delivery.menu.controller;

import com.example.delivery.menu.dto.request.MenuRequest;
import com.example.delivery.menu.dto.response.MenuResponse;
import com.example.delivery.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // 메뉴 등록 - 사장님
    @PostMapping
    public ResponseEntity<MenuResponse.Create> createMenu(
            @AuthenticationPrincipal String loginId,
            @Valid @RequestBody MenuRequest.Create createRequest
    ) {
        var response = menuService.createMenu(loginId, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 메뉴 수정  - 사장님
    @PutMapping("/{id}")
    public ResponseEntity<MenuResponse.Update> updateMenu(
            @AuthenticationPrincipal String loginId,
            @PathVariable("id") Long menuId,
            @Valid @RequestBody MenuRequest.Update updateRequest) {
        var response = menuService.updateMenu(menuId, loginId, updateRequest);

        return ResponseEntity.ok(response);
    }

    // 메뉴 삭제 - 사장님 (소프트 딜리트)
    @DeleteMapping("/{id}")
    public void deleteMenu(
            @AuthenticationPrincipal String loginId,
            @PathVariable("id") Long menuId
    ) {
        menuService.deleteMenu(loginId, menuId);
    }

    // 메뉴 목록 조회
    @GetMapping
    public PagedModel<MenuResponse.Summary> findAllMenu(MenuRequest.Search condition, Pageable pageable) {
        return new PagedModel<>(menuService.search(condition, pageable));
    }

    // 메뉴 단건 조회
    @GetMapping("/{id}")
    public MenuResponse.Detail findMenu(@PathVariable("id") Long menuId) {
        return menuService.findDetail(menuId);
    }
}
