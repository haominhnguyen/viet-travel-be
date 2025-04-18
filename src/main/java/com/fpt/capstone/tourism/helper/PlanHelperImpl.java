package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.common.TourBookingWithDetailDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PlanSaleResponseDTO;
import com.fpt.capstone.tourism.helper.IHelper.PlanHelper;
import com.fpt.capstone.tourism.mapper.PlanMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.PlanStatus;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
@RequiredArgsConstructor
public class PlanHelperImpl implements PlanHelper {

    private final PlanMapper planMapper;

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

    @Override
    public Specification<Plan> buildSearchSpecification(Long userId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search in tour name
            if (userId != null) {
                // Tham gia bảng user để truy cập user.id
                Join<Plan, User> userJoin = root.join("user", JoinType.INNER);
                predicates.add(cb.equal(userJoin.get("id"), userId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Specification<Plan> buildSearchSpecification(PlanStatus planStatus, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search in tour name
            if (planStatus != null) {
                predicates.add(
                        cb.equal(root.get("planStatus"), planStatus)
                );
            } else {
                predicates.add(cb.or(
                        cb.equal(root.get("planStatus"), PlanStatus.PENDING),
                        cb.equal(root.get("planStatus"), PlanStatus.SUCCESS),
                        cb.equal(root.get("planStatus"), PlanStatus.CANCELLED)
                ));
            }

            if(keyword != null) {
                Join<Plan, User> userJoin = root.join("user", JoinType.INNER);
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));
                Expression<String> normalizedUserName = cb.function("unaccent", String.class, cb.lower(userJoin.get("fullName")));

                Predicate userFullNamePredicate = cb.like(normalizedUserName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                predicates.add(userFullNamePredicate);

            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public GeneralResponse<PagingDTO<List<PlanDTO>>> buildPagedResponse(Page<Plan> planPage) {

        List<Plan> entities = planPage.getContent();
        List<PlanDTO> dtoList = entities.stream().map(planMapper::toPlanDto).toList();

        PagingDTO<List<PlanDTO>> pagingDTO = PagingDTO.<List<PlanDTO>>builder()
                .page(planPage.getNumber())
                .size(planPage.getSize())
                .total(planPage.getTotalElements())
                .items(planPage.getContent().stream().map(planMapper::toPlanDto).toList())
                .build();
        return new GeneralResponse<>(HttpStatus.OK.value(), "Success", pagingDTO);
    }

    @Override
    public GeneralResponse<PagingDTO<List<PlanSaleResponseDTO>>> buildPagedPlanSaleResponse(Page<Plan> planPage) {
        List<Plan> entities = planPage.getContent();

        PagingDTO<List<PlanSaleResponseDTO>> pagingDTO = PagingDTO.<List<PlanSaleResponseDTO>>builder()
                .page(planPage.getNumber())
                .size(planPage.getSize())
                .total(planPage.getTotalElements())
                .items(planPage.getContent().stream().map(planMapper::toPlanSaleResponseDTO).toList())
                .build();
        return new GeneralResponse<>(HttpStatus.OK.value(), "Success", pagingDTO);
    }
}
