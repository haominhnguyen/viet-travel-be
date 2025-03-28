package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.ActivityCategoryDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;

import java.util.List;

public interface ActivityCategoryService {
    GeneralResponse<List<ActivityCategoryDTO>> getAllActivityCategories();
}
