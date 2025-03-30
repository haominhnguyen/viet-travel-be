package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourWithNumberBookingDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TransactionAccountantResponseDTO;
import com.fpt.capstone.tourism.helper.IHelper.TransactionHelper;
import com.fpt.capstone.tourism.mapper.TransactionMapper;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.TransactionType;
import com.fpt.capstone.tourism.service.TransactionService;
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
public class TransactionHelperImpl implements TransactionHelper {

    private final TransactionMapper transactionMapper;

    @Override
    public Specification<Transaction> buildTransactionPublicSearchSpecification(String keyword, TransactionType transactionType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                // Search in paid by
                Expression<String> normalizedPaidBy = cb.function("unaccent", String.class, cb.lower(root.get("paidBy")));
                Predicate paidByPredicate = cb.like(normalizedPaidBy, cb.concat("%", cb.concat(normalizedKeyword, "%")));
                predicates.add(paidByPredicate);

                // Search in paid by
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(root.get("receivedBy")));
                Predicate receivedByPredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                predicates.add(receivedByPredicate);
            }

            predicates.add(cb.equal(root.get("category"), transactionType));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public GeneralResponse<?> buildPublicTransactionPagedResponse(Page<Transaction> transactionPage) {

        List<TransactionAccountantResponseDTO> transactionAccountantResponseDTOS = transactionPage.getContent().stream().map(transactionMapper::toTransactionAccountantResponseDTO).toList();

        PagingDTO<List<TransactionAccountantResponseDTO>> pagingDTO = PagingDTO.<List<TransactionAccountantResponseDTO>>builder()
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .total(transactionPage.getTotalElements())
                .items(transactionAccountantResponseDTOS)
                .build();

        return GeneralResponse.of(pagingDTO);
    }
}
