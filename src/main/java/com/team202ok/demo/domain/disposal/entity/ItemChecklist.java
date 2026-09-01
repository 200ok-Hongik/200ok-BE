package com.team202ok.demo.domain.disposal.entity;

import com.team202ok.demo.global.entity.BaseCreatedAtEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "item_checklists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemChecklist extends BaseCreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trash_category_id", nullable = false)
    private Long trashCategoryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trash_category_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_item_checklists_category"))
    private TrashCategory trashCategory;

    @Column(name = "check_item_name", nullable = false, length = 100)
    private String checkItemName;

    @Column(name = "guide_message", nullable = false, length = 255)
    private String guideMessage;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "trigger_value", length = 100)
    private String triggerValue; // null이면 무조건 노출, 값이 있으면 해당 상태값일 때만 노출

    @Builder
    private ItemChecklist(Long trashCategoryId, String checkItemName, String guideMessage,
                          Integer displayOrder, String triggerValue) {
        this.trashCategoryId = trashCategoryId;
        this.checkItemName = checkItemName;
        this.guideMessage = guideMessage;
        this.displayOrder = displayOrder;
        this.triggerValue = triggerValue;
    }

    public boolean isApplicable(String statusValue) {
        return this.triggerValue == null || this.triggerValue.equals(statusValue);
    }
}
