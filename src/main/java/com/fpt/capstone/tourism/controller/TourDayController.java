package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDayFullDTO;
import com.fpt.capstone.tourism.dto.request.TourDayCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateRequestDTO;
import com.fpt.capstone.tourism.service.TourDayServiceI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour/{tourId}/tour-days")
public class TourDayController {
    private final TourDayServiceI tourDayServiceI;

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<List<TourDayFullDTO>>> getTourDaysByTourId(
            @PathVariable Long tourId,
            @RequestParam(required = false) Boolean isDeleted) {
        return ResponseEntity.ok(tourDayServiceI.getTourDayDetail(tourId, isDeleted));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<GeneralResponse<TourDayFullDTO>> getTourDayById(
//            @PathVariable Long tourId,
//            @PathVariable Long id) {
//        return ResponseEntity.ok(tourDayServiceI.getTourDayById(id, tourId));
//    }

    @PostMapping("create")
    public ResponseEntity<GeneralResponse<TourDayFullDTO>> createTourDay(
            @PathVariable Long tourId,
            @RequestBody TourDayCreateRequestDTO request) {
        return ResponseEntity.ok(tourDayServiceI.createTourDay(tourId, request));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GeneralResponse<TourDayFullDTO>> updateTourDay(
            @PathVariable Long tourId,
            @PathVariable Long id,
            @RequestBody TourDayUpdateRequestDTO request) {
        return ResponseEntity.ok(tourDayServiceI.updateTourDay(id, tourId, request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<GeneralResponse<String>> changeTourDayStatus(
            @PathVariable Long tourId,
            @PathVariable Long id,
            @RequestParam Boolean isDeleted) {
        return ResponseEntity.ok(tourDayServiceI.changeTourDayStatus(id, tourId, isDeleted));
    }

}
