package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface TourScheduleHelper {
    Specification<TourSchedule> buildTourScheduleSearchSpecification(String keyword, List<TourScheduleStatus> tourScheduleStatus);

    GeneralResponse<?> buildPublicTourSchedulePagedResponse(Page<TourSchedule> tourSchedulePage);
}
