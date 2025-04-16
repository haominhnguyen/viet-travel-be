package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.common.TourBookingWithDetailDTO;
import com.fpt.capstone.tourism.dto.request.GeneratePlanRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;

import java.util.List;

public interface PlanService {

    GeneralResponse<?> getLocations();
    GeneralResponse<?> getLocations(String name);


    String buildServiceProviderContext(Long locationId);

    String buildCustomerPreferContext(GeneratePlanRequestDTO dto);


    GeneralResponse<?> generatePlan(GeneratePlanRequestDTO dto);

    GeneralResponse<?> getPlanById(Long planId);

    GeneralResponse<?> deletePlanById(Long planId);

    GeneralResponse<?> getPlansByUserId(Long userId);

    GeneralResponse<PagingDTO<List<PlanDTO>>> getPlans(int page, int size, String sortField, String sortDirection, Long userId);
}
