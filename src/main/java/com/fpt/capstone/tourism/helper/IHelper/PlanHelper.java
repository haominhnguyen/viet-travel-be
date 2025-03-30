package com.fpt.capstone.tourism.helper.IHelper;

import com.fpt.capstone.tourism.model.Location;
import org.springframework.data.jpa.domain.Specification;

public interface PlanHelper {
    Specification<Location> searchLocationByName(String name);
}
