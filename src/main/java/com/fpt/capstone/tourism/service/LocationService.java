package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.request.LocationRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;

import java.util.List;

public interface LocationService {
    GeneralResponse<LocationDTO> saveLocation(LocationRequestDTO locationRequestDTO);

    GeneralResponse<LocationDTO> getLocationById(Long id);

    GeneralResponse<PagingDTO<List<LocationDTO>>> getAllLocation(int page, int size, String keyword, Boolean isDeleted, String orderDate);

    GeneralResponse<LocationDTO> deleteLocation(Long id, boolean isDeleted);

    GeneralResponse<LocationDTO> updateLocation(Long id, LocationRequestDTO locationRequestDTO);

    List<PublicLocationDTO> findRecommendedLocations(int numberLocation);
    List<PublicLocationDTO> findRecommendedLocations(int numberLocation, Long locationId);

    GeneralResponse<PagingDTO<List<LocationDTO>>> getLocationsByTourId(Long tourId, int page, int size, String keyword, Boolean isDeleted, String orderDate);

    GeneralResponse<?> getListLocation();
}
