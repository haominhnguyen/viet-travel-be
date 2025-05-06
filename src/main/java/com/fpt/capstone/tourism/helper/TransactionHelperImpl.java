package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TransactionAccountantResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.TransactionHelper;
import com.fpt.capstone.tourism.mapper.TransactionMapper;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.TourSchedule;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.repository.ServiceRepository;
import com.fpt.capstone.tourism.repository.TourScheduleRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.fpt.capstone.tourism.constants.Constants.Message.SERVICE_NOT_FOUND;
import static com.fpt.capstone.tourism.constants.Constants.Message.TOUR_SCHEDULE_NOT_FOUND;

@Component
@RequiredArgsConstructor
public class TransactionHelperImpl implements TransactionHelper {

    private final TransactionMapper transactionMapper;
    private final TourScheduleRepository tourScheduleRepository;
    private final ServiceRepository serviceRepository;

    @Override
    public Specification<Transaction> buildTransactionPublicSearchSpecification(String keyword, List<TransactionType> transactionTypes, String transactionStatus) {
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

            if (transactionStatus != null && !transactionStatus.isEmpty()) {
                TransactionStatus status = TransactionStatus.valueOf(transactionStatus.trim());
                predicates.add(root.get("transactionStatus").in(status));
            }

            predicates.add(root.get("category").in(transactionTypes));

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

    @Override
    public Double calculateTransportFeePerPerson(Long scheduleId, Long serviceId) {
        TourSchedule tourSchedule = tourScheduleRepository.findById(scheduleId).orElseThrow(
                () ->BusinessException.of(TOUR_SCHEDULE_NOT_FOUND)
        );

        int minPax = tourSchedule.getTourPax().getMinPax();

        Service service = serviceRepository.findById(serviceId).orElseThrow(
                () ->BusinessException.of(SERVICE_NOT_FOUND)
        );

        Double result = service.getNettPrice()/minPax;

        return result;
    }
}
