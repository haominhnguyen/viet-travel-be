package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.ServiceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("admin/service-category")
public class ServiceCategoryController {

    private final ServiceCategoryService serviceCategoryService;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<ServiceCategoryDTO>> create(@RequestBody ServiceCategoryDTO requestDTO) {
        return ResponseEntity.ok(serviceCategoryService.createCategory(requestDTO));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<GeneralResponse<ServiceCategoryDTO>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(serviceCategoryService.getCategoryById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GeneralResponse<ServiceCategoryDTO>> update(@PathVariable Long id, @RequestBody ServiceCategoryDTO requestDTO) {
        return ResponseEntity.ok(serviceCategoryService.updateCategory(id, requestDTO));
    }

    @PostMapping("/change-status/{id}")
    public ResponseEntity<GeneralResponse<ServiceCategoryDTO>> changeStatus(@PathVariable Long id, @RequestBody boolean isDeleted) {
        return ResponseEntity.ok(serviceCategoryService.changeCategoryDeletedStatus(id, isDeleted));
    }

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<ServiceCategoryDTO>>>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(serviceCategoryService.getAllCategories(page, size,keyword,isDeleted, sortField, sortDirection));
    }
}
