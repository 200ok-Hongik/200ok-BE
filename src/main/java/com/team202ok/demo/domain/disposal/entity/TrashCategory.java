package com.team202ok.demo.domain.disposal.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "trash_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrashCategory extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Builder
    public TrashCategory(String code, String name) {
        this.code = code;
        this.name = name;
    }
}