package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;

import java.util.List;

public interface TourDiscountService {
    GeneralResponse<ServiceByCategoryDTO> getServiceDetail(Long tourId, Long serviceId);
    GeneralResponse<ServiceProviderServicesDTO> getServiceProviderServices(Long providerId, Long locationId);
    GeneralResponse<ServiceByCategoryDTO> updateServiceDetail(Long tourId, Long serviceId, ServiceUpdateRequestDTO request);

    GeneralResponse<TourServiceListDTO> getTourServicesList(Long tourId, Integer paxCount);

    GeneralResponse<ServiceProviderOptionsDTO> getServiceProviderOptions(Long locationId, String categoryName);

    GeneralResponse<ServiceByCategoryDTO> createServiceDetail(Long tourId, ServiceCreateRequestDTO request);

    GeneralResponse<Void> changeServiceStatus(Long tourId, Long serviceId, Boolean delete);

    GeneralResponse<List<Integer>> getDayNumbersByServiceAndTour(Long tourId, Long serviceId);

    GeneralResponse<ServiceProviderServicesDTO> getServicesByProviderAndCategory(Long providerId, String categoryName, Long locationId);

    GeneralResponse<Void> removeServiceFromTour(Long tourId, Long serviceId, Integer dayNumber);

    GeneralResponse<ServiceByCategoryDTO> getServiceDetailByDayAndService(Long tourId, Integer dayNumber, Long serviceId);

    GeneralResponse<ServiceProviderOptionsDTO> getTicketProviders();

    GeneralResponse<ServiceProviderServicesDTO> getServicesByTicketProvider(Long providerId);
}
