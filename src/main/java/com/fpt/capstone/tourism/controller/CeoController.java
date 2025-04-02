package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourProcessDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.service.LocationService;
import com.fpt.capstone.tourism.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/ceo")
@RequiredArgsConstructor
public class CeoController {
    private final TourService tourService;
    private final LocationService locationService;

    @GetMapping("/process-tour/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<TourProcessDTO>>>> getAllTourNeedToProcess(@RequestParam(defaultValue = "0") int page,
                                                                                                    @RequestParam(defaultValue = "10") int size,
                                                                                                    @RequestParam(required = false) String keyword,
                                                                                                    @RequestParam(required = false) TourStatus tourStatus,
                                                                                                    @RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(tourService.getAllTourNeedToProcess(page, size, keyword, tourStatus, orderDate));
    }

    @GetMapping("/process-tour/detail/{tourId}")
    public ResponseEntity<GeneralResponse<?>> getDetailTourNeedToProcess(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourService.getDetailTourNeedToProcess(tourId));
    }

    @GetMapping("/process-tour/detail/{tourId}/tour-day-detail/{tourDayId}")
    public ResponseEntity<GeneralResponse<?>> getDetailTourDay(@PathVariable Long tourId,
                                                               @PathVariable Long tourDayId) {
        return ResponseEntity.ok(tourService.getDetailTourDay(tourId, tourDayId));
    }

    @PutMapping("/process-tour/approve-tour-process/{tourId}")
    public ResponseEntity<GeneralResponse<?>> approveTourProcess(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourService.approveTourProcess(tourId));
    }

    @PutMapping("/process-tour/reject-tour-process/{tourId}")
    public ResponseEntity<GeneralResponse<?>> rejectTourProcess(@PathVariable Long tourId) {
        return ResponseEntity.ok(tourService.rejectTourProcess(tourId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<GeneralResponse<?>> viewDashboard(@RequestParam(value = "fromDate", required = false)
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
                                                            @RequestParam(value = "toDate", required = false)
                                                            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate) {
        return ResponseEntity.ok(tourService.viewDashboard(fromDate, toDate));
    }
}
