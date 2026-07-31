package com.team202ok.demo.recycle.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "rag_rule")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ruleId;

//    @Column(nullable = false)
    private String itemName;

    private String category;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String disposalMethod;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String caution;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String similarItems;

    private String sourceUrl;

    @Builder
    public Rule(String ruleId,
                String itemName,
                String category,
                String disposalMethod,
                String caution,
                String similarItems,
                String sourceUrl) {

        this.ruleId = ruleId;
        this.itemName = itemName;
        this.category = category;
        this.disposalMethod = disposalMethod;
        this.caution = caution;
        this.similarItems = similarItems;
        this.sourceUrl = sourceUrl;
    }

    public void update(String itemName,
                       String category,
                       String disposalMethod,
                       String caution,
                       String similarItems,
                       String sourceUrl) {
        this.itemName = itemName;
        this.category = category;
        this.disposalMethod = disposalMethod;
        this.caution = caution;
        this.similarItems = similarItems;
        this.sourceUrl = sourceUrl;
    }
}
