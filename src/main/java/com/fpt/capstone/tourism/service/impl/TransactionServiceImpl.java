package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.CreateTransactionRequestDTO;
import com.fpt.capstone.tourism.dto.request.UpdateTransactionRequestDTO;
import com.fpt.capstone.tourism.dto.response.TransactionAccountantResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.BookingHelper;
import com.fpt.capstone.tourism.helper.IHelper.TransactionHelper;
import com.fpt.capstone.tourism.mapper.TransactionMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import com.fpt.capstone.tourism.model.enums.TourBookingStatus;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.repository.CostAccountRepository;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import com.fpt.capstone.tourism.repository.TourBookingRepository;
import com.fpt.capstone.tourism.repository.TransactionRepository;
import com.fpt.capstone.tourism.service.TransactionService;
import jakarta.persistence.criteria.Join;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CostAccountRepository costAccountRepository;
    private final TourBookingRepository tourBookingRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    private final TransactionHelper transactionHelper;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public GeneralResponse<?> getTransactions(int page, int size, String keyword, String sortField, String sortDirection, List<TransactionType> transactionTypes, String transactionStatus) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Transaction> spec = transactionHelper.buildTransactionPublicSearchSpecification(keyword, transactionTypes, transactionStatus);

            Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

            return transactionHelper.buildPublicTransactionPagedResponse(transactionPage);
        } catch (Exception ex) {
            throw BusinessException.of(GET_DATA_FAILED, ex);
        }
    }

    @Override
    public GeneralResponse<?> getTransactionDetails(Long id) {
        try {
            Transaction transaction = transactionRepository.findById(id).orElseThrow();
            TransactionAccountantResponseDTO dto = transactionMapper.toTransactionAccountantResponseDTO(transaction);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TRANSACTION_DETAILS_FAILED, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> updateTransaction(UpdateTransactionRequestDTO dto) {
        try {
            Transaction transaction = transactionRepository.findById(dto.getId()).orElseThrow();

            transaction.setNotes(dto.getNotes());
            transaction.setAmount(dto.getTotalAmount());
            transaction.setPaidBy(dto.getPaidBy());
            transaction.setReceivedBy(dto.getReceivedBy());
            transaction.setPaymentMethod(dto.getPaymentMethod());

            // Get existing cost accounts
            List<CostAccount> existingCostAccounts = transaction.getCostAccount();

            Map<Long, CostAccount> existingCostAccountsMap = existingCostAccounts.stream()
                    .collect(Collectors.toMap(CostAccount::getId, Function.identity()));

            List<CostAccount> updatedCostAccounts = new ArrayList<>();

            // Update or delete cost accounts
            // Process cost accounts from DTO
            for (CostAccountDTO costAccountDTO : dto.getCostAccounts()) {
                if (costAccountDTO.getId() != null && existingCostAccountsMap.containsKey(costAccountDTO.getId())) {
                    // Update existing cost account
                    CostAccount costAccount = existingCostAccountsMap.get(costAccountDTO.getId());
                    costAccount.setContent(costAccountDTO.getContent());
                    costAccount.setAmount(costAccountDTO.getAmount());
                    costAccount.setDiscount(costAccountDTO.getDiscount());
                    costAccount.setQuantity(costAccountDTO.getQuantity());
                    costAccount.setFinalAmount(costAccountDTO.getFinalAmount());
                    costAccount.setStatus(costAccountDTO.getStatus());
                    updatedCostAccounts.add(costAccount);
                } else {
                    // Add new cost account
                    CostAccount newCostAccount = CostAccount.builder()
                            .content(costAccountDTO.getContent())
                            .amount(costAccountDTO.getAmount())
                            .discount(costAccountDTO.getDiscount())
                            .quantity(costAccountDTO.getQuantity())
                            .finalAmount(costAccountDTO.getFinalAmount())
                            .status(costAccountDTO.getStatus())
                            .transaction(transaction) // Link to the transaction
                            .build();
                    updatedCostAccounts.add(newCostAccount);
                }
            }

            // Remove cost accounts that are no longer in DTO
            List<Long> dtoCostAccountIds = dto.getCostAccounts().stream()
                    .map(CostAccountDTO::getId)
                    .filter(Objects::nonNull)
                    .toList();


            existingCostAccounts.removeIf(costAccount -> !dtoCostAccountIds.contains(costAccount.getId()));

            // Check if all cost accounts have status PAID
            boolean allPaid = updatedCostAccounts.stream()
                    .allMatch(costAccount -> costAccount.getStatus() == CostAccountStatus.PAID);

            // Update transaction status if all cost accounts are PAID
            if (allPaid) {
                transaction.setTransactionStatus(TransactionStatus.PAID); // Update to desired status
            } else {
                transaction.setTransactionStatus(TransactionStatus.PENDING); // Keep as pending if any cost account is not paid

            }

            costAccountRepository.saveAll(updatedCostAccounts);

            // Set updated cost accounts list
            transaction.setCostAccount(updatedCostAccounts);

            // Save transaction
            Transaction savedEntity = transactionRepository.save(transaction);


            return GeneralResponse.of(transactionMapper.toTransactionAccountantResponseDTO(savedEntity));
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_TRANSACTION_FAILED, ex);
        }
    }

    @Override
    public GeneralResponse<?> getBookingByBookingCode(String keyword) {
        try {
            List<Object[]> results = tourBookingRepository.findByBookingCodeContaining(keyword);

            List<TourBookingWithBookedPersonDTO> dto =  results.stream().map(row -> TourBookingWithBookedPersonDTO.builder()
                    .id(((Number) row[0]).longValue())  // Convert numeric ID
                    .bookingCode((String) row[1])
                    .bookedPerson(BookedPersonDTO.builder()
                            .fullName((String) row[2])
                            .phone((String) row[3])
                            .email((String) row[4])
                            .address((String) row[5])
                            .build())
                    .build()).toList();
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(GET_BOOKING_FAILED, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<?> createTransaction(CreateTransactionRequestDTO dto) {
        try {
            TourBooking tourBooking = tourBookingRepository.findByBookingCode(dto.getBookingCode());

            boolean allPaid = dto.getCostAccounts().stream()
                    .allMatch(costAccount -> costAccount.getStatus() == CostAccountStatus.PAID);

            // Create new transaction
            Transaction transaction = Transaction.builder()
                    .booking(tourBooking)
                    .category(dto.getCategory())
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(dto.getTotalAmount())
                    .paidBy(dto.getPaidBy())
                    .receivedBy(dto.getReceivedBy())
                    .notes(dto.getNotes())
                    .paymentMethod(dto.getPaymentMethod())
                    .build();

            if (allPaid) {
                transaction.setTransactionStatus(TransactionStatus.PAID); // Update to desired status
            } else {
                transaction.setTransactionStatus(TransactionStatus.PENDING); // Keep as pending if any cost account is not paid

            }

            transaction = transactionRepository.save(transaction);

            List<CostAccount> costAccounts = new ArrayList<>();

            // Create cost accounts
            for (CostAccountDTO costAccountDTO : dto.getCostAccounts()) {
                CostAccount costAccount = CostAccount.builder()
                        .content(costAccountDTO.getContent())
                        .amount(costAccountDTO.getAmount())
                        .discount(costAccountDTO.getDiscount())
                        .quantity(costAccountDTO.getQuantity())
                        .finalAmount(costAccountDTO.getFinalAmount())
                        .status(costAccountDTO.getStatus())
                        .transaction(transaction) // Link to the transaction
                        .build();
                costAccounts.add(costAccount);
            }

            costAccountRepository.saveAll(costAccounts);

            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(CREATE_BOOKING_FAILED, ex);
        }
    }

    @Override
    public GeneralResponse<?> getBookingProvider(Long bookingId) {
        try {
            List<ServiceProvider> providers = serviceProviderRepository.findDistinctServiceProvidersByBookingId(bookingId);
            List<ServiceProviderSimpleDTO> dto = providers.stream().map(transactionMapper::toServiceProviderSimpleDTO).toList();
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(GET_TRANSACTION_DETAILS_FAILED, ex);
        }
    }

}
