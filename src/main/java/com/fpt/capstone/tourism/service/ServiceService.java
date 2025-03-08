package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.ServiceBaseDTO;
import com.fpt.capstone.tourism.dto.common.ServiceDTO;
import com.fpt.capstone.tourism.dto.common.ServiceFullDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;

import java.util.List;

public interface ServiceService {
    GeneralResponse<PagingDTO<List<ServiceBaseDTO>>> getAllServices(int page, int size, String keyword,
                                                                    Boolean isDeleted, String sortField,
                                                                    String sortDirection, Long providerId);

    GeneralResponse<ServiceFullDTO> getServiceById(Long id, Long providerId);
}

