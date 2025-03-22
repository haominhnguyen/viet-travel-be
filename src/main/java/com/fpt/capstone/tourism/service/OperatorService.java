package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.AddServiceRequestDTO;
import com.fpt.capstone.tourism.dto.request.AssignTourGuideRequestDTO;
import com.fpt.capstone.tourism.dto.request.PayServiceRequestDTO;
import com.fpt.capstone.tourism.dto.request.TourOperationLogRequestDTO;
import com.fpt.capstone.tourism.dto.response.*;

import java.util.List;
import java.util.Map;

public interface OperatorService {
    GeneralResponse<PagingDTO<List<OperatorTourDTO>>> getListTour(int page, int size, String keyword, String status, String orderDate);

    GeneralResponse<OperatorTourDTO> operateTour(Long id);

    GeneralResponse<OperatorTourDetailDTO> getTourDetail(Long scheduleId);

    GeneralResponse<List<OperatorTourCustomerDTO>> getListCustomerOfTourDetail(Long scheduleId);

    GeneralResponse<List<OperatorTourBookingDTO>> getListBookingOfTourDetail(Long scheduleId);

    GeneralResponse<List<TourOperationLogDTO>> getListOperationLogOfTourDetail(Long scheduleId);

    GeneralResponse<TourOperationLogDTO> createOperationLog(Long scheduleId, TourOperationLogRequestDTO logRequestDTO);

    GeneralResponse<TourOperationLogDTO> deleteOperationLog(Long logId);

    GeneralResponse<AssignTourGuideRequestDTO> assignTourGuide(Long scheduleId, AssignTourGuideRequestDTO requestDTO);

    GeneralResponse<List<UserResponseDTO>> getListAvailableTourGuide(Long scheduleId);

    GeneralResponse<List<OperatorTransactionDTO>> getListTransaction(Long scheduleId);

    GeneralResponse<OperatorServiceListDTO> getListService(Long scheduleId);

    GeneralResponse<PublicServiceProviderDTO> chooseServiceToPay(Long serviceId);

    GeneralResponse<OperatorTransactionDTO> payService(PayServiceRequestDTO requestDTO);

    GeneralResponse<Map<Long, String>> getListLocation();

    GeneralResponse<Map<Long, String>> getListServiceProviderByLocationId(Long locationId);

    GeneralResponse<List<ServiceSimpleDTO>> getListServiceByServiceProviderId(Long serviceProviderId);

    GeneralResponse<?> getServiceDetail(Long serviceId);

    GeneralResponse<?> addService(AddServiceRequestDTO requestDTO);
}
