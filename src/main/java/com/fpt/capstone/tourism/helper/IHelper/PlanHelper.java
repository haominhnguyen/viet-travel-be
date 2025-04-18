package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PlanSaleResponseDTO;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Plan;
import com.fpt.capstone.tourism.model.enums.PlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PlanHelper {
    Specification<Location> searchLocationByName(String name);

    Specification<Plan> buildSearchSpecification(Long userId);

    Specification<Plan> buildSearchSpecification(PlanStatus planStatus, String keyword);

    GeneralResponse<PagingDTO<List<PlanDTO>>> buildPagedResponse(Page<Plan> planPage);


    GeneralResponse<PagingDTO<List<PlanSaleResponseDTO>>> buildPagedPlanSaleResponse(Page<Plan> planPage);
}
