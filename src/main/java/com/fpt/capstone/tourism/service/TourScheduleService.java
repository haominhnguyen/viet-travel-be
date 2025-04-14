package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.EndDateOption;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.OperatorAvailabilityDTO;
import com.fpt.capstone.tourism.dto.request.TourScheduleRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourScheduleBasicResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourScheduleResponseDTO;
import com.fpt.capstone.tourism.model.User;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

public interface TourScheduleService {

    GeneralResponse<List<EndDateOption>> calculatePossibleEndDates(Long tourId, LocalDateTime startDate);

    GeneralResponse<List<OperatorAvailabilityDTO>> findAvailableOperators(Long tourId, LocalDateTime startDate, LocalDateTime endDate);

    GeneralResponse<TourScheduleBasicResponseDTO> setTourSchedule(@Valid TourScheduleRequestDTO scheduleRequestDTO, User user);

    GeneralResponse<TourScheduleBasicResponseDTO> updateTourSchedule(@Valid TourScheduleRequestDTO scheduleRequestDTO, User user);

    GeneralResponse<Object> cancelTourSchedule(Long scheduleId, User user);


    GeneralResponse<?> getTourScheduleSettlement(int page, int size, String keyword, String sortField, String sortDirection);

    GeneralResponse<?> getSettlementDetails(Long tourScheduleId);

    GeneralResponse<?> finishSettlement(Long tourScheduleId);

    GeneralResponse<?>  getProviderByScheduleId(Long tourScheduleId);
}
