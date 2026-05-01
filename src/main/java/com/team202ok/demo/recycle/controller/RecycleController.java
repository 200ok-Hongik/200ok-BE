package com.team202ok.demo.recycle.controller;

import com.team202ok.demo.recycle.dto.RecycleRuleReq;
import com.team202ok.demo.recycle.dto.RecycleRuleRes;
import com.team202ok.demo.recycle.service.RecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RecycleController {

    private final RecycleService recycleService;

    @PostMapping("/recycle")
    public ResponseEntity<RecycleRuleRes> check(@RequestBody RecycleRuleReq request) {
        return ResponseEntity.ok(recycleService.check(request));
    }
}