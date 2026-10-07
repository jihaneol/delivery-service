package com.example.delivery.menu.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class MenuResponse {
    @Getter
    @AllArgsConstructor
    public static class Create{
       private Long menuId;
    }

    @Getter
    @AllArgsConstructor
    public static class Update {
        private Long menuId;
    }

    // 목록 조회용 (QueryDSL 어노테이션 기반 프로젝션 -> QMenuResponse_Summary 생성)
    @Getter
    public static class Summary {
        private final Long menuId;
        private final String menuName;
        private final Integer price;
        private final String description;

        @QueryProjection
        public Summary(Long menuId, String menuName, Integer price, String description) {
            this.menuId = menuId;
            this.menuName = menuName;
            this.price = price;
            this.description = description;
        }
    }

    // 단건 조회용 (QMenuResponse_Detail 생성)
    @Getter
    public static class Detail {
        private final Long menuId;
        private final String menuName;
        private final Integer price;
        private final String description;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;

        @QueryProjection
        public Detail(Long menuId, String menuName, Integer price, String description,
                      LocalDateTime createdAt, LocalDateTime updatedAt) {
            this.menuId = menuId;
            this.menuName = menuName;
            this.price = price;
            this.description = description;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
}
