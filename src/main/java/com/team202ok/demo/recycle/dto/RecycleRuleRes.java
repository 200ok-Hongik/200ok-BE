//package com.team202ok.demo.recycle.dto;
//
//import com.team202ok.demo.recycle.domain.RecycleRule;
//import com.team202ok.demo.recycle.entity.Rule;
//import lombok.Getter;
//
//import java.util.List;
//
//@Getter
//public class RecycleRuleRes {
//    private String verdict;
//    private String requiredAction;
//    private String disposalMethod;
//    private String basis;
//    private String matchedRuleId;
//    private List<String> actionCodes;
//    private List<String> actions;
//    private String finalMessage;
//    private String sourceUrl;
//
//    public static RecycleRuleRes from(RecycleRule rule) {
//        RecycleRuleRes res = new RecycleRuleRes();
//        res.verdict = rule.getVerdict();
//        res.requiredAction = rule.getRequiredAction();
//        res.disposalMethod = rule.getDisposalMethod();
//        res.basis = rule.getBasis();
//        res.matchedRuleId = rule.getRuleId();
//        return res;
//    }
//
//    public static RecycleRuleRes empty() {
//        return new RecycleRuleRes();
//    }
//
//    public static RecycleRuleRes fromActions(String verdict,
//                                             List<String> actionCodes,
//                                             List<String> actions,
//                                             String requiredAction,
//                                             String disposalMethod,
//                                             String finalMessage,
//                                             String matchedRuleId,
//                                             Rule basisRule) {
//        RecycleRuleRes res = new RecycleRuleRes();
//        res.verdict = verdict;
//        res.actionCodes = actionCodes;
//        res.actions = actions;
//        res.requiredAction = requiredAction;
//        res.disposalMethod = disposalMethod;
//        res.finalMessage = finalMessage;
//        res.matchedRuleId = matchedRuleId;
//
//        if (basisRule != null) {
//            res.basis = buildBasis(basisRule);
//            res.sourceUrl = basisRule.getSourceUrl();
//        }
//
//        return res;
//    }
//
//    private static String buildBasis(Rule rule) {
//        StringBuilder basis = new StringBuilder();
//
//        if (rule.getDisposalMethod() != null && !rule.getDisposalMethod().isBlank()) {
//            basis.append(rule.getDisposalMethod());
//        }
//
//        if (rule.getCaution() != null && !rule.getCaution().isBlank()) {
//            if (!basis.isEmpty()) {
//                basis.append(" ");
//            }
//            basis.append(rule.getCaution());
//        }
//
//        return basis.toString();
//    }
//}
