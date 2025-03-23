package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceProviderServicesDTO;
import com.fpt.capstone.tourism.dto.common.TourServiceListDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;

public interface TourDiscountService {
    GeneralResponse<ServiceDetailDTO> getServiceDetail(Long tourId, Long serviceId);
    GeneralResponse<ServiceProviderServicesDTO> getServiceProviderServices(Long providerId, Long locationId);
    GeneralResponse<ServiceDetailDTO> updateServiceDetail(Long tourId, Long serviceId, ServiceUpdateRequestDTO request);

    GeneralResponse<TourServiceListDTO> getTourServicesList(Long tourId, Integer paxCount);
}
