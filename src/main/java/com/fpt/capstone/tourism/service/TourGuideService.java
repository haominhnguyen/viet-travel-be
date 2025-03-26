package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.TourGuideRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourGuideResponseDTO;
import com.fpt.capstone.tourism.dto.response.UserFullInformationResponseDTO;

import java.util.List;

public interface TourGuideService {
    GeneralResponse<?> getById(Long id);
    GeneralResponse<?> create(TourGuideRequestDTO userDTO);
    GeneralResponse<?> update(Long id, TourGuideRequestDTO userDTO);
    GeneralResponse<?> delete(Long id, boolean isDeleted);
    GeneralResponse<PagingDTO<List<TourGuideResponseDTO>>> getAll(int page, int size, String keyword, Boolean isDeleted, String roleName, String sortField, String sortDirection);
}
