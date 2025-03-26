package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;

import java.util.List;

public interface ActivityService {
    List<ActivityDTO> findRecommendedActivities(int numberActivity);

    GeneralResponse<ActivityDTO> saveActivity(ActivityDTO activityDTO);

    GeneralResponse<ActivityDTO> getActivityById(Long id);

    GeneralResponse<PagingDTO<List<ActivityDTO>>> getAllActivity(int page, int size, String keyword, Boolean isDeleted, String orderDate, Long categoryId);

    GeneralResponse<ActivityDTO> updateActivity(Long id, ActivityDTO activityDTO);

    GeneralResponse<ActivityDTO> deleteActivity(Long id, boolean isDeleted);

    List<ActivityDTO> findRelatedActivities(Long activityId, int numberActivity);

    //List activity for head of business
    GeneralResponse<List<ActivityListDTO>> getActivityList(Long tourId);
    GeneralResponse<ActivityDetailDTO> getActivityDetail(Long tourId, Long activityId);

    GeneralResponse<List<ActivityBasicDTO>> getActivitiesByLocationAndCategory(Long locationId, Long categoryId);
    GeneralResponse<ActivityDetailDTO> createActivity(Long tourId, ActivityCreateUpdateRequestDTO request);
    GeneralResponse<ActivityDetailDTO> updateActivity(Long tourId, Long activityId, ActivityCreateUpdateRequestDTO request);
}
