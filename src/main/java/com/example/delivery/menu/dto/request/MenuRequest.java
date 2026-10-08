package com.example.delivery.menu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class MenuRequest {

    @Getter
    public static class Create {
        @NotBlank(message = "메뉴 이름을 입력해주세요.")
        private String menuName;

        @NotNull(message = "가격을 입력해주세요.")
        @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
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
        // 수정은 보낸 값만 바뀌므로 null 은 허용하고, 값이 있으면 등록과 같은 규칙을 적용한다
        @Pattern(regexp = ".*\\S.*", message = "메뉴 이름은 비어 있을 수 없습니다.")
        private String menuName;

        @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
        private Integer price;

        private String description;
    }
}
