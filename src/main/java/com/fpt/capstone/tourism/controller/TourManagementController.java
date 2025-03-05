package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.dto.common.TourDetailDTO;
import com.fpt.capstone.tourism.dto.common.TourSimpleDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.service.TourService;
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



}
