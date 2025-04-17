package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.request.GeneratePlanRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;

import java.util.List;

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


    @GetMapping("/details/{id}")
    public ResponseEntity<?> details(@PathVariable(name = "id") Long planId) {
        return ResponseEntity.ok(planService.getPlanById(planId));
    }


    @DeleteMapping("/delete/{planId}")
    public ResponseEntity<?> delete(@PathVariable(name = "planId") Long planId) {
        return ResponseEntity.ok(planService.deletePlanById(planId));
    }


    @PostMapping("/update/{planId}")
    public ResponseEntity<?> update(@RequestBody String planJson, @PathVariable(name = "planId") Long planId) {
        return ResponseEntity.ok(planService.updatePlan(planJson, planId));
    }



    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<PlanDTO>>>> getPlans(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "35") Long userId,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(planService.getPlans(page, size, sortField, sortDirection, userId));
    }


}
