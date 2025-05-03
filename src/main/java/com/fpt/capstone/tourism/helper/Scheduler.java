package com.fpt.capstone.tourism.helper;

import com.fpt.capstone.tourism.model.CostAccount;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import com.fpt.capstone.tourism.repository.CostAccountRepository;
import com.fpt.capstone.tourism.repository.TourBookingRepository;
import com.fpt.capstone.tourism.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final TourBookingRepository tourBookingRepository;
    private final TransactionRepository transactionRepository;
    private final CostAccountRepository costAccountRepository;

    @Scheduled(fixedRate = 2 * 60 * 60 * 1000)
    void removeExpiredUnpaidBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<TourBooking> expiredBookings = tourBookingRepository.findByStatusAndExpiredAtBeforeAndDeletedFalse(
                TourBookingStatus.PENDING, now
        );

        for (TourBooking booking : expiredBookings) {
            booking.setStatus(TourBookingStatus.CANCELLED_WITHOUT_REFUND); // or mark as deleted
            booking.setDeleted(true);

            List<Transaction> transactions = transactionRepository.findByBooking_Id(booking.getId());

            for (Transaction transaction : transactions) {
                transaction.setTransactionStatus(TransactionStatus.CANCELLED);

                List<CostAccount> costAccounts = costAccountRepository.findByTransaction_Id(transaction.getId());

                for(CostAccount costAccount : costAccounts) {
                    costAccount.setStatus(CostAccountStatus.CANCELLED);
                }
                costAccountRepository.saveAll(costAccounts);
            }
            transactionRepository.saveAll(transactions);
        }
        tourBookingRepository.saveAll(expiredBookings);
    }
}
