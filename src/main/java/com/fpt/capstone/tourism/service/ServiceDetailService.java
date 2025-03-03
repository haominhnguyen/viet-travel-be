package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.ServiceDetailRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailResponseDTO;

import java.util.List;

public interface ServiceDetailService {
    GeneralResponse<ServiceDetailResponseDTO> createServiceDetail(Long serviceId, Long providerId, ServiceDetailRequestDTO requestDTO);
    GeneralResponse<List<ServiceDetailResponseDTO>> getAllServiceDetails(Long serviceId, Long providerId);
    GeneralResponse<ServiceDetailResponseDTO> getServiceDetailById(Long serviceId, Long detailId, Long providerId);
    GeneralResponse<ServiceDetailResponseDTO> updateServiceDetail(Long serviceId, Long detailId, Long providerId, ServiceDetailRequestDTO requestDTO);
    GeneralResponse<ServiceDetailResponseDTO> changeServiceDetailStatus(Long serviceId, Long detailId, Boolean isDeleted,Long providerId);
}
