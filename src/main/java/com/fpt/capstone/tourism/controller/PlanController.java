package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.GeneratePlanRequestDTO;
import com.fpt.capstone.tourism.service.GeminiApiService;
import com.fpt.capstone.tourism.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/plans")
public class PlanController {

    private static final Logger LOG = LoggerFactory.getLogger(PlanController.class);

    private final PlanService planService;


    @PostMapping("/generate")
    public GeneralResponse<?> generate(@RequestBody GeneratePlanRequestDTO dto) {
        return planService.generatePlan(dto);
    }



    @GetMapping("/locations")
    public ResponseEntity<?> locations() {
        return ResponseEntity.ok(planService.getLocations());
    }


    @GetMapping("/locations/all")
    public ResponseEntity<?> locations(@RequestParam(defaultValue = "", required = false) String name) {
        return ResponseEntity.ok(planService.getLocations(name));
    }


}
