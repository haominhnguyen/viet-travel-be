package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.OperatorTourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;

import java.util.List;

public interface OperatorService {
    GeneralResponse<PagingDTO<List<OperatorTourDTO>>> getListTour(int page, int size, String keyword, String status, String orderDate);
}
