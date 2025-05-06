package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TourDTO;
import com.fpt.capstone.tourism.dto.common.TourWithNumberBookingDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.helper.IHelper.TourHelper;
import com.fpt.capstone.tourism.mapper.BookingMapper;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import com.fpt.capstone.tourism.repository.TourBookingRepository;
import jakarta.persistence.criteria.Expression;
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
public class TourHelperImpl implements TourHelper {


    private final TourBookingRepository tourBookingRepository;
    private final BookingMapper bookingMapper;


    @Override
    public Specification<Tour> buildTourPublicSearchSpecification(String keyword, TourStatus status,  TourType tourType) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                // Search in tour name
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(root.get("name")));
                Predicate tourNamePredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                predicates.add(tourNamePredicate);
            }

            predicates.add(cb.equal(root.get("deleted"), false));

            if(status != null) {
                predicates.add(cb.equal(root.get("tourStatus"), status));
            }


            predicates.add(cb.equal(root.get("tourType"), tourType));

            if (tourType.toString().equalsIgnoreCase(TourType.SIC.name())) {
                predicates.add(cb.or(
                        cb.equal(root.get("tourStatus"), TourStatus.OPENED),
                        cb.equal(root.get("tourStatus"), TourStatus.CLOSED)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public GeneralResponse<PagingDTO<List<TourWithNumberBookingDTO>>> buildPublicTourPagedResponse(Page<Tour> tourPage) {

        List<TourWithNumberBookingDTO> tourWithNumberBookingDTOS = new ArrayList<>();

        List<TourBookingStatus> tourBookingStatuses = new ArrayList<>();
        tourBookingStatuses.add(TourBookingStatus.PENDING);
        tourBookingStatuses.add(TourBookingStatus.SUCCESS);

        for (Tour tour : tourPage.getContent()) {
            long numberBooking = tourBookingRepository.countByTourAndStatusIn(tour, tourBookingStatuses);
            TourWithNumberBookingDTO tourWithNumberBookingDTO = TourWithNumberBookingDTO.builder()
                    .tour(bookingMapper.toTourDTO(tour))
                    .numberBooking(numberBooking)
                    .build();
            tourWithNumberBookingDTOS.add(tourWithNumberBookingDTO);
        }




        PagingDTO<List<TourWithNumberBookingDTO>> pagingDTO = PagingDTO.<List<TourWithNumberBookingDTO>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tourWithNumberBookingDTOS)
                .build();
        return new GeneralResponse<>(HttpStatus.OK.value(), "Success", pagingDTO);
    }
}
