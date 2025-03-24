package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;

public interface TourDiscountService {
    GeneralResponse<ServiceByCategoryDTO> getServiceDetail(Long tourId, Long serviceId);
    GeneralResponse<ServiceProviderServicesDTO> getServiceProviderServices(Long providerId, Long locationId);
    GeneralResponse<ServiceByCategoryDTO> updateServiceDetail(Long tourId, Long serviceId, ServiceUpdateRequestDTO request);

    GeneralResponse<TourServiceListDTO> getTourServicesList(Long tourId, Integer paxCount);

    GeneralResponse<ServiceProviderOptionsDTO> getServiceProviderOptions(Long locationId, String categoryName);
}
