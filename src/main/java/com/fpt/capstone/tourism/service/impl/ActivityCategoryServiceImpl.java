package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.ActivityCategoryDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.ActivityCategory;
import com.fpt.capstone.tourism.repository.ActivityCategoryRepository;
import com.fpt.capstone.tourism.service.ActivityCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
@RequiredArgsConstructor
public class ActivityCategoryServiceImpl implements ActivityCategoryService {
    private final ActivityCategoryRepository activityCategoryRepository;
    @Override
    public GeneralResponse<List<ActivityCategoryDTO>> getAllActivityCategories() {
        try {
            List<ActivityCategory> categories = activityCategoryRepository.findByDeletedFalse();

            List<ActivityCategoryDTO> categoryDTOs = categories.stream()
                    .map(category -> ActivityCategoryDTO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .build())
                    .collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), CATEGORY_LIST_LOAD_SUCCESS, categoryDTOs);
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, CATEGORY_LIST_LOAD_FAIL, ex);
        }
    }
}
