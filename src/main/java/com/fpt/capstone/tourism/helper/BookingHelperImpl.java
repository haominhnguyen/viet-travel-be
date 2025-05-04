package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.mapper.BookingMapper;
import com.fpt.capstone.tourism.mapper.TourBookingCustomerMapper;
import com.fpt.capstone.tourism.mapper.TourMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.*;
import com.fpt.capstone.tourism.repository.*;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingHelperImpl implements BookingHelper {


    private final BookingMapper bookingMapper;
    private final TransactionRepository transactionRepository;
    private final TourBookingCustomerRepository tourBookingCustomerRepository;
    private final TourBookingCustomerMapper tourBookingCustomerMapper;
    private final TourScheduleRepository tourScheduleRepository;
    private final TourBookingServiceRepository tourBookingServiceRepository;

    private final TourMapper tourMapper;


    private final List<TransactionType> transactionTypes = List.of(TransactionType.RECEIPT, TransactionType.COLLECTION);
    private final TourDayRepository tourDayRepository;


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
            TourBookingShortSaleResponseDTO tourBookingDTO = tourMapper.toTourBookingShortSaleResponseDTO(tourBooking);

            TourBookingCustomer customer = tourBookingCustomerRepository.findByTourBookingAndBookedPerson(tourBooking, true);

            TourBookingWithDetailDTO tourBookingWithDetailDTO = TourBookingWithDetailDTO.builder()
                    .tourBooking(tourBookingDTO)
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
    public Specification<TourBooking> buildSearchSpecification(String keyword, String status) {
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



                predicates.add(cb.or(bookingCodePredicate, tourNamePredicate));
            }

            //TourBookingStatus
            predicates.add(cb.or(
                    cb.equal(root.get("status"), TourBookingStatus.PENDING),
                    cb.equal(root.get("status"), TourBookingStatus.SUCCESS)
            ));

            //Sale is null
            predicates.add(cb.isNull(root.get("sale")));

            // Filter by status
            if (status != null && !status.isEmpty()) {
                TourBookingStatus tourBookingStatus = TourBookingStatus.valueOf(status);
                predicates.add(cb.equal(root.get("status"), tourBookingStatus));
            }

            // Filter by deletion status
                predicates.add(cb.equal(root.get("deleted"), false));

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
            TourBookingSaleResponseDTO tourBookingSaleResponseDTO = setPaymentStatistic(tourBooking);
            tourBookingSaleResponseDTOS.add(tourBookingSaleResponseDTO);
        }


        return tourBookingSaleResponseDTOS;
    }

    @Override
    public TourBookingSaleResponseDTO setPaymentStatistic(TourBooking tourBooking) {
        TourBookingSaleResponseDTO tourBookingSaleResponseDTO = bookingMapper.toTourBookingSaleResponseDTO(tourBooking);
        List<Transaction> tourBookingReceipts = transactionRepository.findAllByBookingAndCategoryIn(tourBooking, transactionTypes);
        double totalCost = getTotal(tourBookingReceipts);
        double paid = getPaidAmount(tourBookingReceipts);
        tourBookingSaleResponseDTO.setPaid(paid);
        tourBookingSaleResponseDTO.setTotal(totalCost);
        return tourBookingSaleResponseDTO;
    }

    @Override
    public TourBookingDetailSaleResponseDTO setPaymentStatisticForBookingDetail(TourBooking tourBooking) {
        log.info("Start setPaymentStatisticForBookingDetail booking detail with ID: {}", tourBooking.getId());
        TourBookingDetailSaleResponseDTO tourBookingSaleResponseDTO = bookingMapper.toBookingDetailSaleResponseDTO(tourBooking);

        log.info("Start findAllByBookingAndCategoryIn booking detail with ID: {}", tourBooking.getId());
        List<Transaction> tourBookingReceipts = transactionRepository.findAllByBookingAndCategoryIn(tourBooking, transactionTypes);
        log.info("End findAllByBookingAndCategoryIn booking detail with ID: {}", tourBooking.getId());

        log.info("Start findTourScheduleByTourId booking detail with ID: {}", tourBooking.getId());
        PublicTourScheduleDTO publicTour = tourScheduleRepository.findTourScheduleByTourId(tourBooking.getTour().getId(), tourBooking.getTourSchedule().getId());
        log.info("End findTourScheduleByTourId booking detail with ID: {}", tourBooking.getId());

        double totalCost = getTotal(tourBookingReceipts);
        double paid = getPaidAmount(tourBookingReceipts);
        tourBookingSaleResponseDTO.setPaid(paid);
        tourBookingSaleResponseDTO.setTotal(totalCost);
        tourBookingSaleResponseDTO.setSchedule(publicTour);
        tourBookingSaleResponseDTO.setCreatedAt(tourBooking.getCreatedAt());
        tourBookingSaleResponseDTO.setTransactions(tourBookingReceipts.stream().map(bookingMapper::toTransactionDTO).toList());
        return tourBookingSaleResponseDTO;
    }



    @Override
    public List<TourBookingServiceSaleResponseDTO> getTourBookingListService(List<TourDay> tourDays, TourBooking tourBooking) {
        List<TourBookingServiceSaleResponseDTO> tourBookingServiceSaleResponseDTOS = new ArrayList<>();

        for (TourDay tourDay : tourDays) {

            List<TourBookingService> tourBookingServices = tourBookingServiceRepository.findByTourDayAndBooking(tourDay, tourBooking);
            TourDayDTO tourDayDTO = bookingMapper.toTourDayDto(tourDay);



            TourBookingServiceSaleResponseDTO tourBookingServiceSaleResponseDTO = TourBookingServiceSaleResponseDTO.builder()
                    .tourDay(tourDayDTO)
                    .bookingServices(tourBookingServices.stream().map(bookingMapper::toTourBookingServiceDTO).toList())
                    .build();

            tourBookingServiceSaleResponseDTOS.add(tourBookingServiceSaleResponseDTO);

        }

        return tourBookingServiceSaleResponseDTOS;
    }

    @Override
    public Specification<Tour> searchByNameAndTourType(String name, TourType tourType) {
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

            if (tourType != null) {
                predicates.add(cb.equal(root.get("tourType"), tourType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public List<TourDay> generateTourDays(int numberDays, Tour tour) {
        List<TourDay> tourDays = new ArrayList<>();
        for (int i = 1; i <= numberDays; i++) {
            TourDay tourDay = new TourDay();
            tourDay.setDayNumber(i);
            tourDay.setTitle("Ngày " + i);
            tourDay.setDeleted(false);
            tourDay.setTour(tour);
            tourDays.add(tourDay);
        }
        return tourDays;
    }

    @Override
    public String loadTemplate(String path) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Template file not found: " + path);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
