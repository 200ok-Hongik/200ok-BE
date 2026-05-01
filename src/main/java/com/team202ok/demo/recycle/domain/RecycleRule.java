package com.team202ok.demo.recycle.domain;

import com.team202ok.demo.recycle.converter.JsonMapConverter;
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

    private String item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> conditions;

    private String verdict;

    private String requiredAction;

    private String disposalMethod;

    private String basis;

    private int priority;

    public boolean matches(Map<String, Object> inputConditions) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }
        return conditions.entrySet().stream()
                .allMatch(e -> e.getValue().equals(inputConditions.get(e.getKey())));
    }
}