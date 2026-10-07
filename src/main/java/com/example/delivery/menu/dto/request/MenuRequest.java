package com.example.delivery.menu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MenuRequest {

    @Getter
    public static class Create {
        private String menuName;
        private Integer price;
        private String description;
    }

    // 메뉴 목록 조회 조건 (쿼리 파라미터, 모두 선택)
    @Getter
    @AllArgsConstructor
    public static class Search {
        private String keyword;
        private Integer minPrice;
        private Integer maxPrice;
    }

    @Getter
    public static class Update {
        private String menuName;
        private Integer price;
        private String description;
    }
}
