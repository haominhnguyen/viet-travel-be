package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourScheduleSettlementDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TransactionAccountantResponseDTO;
import com.fpt.capstone.tourism.helper.IHelper.TourScheduleHelper;
import com.fpt.capstone.tourism.mapper.TourMapper;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.enums.TourScheduleStatus;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TourScheduleHelperImpl implements TourScheduleHelper {

    private final TourMapper tourMapper;

    @Override
    public Specification<TourSchedule> buildTourScheduleSearchSpecification(String keyword, List<TourScheduleStatus> tourScheduleStatus) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                // Search in paid by
                Expression<String> normalizedPaidBy = cb.function("unaccent", String.class, cb.lower(root.get("tour").get("name")));
                Predicate tourNamePredicate = cb.like(normalizedPaidBy, cb.concat("%", cb.concat(normalizedKeyword, "%")));
                predicates.add(tourNamePredicate);

            }

            predicates.add(root.get("status").in(tourScheduleStatus));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public GeneralResponse<?> buildPublicTourSchedulePagedResponse(Page<TourSchedule> tourSchedulePage) {
        List<TourScheduleSettlementDTO> transactionAccountantResponseDTOS = tourSchedulePage.getContent().stream().map(tourMapper::toTourScheduleSettlementDTO).toList();

        PagingDTO<List<TourScheduleSettlementDTO>> pagingDTO = PagingDTO.<List<TourScheduleSettlementDTO>>builder()
                .page(tourSchedulePage.getNumber())
                .size(tourSchedulePage.getSize())
                .total(tourSchedulePage.getTotalElements())
                .items(transactionAccountantResponseDTOS)
                .build();

        return GeneralResponse.of(pagingDTO);
    }
}
