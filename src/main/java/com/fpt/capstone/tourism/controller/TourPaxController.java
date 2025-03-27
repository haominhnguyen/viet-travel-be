package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourPaxDTO;
import com.fpt.capstone.tourism.dto.common.TourPaxFullDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourPaxUpdateRequestDTO;
import com.fpt.capstone.tourism.service.TourPaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour/{tourId}/tour-pax")
public class TourPaxController {
    private final TourPaxService tourPaxService;

    @GetMapping
    public ResponseEntity<GeneralResponse<List<TourPaxFullDTO>>> getTourPaxConfigurations(
            @PathVariable Long tourId) {
        return ResponseEntity.ok(tourPaxService.getTourPaxConfigurations(tourId));
    }

    @GetMapping("/{paxId}")
    public ResponseEntity<GeneralResponse<TourPaxFullDTO>> getTourPaxConfiguration(
            @PathVariable Long tourId,
            @PathVariable Long paxId) {
        return ResponseEntity.ok(tourPaxService.getTourPaxConfiguration(tourId, paxId));
    }

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<TourPaxFullDTO>> createTourPaxConfiguration(
            @PathVariable Long tourId,
            @RequestBody TourPaxCreateRequestDTO request) {
        return ResponseEntity.ok(tourPaxService.createTourPaxConfiguration(tourId, request));
    }

    @PutMapping("/{paxId}")
    public ResponseEntity<GeneralResponse<TourPaxFullDTO>> updateTourPaxConfiguration(
            @PathVariable Long tourId,
            @PathVariable Long paxId,
            @RequestBody TourPaxUpdateRequestDTO request) {
        return ResponseEntity.ok(tourPaxService.updateTourPaxConfiguration(tourId, paxId, request));
    }

    @DeleteMapping("/{paxId}")
    public ResponseEntity<GeneralResponse<String>> deleteTourPaxConfiguration(
            @PathVariable Long tourId,
            @PathVariable Long paxId) {
        return ResponseEntity.ok(tourPaxService.deleteTourPaxConfiguration(tourId, paxId));
    }
}
