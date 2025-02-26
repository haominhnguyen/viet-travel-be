package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;

import java.util.List;

public interface ServiceCategoryService {
    GeneralResponse<ServiceCategoryDTO> createCategory(ServiceCategoryDTO requestDTO);
    GeneralResponse<ServiceCategoryDTO> getCategoryById(Long id);
    GeneralResponse<PagingDTO<List<ServiceCategoryDTO>>> getAllCategories(int page, int size, String keyword,Boolean isDeleted, String sortField, String sortDirection);
    GeneralResponse<ServiceCategoryDTO> updateCategory(Long id, ServiceCategoryDTO requestDTO);
    GeneralResponse<ServiceCategoryDTO> changeCategoryDeletedStatus(Long id, boolean isDeleted);
}
