package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PlanHelper {
    Specification<Location> searchLocationByName(String name);

    Specification<Plan> buildSearchSpecification(Long userId);

    GeneralResponse<PagingDTO<List<PlanDTO>>> buildPagedResponse(Page<Plan> tourBookingPage);
}
