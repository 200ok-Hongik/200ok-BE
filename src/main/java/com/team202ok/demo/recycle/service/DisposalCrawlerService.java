//package com.team202ok.demo.recycle.service;
//
//import com.team202ok.demo.recycle.entity.Rule;
//import com.team202ok.demo.recycle.repository.RagRecycleRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.springframework.stereotype.Service;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.nio.charset.StandardCharsets;
//import java.util.UUID;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class DisposalCrawlerService {
//
//    private final RagRecycleRepository ragRecycleRepository;
//
//    @Transactional
//    public Rule crawlPetBottle() {
//        return crawlDisposalRule("페트병", "546");
//    }
//
//    @Transactional
//    public Rule crawlDisposalRule(String itemName, String niIdx) {
//        String searchItemName = normalizeItemName(itemName);
//        String url = buildDisposalUrl(searchItemName, niIdx);
//
//        try {
//            Document doc = Jsoup.connect(url)
//                    .userAgent("Mozilla/5.0")
//                    .timeout(10000)
//                    .get();
//
//            if (niIdx == null || niIdx.isBlank()) {
//                Element detailLink = doc.selectFirst("a[href*=niIdx]");
//                if (detailLink != null) {
//                    String detailUrl = detailLink.absUrl("href");
//                    if (!detailUrl.isBlank()) {
//                        url = detailUrl;
//                        doc = Jsoup.connect(url)
//                                .userAgent("Mozilla/5.0")
//                                .timeout(10000)
//                                .get();
//                    }
//                }
//            }
//
//            String text = doc.body().text();
//            String sourceUrl = url;
//
//            log.info("========== 크롤링 시작 ==========");
//            log.info("Title : {}", doc.title());
//            log.info("Body Length : {}", text.length());
//            log.info("Preview : {}", text.substring(0, Math.min(text.length(), 1000)));
//
//            String crawledItemName = firstNotBlank(
//                    extractBetween(text, "자세한 배출방법을 알려드립니다.", "폐기물 분류체계"),
//                    searchItemName
//            );
//            String category = firstNotBlank(extractCategory(text, crawledItemName), "생활폐기물");
//            String similarItems = extractBetween(text, "유사품목", "배출방법");
//            String disposalMethod = extractBetweenAfter(text, "유사품목", "배출방법", "특징");
//            String caution = extractBetween(text, "특징", "※ 배출방법은 지역별");
//
//            log.info("===== 추출 결과 =====");
//            log.info("품목명 : {}", crawledItemName);
//            log.info("분류 : {}", category);
//            log.info("유사품목 : {}", similarItems);
//            log.info("배출방법 : {}", disposalMethod);
//            log.info("주의사항 : {}", caution);
//
//            validateCrawledRule(searchItemName, disposalMethod);
//
//            log.info("========== DB 저장 시작 ==========");
//
//            Rule rule = ragRecycleRepository.findBySourceUrl(sourceUrl)
//                    .or(() -> ragRecycleRepository.findByItemName(crawledItemName))
//                    .map(existing -> {
//                        existing.update(crawledItemName, category, disposalMethod, caution, similarItems, sourceUrl);
//                        return existing;
//                    })
//                    .orElseGet(() -> Rule.builder()
//                            .ruleId(UUID.randomUUID().toString())
//                            .itemName(crawledItemName)
//                            .category(category)
//                            .similarItems(similarItems)
//                            .disposalMethod(disposalMethod)
//                            .caution(caution)
//                            .sourceUrl(sourceUrl)
//                            .build());
//
//            Rule saved = ragRecycleRepository.save(rule);
//
//            // 실제 DB에 즉시 반영
//            ragRecycleRepository.flush();
//
//            log.info("저장된 PK = {}", saved.getId());
//            log.info("저장된 RuleId = {}", saved.getRuleId());
//            log.info("현재 Rule 개수 = {}", ragRecycleRepository.count());
//
//            log.info("========== DB 저장 완료 ==========");
//            return saved;
//
//        } catch (Exception e) {
//            log.error("크롤링 실패", e);
//            throw new RuntimeException("크롤링 실패", e);
//        }
//    }
//
//    private String buildDisposalUrl(String itemName, String niIdx) {
//        UriComponentsBuilder builder = UriComponentsBuilder
//                .fromUriString("https://xn--oy2b29bd3a601b.kr/front/search/searchDispose.do")
//                .queryParam("searchWrd", itemName)
//                .queryParam("rgnCd", "")
//                .queryParam("admdstCd", "");
//
//        if (niIdx != null && !niIdx.isBlank()) {
//            builder.queryParam("niIdx", niIdx.trim());
//        }
//
//        return builder.encode(StandardCharsets.UTF_8).toUriString();
//    }
//
//    private String normalizeItemName(String itemName) {
//        if (itemName == null || itemName.isBlank()) {
//            throw new IllegalArgumentException("itemName은 필수입니다.");
//        }
//
//        for (String token : itemName.split(",")) {
//            String trimmed = token.trim();
//            if (containsKorean(trimmed)) {
//                return trimmed;
//            }
//        }
//
//        return switch (itemName.trim().toLowerCase()) {
//            case "plastic_bottle", "pet_bottle" -> "페트병";
//            case "battery" -> "건전지";
//            default -> itemName.trim();
//        };
//    }
//
//    private boolean containsKorean(String value) {
//        return value != null && value.matches(".*[가-힣].*");
//    }
//
//    private void validateCrawledRule(String searchItemName, String disposalMethod) {
//        if (disposalMethod == null || disposalMethod.isBlank()) {
//            throw new IllegalStateException("배출방법을 찾지 못해 저장하지 않습니다. itemName=" + searchItemName);
//        }
//    }
//
//    private String extractBetween(String text, String start, String end) {
//
//        int s = text.indexOf(start);
//        int e = text.indexOf(end, s + start.length());
//
//        log.info("'{}' 위치 = {}, '{}' 위치 = {}", start, s, end, e);
//
//        if (s == -1 || e == -1 || s >= e) {
//            return "";
//        }
//
//        return text.substring(s + start.length(), e)
//                .replaceAll("\\s+", " ")
//                .trim();
//    }
//
//    private String extractBetweenAfter(String text, String after, String start, String end) {
//
//        int base = text.indexOf(after);
//
//        if (base == -1) {
//            return "";
//        }
//
//        int s = text.indexOf(start, base);
//        int e = text.indexOf(end, s + start.length());
//
//        log.info("after='{}', '{}' 위치 = {}, '{}' 위치 = {}", after, start, s, end, e);
//
//        if (s == -1 || e == -1 || s >= e) {
//            return "";
//        }
//
//        return text.substring(s + start.length(), e)
//                .replaceAll("\\s+", " ")
//                .trim();
//    }
//
//    private String extractCategory(String text, String itemName) {
//        String categoryAndItem = extractBetweenAfter(
//                text,
//                "자세한 배출방법을 알려드립니다.",
//                "폐기물 분류체계",
//                "유사품목"
//        );
//
//        if (categoryAndItem.isBlank()) {
//            return "";
//        }
//
//        if (itemName != null && !itemName.isBlank()) {
//            return categoryAndItem.replace(itemName, "")
//                    .replaceAll("\\s+", " ")
//                    .trim();
//        }
//
//        return categoryAndItem;
//    }
//
//    private String firstNotBlank(String value, String fallback) {
//        if (value == null || value.isBlank()) {
//            return fallback;
//        }
//        return value;
//    }
//}
