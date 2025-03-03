package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.ServiceResponseDTO;

import java.util.List;

public interface ServiceService {
    GeneralResponse<PagingDTO<List<ServiceBaseDTO>>> getAllServices(int page, int size, String keyword,
                                                                    Boolean isDeleted, String sortField,
                                                                    String sortDirection, Long providerId);

    //GeneralResponse<ServiceFullDTO> getServiceById(Long id, Long providerId);
    GeneralResponse<List<TourDayServiceDTO>> getTourDayServicesByServiceId(Long serviceId, Long providerId);
    GeneralResponse<List<ServiceDetailDTO>> getServiceDetailsByServiceId(Long serviceId, Long providerId);
    GeneralResponse<ServiceResponseDTO> createService(ServiceRequestDTO requestDTO, Long providerId);
    GeneralResponse<ServiceResponseDTO> updateService(Long serviceId, ServiceRequestDTO requestDTO, Long providerId);
    GeneralResponse<ServiceResponseDTO> changeServiceStatus(Long serviceId, Boolean isDeleted, Long providerId);
}

