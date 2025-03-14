package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourBookingDTO;
import com.fpt.capstone.tourism.dto.common.TourBookingWithDetailDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingSaleResponseDTO;
import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.mapper.BookingMapper;
import com.fpt.capstone.tourism.mapper.TourBookingCustomerMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.repository.TourBookingCustomerRepository;
import com.fpt.capstone.tourism.repository.TransactionRepository;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingHelperImpl implements BookingHelper {


    private final BookingMapper bookingMapper;
    private final TransactionRepository transactionRepository;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;
    private final TourBookingCustomerMapper tourBookingCustomerMapper;

    @Override
    public String generateBookingCode(Long tourId, Long scheduleId, Long customerId) {
        // Get current date in DDMMYY format
        String datePart = new SimpleDateFormat("ddMMyy").format(new Date());

        // Extract last 4 digits of the customer ID (ensuring at least 4 digits)
        String customerPart = "C" + String.format("%04d", customerId % 10000);


        String millisPart = String.valueOf(System.currentTimeMillis() % 1000);

        // Construct the booking code
        return String.format("%sVT%dSD%d%s-%s", datePart, tourId, scheduleId, customerPart, millisPart);
    }

    @Override
    public GeneralResponse<PagingDTO<List<TourBookingWithDetailDTO>>> buildPagedResponse(Page<TourBooking> tourBookingPage) {
        List<TourBookingWithDetailDTO> tourBookingWithDetailDTOS = new ArrayList<>();

        //Loop used to iterate through tour booking list
        for (TourBooking tourBooking : tourBookingPage.getContent()) {
            TourBookingDTO tourBookingDTO = bookingMapper.toDto(tourBooking);
            List<Transaction> tourBookingReceipts = transactionRepository.findAllByBookingAndCategory(tourBooking, TransactionType.RECEIPT);

            //Total = sum of transaction amount
            double totalCost = getTotal(tourBookingReceipts);


            double paid = getPaidAmount(tourBookingReceipts);


            User operator = tourBooking.getTourSchedule().getOperator();

            TourBookingCustomer customer = tourBookingCustomerRepository.findByTourBookingAndBookedPerson(tourBooking, true);



            TourBookingWithDetailDTO tourBookingWithDetailDTO = TourBookingWithDetailDTO.builder()
                    .tourBooking(tourBookingDTO)
                    .total(totalCost)
                    .remaining(totalCost - paid)
                    .paid(paid)
                    .operator(bookingMapper.toStaffDto(operator))
                    .bookedCustomer(tourBookingCustomerMapper.toBookedPersonDTO(customer))
                    .build();

            tourBookingWithDetailDTOS.add(tourBookingWithDetailDTO);
        }



        PagingDTO<List<TourBookingWithDetailDTO>> pagingDTO = PagingDTO.<List<TourBookingWithDetailDTO>>builder()
                .page(tourBookingPage.getNumber())
                .size(tourBookingPage.getSize())
                .total(tourBookingPage.getTotalElements())
                .items(tourBookingWithDetailDTOS)
                .build();
        return new GeneralResponse<>(HttpStatus.OK.value(), "Success", pagingDTO);
    }

    @Override
    public Specification<TourBooking> buildSearchSpecification(String keyword, Boolean isDeleted) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                // Search in booking code
                Expression<String> normalizedBookingCode = cb.function("unaccent", String.class, cb.lower(root.get("bookingCode")));
                Predicate bookingCodePredicate = cb.like(normalizedBookingCode, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                // Search in tour name
                Join<TourBooking, Tour> tourJoin = root.join("tour", JoinType.LEFT);
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(tourJoin.get("name")));
                Predicate tourNamePredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                // Search in user full name
                Join<TourBooking, User> userJoin = root.join("user", JoinType.LEFT);
                Expression<String> normalizedUserFullName = cb.function("unaccent", String.class, cb.lower(userJoin.get("fullName")));
                Predicate userFullNamePredicate = cb.like(normalizedUserFullName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                predicates.add(cb.or(bookingCodePredicate, tourNamePredicate, userFullNamePredicate));
            }

            // Filter by status
            if (keyword != null) {
                try {
                    TourBookingStatus status = TourBookingStatus.valueOf(keyword.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status values
                }
            }

            // Filter by deletion status
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("deleted"), isDeleted));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public Double getPaidAmount(List<Transaction> tourBookingReceipts) {
        //Calculate paid amount by sum cost account in transaction
        for (Transaction transaction : tourBookingReceipts) {
            List<CostAccount> costAccounts = transaction.getCostAccount();
            if(costAccounts != null && !costAccounts.isEmpty()) {
                return costAccounts.stream()
                        .filter(costAccount -> costAccount.getStatus() == CostAccountStatus.PAID) // Filter only PAID status
                        .mapToDouble(CostAccount::getAmount) // Assuming getAmount() returns a numeric value
                        .sum();
            }
        }
        return 0.0;
    }

    @Override
    public Double getTotal(List<Transaction> tourBookingReceipts) {
        return tourBookingReceipts.stream().mapToDouble(Transaction::getAmount).sum();
    }

    @Override
    public List<TourBookingSaleResponseDTO> setPaymentStatistics(List<TourBooking> tourBookings) {

        List<TourBookingSaleResponseDTO> tourBookingSaleResponseDTOS = new ArrayList<>();

        for (TourBooking tourBooking : tourBookings) {

            TourBookingSaleResponseDTO tourBookingSaleResponseDTO = bookingMapper.toTourBookingSaleResponseDTO(tourBooking);
            List<Transaction> tourBookingReceipts = transactionRepository.findAllByBookingAndCategory(tourBooking, TransactionType.RECEIPT);
            double totalCost = getTotal(tourBookingReceipts);
            double paid = getPaidAmount(tourBookingReceipts);
            tourBookingSaleResponseDTO.setPaid(paid);
            tourBookingSaleResponseDTO.setTotal(totalCost);
            tourBookingSaleResponseDTOS.add(tourBookingSaleResponseDTO);
        }


        return tourBookingSaleResponseDTOS;
    }
}
