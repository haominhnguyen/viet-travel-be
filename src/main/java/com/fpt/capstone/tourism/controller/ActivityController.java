package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.ActivityDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.request.LocationRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.ActivityService;
import com.fpt.capstone.tourism.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/activity")
public class ActivityController {
    private final ActivityService activityService;


    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<ActivityDTO>> create(@RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.ok(activityService.saveActivity(activityDTO));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<GeneralResponse<ActivityDTO>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivityById(id));
    }

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<ActivityDTO>>>> getAll(@RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size,
                                                                                @RequestParam(required = false) String keyword,
                                                                                @RequestParam(required = false) Boolean isDeleted,
                                                                                @RequestParam(required = false) Long categoryId,
                                                                                @RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(activityService.getAllActivity(page, size, keyword, isDeleted, orderDate, categoryId));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GeneralResponse<ActivityDTO>> update(@PathVariable Long id, @RequestBody ActivityDTO activityDTO) {
        return ResponseEntity.ok(activityService.updateActivity(id, activityDTO));
    }

    @DeleteMapping("/change-status/{id}")
    public ResponseEntity<GeneralResponse<ActivityDTO>> delete(@PathVariable Long id,
                                                               @RequestParam boolean isDeleted) {
        return ResponseEntity.ok(activityService.deleteActivity(id, isDeleted));
    }


}
