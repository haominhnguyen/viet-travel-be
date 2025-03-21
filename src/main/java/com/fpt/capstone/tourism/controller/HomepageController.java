package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.repository.LocationRepository;
import com.fpt.capstone.tourism.service.HomepageService;
import com.fpt.capstone.tourism.service.LocationService;
import com.fpt.capstone.tourism.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public")
public class HomepageController {
    private final HomepageService homepageService;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @GetMapping("/homepage")
    public ResponseEntity<GeneralResponse<HomepageDTO>> view(@RequestParam(value = "numberTour", defaultValue = "3") int numberTour,
                                                             @RequestParam(value = "numberBlog", defaultValue = "3") int numberBlog,
                                                             @RequestParam(value = "numberActivity", defaultValue = "3") int numberActivity,
                                                             @RequestParam(value = "numberLocation", defaultValue = "7") int numberLocation) {
        return ResponseEntity.ok(homepageService.viewHomepage(numberTour, numberBlog, numberActivity, numberLocation));
    }

    @GetMapping("/list-tour")
    public ResponseEntity<GeneralResponse<Map<String, Object>>> viewAllTour(@RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @RequestParam(required = false) String keyword,
                                                                            @RequestParam(value = "budgetTo", required = false) Double budgetTo,
                                                                            @RequestParam(value = "budgetFrom", required = false) Double budgetFrom,
                                                                            @RequestParam(value = "duration", required = false) Integer duration,
                                                                            @RequestParam(value = "fromDate", required = false)
                                                                            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
                                                                            @RequestParam(value = "departLocationId", required = false) Long departLocationId) {

        GeneralResponse<PagingDTO<List<PublicTourDTO>>> tourResponse = homepageService.viewAllTour(page, size, keyword, budgetFrom, budgetTo, duration, fromDate, departLocationId);
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("tours", tourResponse.getData());
        List<PublicLocationDTO> locations = locationRepository.findAll().stream().map(locationMapper::toPublicLocationDTO).collect(Collectors.toList());
        responseData.put("locations", locations);
        return ResponseEntity.ok(new GeneralResponse<>(HttpStatus.OK.value(), "Success", responseData));
    }

    @GetMapping("/list-hotel")
    public ResponseEntity<GeneralResponse<PagingDTO<List<PublicServiceProviderDTO>>>> viewAllHotel(@RequestParam(defaultValue = "0") int page,
                                                                                                   @RequestParam(defaultValue = "10") int size,
                                                                                                   @RequestParam(required = false) String keyword,
                                                                                                   @RequestParam(value = "star", required = false) Integer star,
                                                                                                   @RequestParam(value = "budgetTo", required = false) Double budgetTo,
                                                                                                   @RequestParam(value = "budgetFrom", required = false) Double budgetFrom
    ) {
        return ResponseEntity.ok(homepageService.viewAllHotel(page, size, keyword, star));
    }

    @GetMapping("/tour-detail/{id}")
    public ResponseEntity<GeneralResponse<PublicTourDetailDTO>> viewTourDetail(@PathVariable Long id) {
        return ResponseEntity.ok(homepageService.viewTourDetail(id));
    }

    @GetMapping("/location-detail/{id}")
    public ResponseEntity<GeneralResponse<PublicLocationDetailDTO>> viewLocationDetail(@PathVariable Long id) {
        return ResponseEntity.ok(homepageService.viewPublicLocationDetail(id));
    }

    @GetMapping("/hotel-detail/{id}")
    public ResponseEntity<GeneralResponse<PublicHotelDetailDTO>> viewHotelDetail(@PathVariable Long id) {
        return ResponseEntity.ok(homepageService.viewPublicHotelDetail(id));
    }


}
