//package com.team202ok.demo.recycle.controller;
//
//import com.team202ok.demo.recycle.dto.RecycleRuleReq;
//import com.team202ok.demo.recycle.dto.RecycleRuleRes;
//import com.team202ok.demo.recycle.service.RecycleService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api")
//@CrossOrigin(origins = "*")
//public class RecycleController {
//
//    private final RecycleService recycleService;
//
//    @PostMapping("/recycle")
//    public ResponseEntity<RecycleRuleRes> check(@RequestBody RecycleRuleReq request) {
//        log.info("[POST /api/recycle] request: {}", request);
//
//        RecycleRuleRes response = recycleService.check(request);
//
//        log.info("[POST /api/recycle] response: {}", response);
//        return ResponseEntity.ok(response);
//    }
//
//
//}