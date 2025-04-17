package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
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

    GeneralResponse<?> getListLocationAndServiceCategory(Long tourScheduleId);

    GeneralResponse<Map<Long, String>> getListServiceProviderByLocationIdAndServiceCategoryId(Long locationId, Long serviceCategoryId);

    GeneralResponse<List<ServiceSimpleDTO>> getListServiceByServiceProviderId(Long serviceProviderId, Long serviceCategoryId);

    GeneralResponse<?> getServiceDetail(Long serviceId);

    GeneralResponse<?> addService(AddServiceRequestDTO requestDTO);

    GeneralResponse<?> sendMailToProvider(MailServiceDTO mailServiceDTO);

    GeneralResponse<?> getListServiceRequest(int page, int size);

    GeneralResponse<?> getChangeServiceRequestDetail(Long tourBookingServiceId);

    GeneralResponse<?> rejectServiceRequest(Long tourBookingServiceId);

    GeneralResponse<?> approveServiceRequest(Long tourBookingServiceId);

    GeneralResponse<?> getTourSummary(Long scheduleId);

    GeneralResponse<?> previewMail(PreviewMailDTO previewMailDTO);

    GeneralResponse<?> getListBookingForAddService(Long scheduleId);

    GeneralResponse<?> cancelService(Long tourBookingServiceId);

    GeneralResponse<?> updateServiceQuantity(ServiceQuantityUpdateDTO requestDTO);

    GeneralResponse<?> sendAccountant(Long tourScheduleId);

    GeneralResponse<PagingDTO<List<OperatorTourDTO>>> getListTourPrivate(int page, int size, String keyword, String status, String orderDate);

    GeneralResponse<?> getListTourDayOfSchedule(Long scheduleId);

//    GeneralResponse<?> getListServiceRequest(int page, int size);
}
