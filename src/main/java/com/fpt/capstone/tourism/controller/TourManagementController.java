package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.TourDayRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourDayUpdateDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.TourDayService;
import com.fpt.capstone.tourism.service.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour")
public class TourManagementController {
    private final TourService tourService;
    private final TourDayService tourDayService;

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<TourSimpleDTO>>>> getAllTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) Boolean isOpened,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        Pageable pageable = PageRequest.of(page, size,
                sortDirection.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());
        return ResponseEntity.ok(tourService.getAllTours(keyword, isDeleted, isOpened,pageable));
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<GeneralResponse<TourDetailDTO>> getTourById(@PathVariable Long id) {
        return ResponseEntity.ok(tourService.getTourDetail(id));
    }

    @GetMapping("/{tourId}/tour-days")
    public ResponseEntity<GeneralResponse<List<TourDayFullDTO>>> getTourDaysByTourId(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourDayService.getTourDayDetail(tourId));
    }

    @PutMapping("/{tourId}/tour-days/update/{tourDayId}")
    public ResponseEntity<GeneralResponse<TourDayFullDTO>> updateTourDayDetail(
            @PathVariable Long tourDayId,
            @RequestBody TourDayUpdateDTO tourDayUpdateDTO) {
        return ResponseEntity.ok(tourDayService.updateTourDayDetail(tourDayId, tourDayUpdateDTO));
    }

}
