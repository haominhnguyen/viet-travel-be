package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.common.TourBookingWithDetailDTO;
import com.fpt.capstone.tourism.dto.request.ActivityGenerateDTO;
import com.fpt.capstone.tourism.dto.request.GeneratePlanRequestDTO;
import com.fpt.capstone.tourism.dto.request.SavePlanRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PlanSaleResponseDTO;
import com.fpt.capstone.tourism.model.enums.PlanStatus;

import java.util.List;

public interface PlanService {

    GeneralResponse<?> getLocations();
    GeneralResponse<?> getLocations(String name);


    String buildServiceProviderContext(Long locationId);

    String buildCustomerPreferContext(GeneratePlanRequestDTO dto);

    String buildActivityPreferences(ActivityGenerateDTO dto);


    GeneralResponse<?> generatePlan(GeneratePlanRequestDTO dto);

    GeneralResponse<?> getPlanById(Long planId);

    GeneralResponse<?> deletePlanById(Long planId);

    GeneralResponse<?> getPlansByUserId(Long userId);

    GeneralResponse<PagingDTO<List<PlanDTO>>> getPlans(int page, int size, String sortField, String sortDirection, Long userId);

    GeneralResponse<PagingDTO<List<PlanSaleResponseDTO>>> getPlans(int page, int size, String sortField, String sortDirection, PlanStatus planStatus, String keyword);

    GeneralResponse<?> updatePlan(String planJson, Long planId);

    GeneralResponse<?> updateStatus(Long planId);

    GeneralResponse<?> updateStatus(Long planId, PlanStatus planStatus);

    GeneralResponse<?> requestTourCreate(Long planId);

    GeneralResponse<?> getServiceProviders(Long locationId, String categoryName, List<Long> ids);

    GeneralResponse<?> getActivities(ActivityGenerateDTO dto);


    GeneralResponse<?> savePlan(SavePlanRequestDTO planDTO);
}
