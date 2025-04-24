package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.request.ActivityGenerateDTO;
import com.fpt.capstone.tourism.dto.request.GeneratePlanRequestDTO;
import com.fpt.capstone.tourism.dto.request.SavePlanRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.BookingService;
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
    private final BookingService bookingService;


    @PostMapping("/generate")
    public GeneralResponse<?> generate(@RequestBody GeneratePlanRequestDTO dto) {
        return planService.generatePlan(dto);
    }


    @PostMapping("/save")
    public GeneralResponse<?> generate(@RequestBody SavePlanRequestDTO planDTO) {
        return planService.savePlan(planDTO);
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

    @GetMapping("/service-providers/list")
    public ResponseEntity<?> getServiceProviders(@RequestParam Long locationId, @RequestParam String categoryName, @RequestParam List<Long> ids) {
        return ResponseEntity.ok(planService.getServiceProviders(locationId, categoryName, ids));
    }

    @PostMapping("/activity/list")
    public ResponseEntity<?> getActivities(@RequestBody ActivityGenerateDTO dto) {
        return ResponseEntity.ok(planService.getActivities(dto));
    }


    @PostMapping("/request-tour-create")
    public ResponseEntity<?> requestTourCreate(@RequestBody Long planId) {
        return ResponseEntity.ok(planService.requestTourCreate(planId));
    }


    @DeleteMapping("/delete/{planId}")
    public ResponseEntity<?> delete(@PathVariable(name = "planId") Long planId) {
        return ResponseEntity.ok(planService.deletePlanById(planId));
    }


    @PostMapping("/update/{planId}")
    public ResponseEntity<?> update(@RequestBody String planJson, @PathVariable(name = "planId") Long planId) {
        return ResponseEntity.ok(planService.updatePlan(planJson, planId));
    }



    @PostMapping("/update-status/{planId}")
    public ResponseEntity<?> updateStatus(@PathVariable(name = "planId") Long planId) {
        return ResponseEntity.ok(planService.updateStatus(planId));
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
