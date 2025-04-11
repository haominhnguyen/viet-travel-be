package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.GeneratePlanRequestDTO;

public interface PlanService {

    GeneralResponse<?> getLocations();
    GeneralResponse<?> getLocations(String name);


    String buildServiceProviderContext(Long locationId);

    String buildCustomerPreferContext(GeneratePlanRequestDTO dto);


    GeneralResponse<?> generatePlan(GeneratePlanRequestDTO dto);

    GeneralResponse<?> getPlanById(Long planId);
}
