package com.team202ok.demo.domain.rule.entity;

import com.team202ok.demo.global.converter.JsonMapConverter;
import com.team202ok.demo.domain.region.entity.Region;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rule")
public class RecycleRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ruleId;

    private String itemCode;

    @Column(name = "region_id")
    private Long regionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_rule_region"))
    private Region region;

    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> conditions;

    private String verdict;

    private String requiredAction;

    private String disposalMethod;

    private String basis;

    private int priority;

    public void update(String itemCode,
                       Long regionId,
                       Map<String, Object> conditions,
                       String verdict,
                       String requiredAction,
                       String disposalMethod,
                       String basis,
                       int priority) {
        this.itemCode = itemCode;
        this.regionId = regionId;
        this.conditions = conditions;
        this.verdict = verdict;
        this.requiredAction = requiredAction;
        this.disposalMethod = disposalMethod;
        this.basis = basis;
        this.priority = priority;
    }

    public boolean matches(Map<String, Object> inputConditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        return conditions.entrySet().stream()
                .allMatch(e -> matchesValue(e.getValue(), inputConditions.get(e.getKey())));
    }

    private boolean matchesValue(Object expected, Object actual) {
        if (expected instanceof Boolean expectedBoolean && actual instanceof String actualString) {
            return expectedBoolean == Boolean.parseBoolean(actualString);
        }

        return expected.equals(actual);
    }
}
