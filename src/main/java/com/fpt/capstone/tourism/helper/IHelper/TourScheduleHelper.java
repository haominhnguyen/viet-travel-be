package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

public interface TourScheduleHelper {
    Specification<TourSchedule> buildTourScheduleSearchSpecification(String keyword, TourScheduleStatus tourScheduleStatus);

    GeneralResponse<?> buildPublicTourSchedulePagedResponse(Page<TourSchedule> tourSchedulePage);
}
