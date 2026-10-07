package com.example.delivery.menu.entity;

import com.example.delivery.global.entity.BaseEntity;
import com.example.delivery.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Menu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String menuName;

    @Column(nullable = false)
    private Integer price;

    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;


    public static Menu create(String menuName, Integer price, String description, User user) {
        return Menu.builder()
                .menuName(menuName)
                .price(price)
                .description(description)
                .user(user)
                .build();
    }

    public void update(
            String menuName, Integer price, String description
    ) {
        if(menuName != null){
            this.menuName = menuName;
        }
        if(price != null){
            this.price = price;
        }
        if(description != null){
            this.description = description;
        }
    }

    public void deleted() {
        this.deleted = true;
    }
}
