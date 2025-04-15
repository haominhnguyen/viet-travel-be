package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryDTO;
import com.fpt.capstone.tourism.dto.common.ServiceCategoryFullDTO;
import com.fpt.capstone.tourism.dto.request.ServiceCategoryRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceCategoryResponseDTO;

import java.util.List;

public interface ServiceCategoryService {
    GeneralResponse<ServiceCategoryFullDTO> getCategoryById(Long id);

    GeneralResponse<PagingDTO<List<ServiceCategoryFullDTO>>> getAllCategories(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection);

    GeneralResponse<ServiceCategoryResponseDTO> createCategory(ServiceCategoryRequestDTO requestDTO);

    GeneralResponse<ServiceCategoryResponseDTO> updateCategory(Long id, ServiceCategoryRequestDTO requestDTO);

    GeneralResponse<ServiceCategoryDTO> changeCategoryDeletedStatus(Long id, boolean isDeleted);

    GeneralResponse<List<ServiceCategoryDTO>> getAllServiceCategories();
}

