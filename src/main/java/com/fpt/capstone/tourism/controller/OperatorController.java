package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.OperatorTourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDTO;
import com.fpt.capstone.tourism.service.OperatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/operator")
public class OperatorController {

    private final OperatorService operatorService;
    @GetMapping("/list-tour")
    public ResponseEntity<GeneralResponse<PagingDTO<List<OperatorTourDTO>>>> getListTour(@RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int size,
                                                                                         @RequestParam(required = false) String keyword,
                                                                                         @RequestParam(value = "status", required = false) String status,
                                                                                         @RequestParam(defaultValue = "desc") String orderDate) {
        return ResponseEntity.ok(operatorService.getListTour(page, size, keyword, status, orderDate));
    }
}
