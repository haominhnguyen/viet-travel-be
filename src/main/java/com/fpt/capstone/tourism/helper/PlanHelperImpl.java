package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.helper.IHelper.PlanHelper;
import com.fpt.capstone.tourism.model.Location;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class PlanHelperImpl implements PlanHelper {
    @Override
    public Specification<Location> searchLocationByName(String name) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.function("unaccent", String.class, cb.lower(root.get("name"))),
                                "%" + name.toLowerCase() + "%"
                        )
                );
            }
            predicates.add(cb.equal(root.get("deleted"), false));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
