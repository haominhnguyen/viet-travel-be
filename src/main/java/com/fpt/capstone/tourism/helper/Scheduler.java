package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.repository.TourBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final TourBookingRepository tourBookingRepository;

    @Scheduled(fixedRate = 2 * 60 * 60 * 1000)
    void removeExpiredUnpaidBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<TourBooking> expiredBookings = tourBookingRepository.findByStatusAndExpiredAtBeforeAndDeletedFalse(
                TourBookingStatus.PENDING, now
        );

        for (TourBooking booking : expiredBookings) {
            booking.setStatus(TourBookingStatus.CANCELLED_WITHOUT_REFUND); // or mark as deleted
            booking.setDeleted(true);
        }

        tourBookingRepository.saveAll(expiredBookings);
    }
}
