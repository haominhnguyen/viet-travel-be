package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.Tour;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public interface TourHelper {
    Specification<Tour> buildTourPublicSearchSpecification(String keyword, Boolean isDeleted, Boolean isOpened);
    GeneralResponse<PagingDTO<List<TourDTO>>> buildPublicTourPagedResponse(Page<Tour> tourPage, List<TourDTO> tourDTOs);
}
