package com.team202ok.demo.recycle.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "disposal_guides")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DisposalGuide extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trash_category_id", nullable = false)
    private Long trashCategoryId;

    @Column(name = "guide_message", nullable = false, columnDefinition = "TEXT")
    private String guideMessage;

    @Column(name = "caution_message", length = 255)
    private String cautionMessage;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Builder
    private DisposalGuide(Long trashCategoryId, String guideMessage,
                          String cautionMessage, Boolean isActive) {
        this.trashCategoryId = trashCategoryId;
        this.guideMessage = guideMessage;
        this.cautionMessage = cautionMessage;
        this.isActive = isActive != null ? isActive : true;
    }
}