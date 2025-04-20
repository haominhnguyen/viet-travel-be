package com.fpt.capstone.tourism.service.impl;


import com.fpt.capstone.tourism.dto.common.CostAccountDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.CreateTransactionRequestDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.TourBooking;
import com.fpt.capstone.tourism.model.Transaction;
import com.fpt.capstone.tourism.model.enums.CostAccountStatus;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.model.enums.TransactionStatus;
import com.fpt.capstone.tourism.model.enums.TransactionType;
import com.fpt.capstone.tourism.repository.CostAccountRepository;
import com.fpt.capstone.tourism.repository.TourBookingRepository;
import com.fpt.capstone.tourism.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TourBookingRepository tourBookingRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CostAccountRepository costAccountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        reset(tourBookingRepository, transactionRepository, costAccountRepository);
    }

    @Nested
    class CreateTransactionTests {

        private CreateTransactionRequestDTO createBaseRequestDTO() {
            CreateTransactionRequestDTO requestDTO = new CreateTransactionRequestDTO();
            requestDTO.setBookingCode("250325VT105D10C0035-943");
            requestDTO.setNotes("Thanh toán");
            requestDTO.setPaidBy("Viet Travel");
            requestDTO.setPaymentMethod(PaymentMethod.BANKING);
            requestDTO.setReceivedBy("Nhà cung cấp");
            requestDTO.setCategory(TransactionType.PAYMENT);
            requestDTO.setTotalAmount(200000.0);

            CostAccountDTO costAccountDTO = new CostAccountDTO();
            costAccountDTO.setContent("Dịch vụ ăn");
            costAccountDTO.setAmount(20000.0);
            costAccountDTO.setQuantity(10);
            costAccountDTO.setFinalAmount(200000.0);
            costAccountDTO.setStatus(CostAccountStatus.PENDING);

            List<CostAccountDTO> costAccounts = new ArrayList<>();
            costAccounts.add(costAccountDTO);
            requestDTO.setCostAccounts(costAccounts);

            return requestDTO;
        }

        @Test
        void testCreateTransaction_ValidInputs_Success_UTCID01() {
            // Arrange
            CreateTransactionRequestDTO requestDTO = createBaseRequestDTO();

            TourBooking tourBooking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("250325VT105D10C0035-943")
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(1L)
                    .booking(tourBooking)
                    .category(TransactionType.PAYMENT)
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(200000.0)
                    .paidBy("Viet Travel")
                    .receivedBy("Nhà cung cấp")
                    .notes("Thanh toán")
                    .paymentMethod(PaymentMethod.BANKING)
                    .build();

            // Mock repository calls
            when(tourBookingRepository.findByBookingCode("250325VT105D10C0035-943")).thenReturn(tourBooking);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(costAccountRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

            // Act
            GeneralResponse<?> response = transactionService.createTransaction(requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.getStatus());
            assertEquals("OK", response.getMessage());
            assertEquals(requestDTO, response.getData());
            verify(transactionRepository, times(1)).save(any(Transaction.class));
            verify(costAccountRepository, times(1)).saveAll(anyList());
        }





        @Test
        void testCreateTransaction_NullNotes_Success_UTCID04() {
            // Arrange
            CreateTransactionRequestDTO requestDTO = createBaseRequestDTO();
            requestDTO.setNotes(null);

            TourBooking tourBooking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("250325VT105D10C0035-943")
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(1L)
                    .booking(tourBooking)
                    .category(TransactionType.PAYMENT)
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(200000.0)
                    .paidBy("Viet Travel")
                    .receivedBy("Nhà cung cấp")
                    .notes(null)
                    .paymentMethod(PaymentMethod.BANKING)
                    .build();

            // Mock repository calls
            when(tourBookingRepository.findByBookingCode("250325VT105D10C0035-943")).thenReturn(tourBooking);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(costAccountRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

            // Act
            GeneralResponse<?> response = transactionService.createTransaction(requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.getStatus());
            assertEquals("OK", response.getMessage());
            assertEquals(requestDTO, response.getData());
        }

        @Test
        void testCreateTransaction_NullPaidBy_Success_UTCID05() {
            // Arrange
            CreateTransactionRequestDTO requestDTO = createBaseRequestDTO();
            requestDTO.setPaidBy(null);

            TourBooking tourBooking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("250325VT105D10C0035-943")
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(1L)
                    .booking(tourBooking)
                    .category(TransactionType.PAYMENT)
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(200000.0)
                    .paidBy(null)
                    .receivedBy("Nhà cung cấp")
                    .notes("Thanh toán")
                    .paymentMethod(PaymentMethod.BANKING)
                    .build();

            // Mock repository calls
            when(tourBookingRepository.findByBookingCode("250325VT105D10C0035-943")).thenReturn(tourBooking);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(costAccountRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

            // Act
            GeneralResponse<?> response = transactionService.createTransaction(requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.getStatus());
            assertEquals("OK", response.getMessage());
            assertEquals(requestDTO, response.getData());
        }




        @Test
        void testCreateTransaction_NullReceivedBy_Success_UTCID08() {
            // Arrange
            CreateTransactionRequestDTO requestDTO = createBaseRequestDTO();
            requestDTO.setReceivedBy(null);

            TourBooking tourBooking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("250325VT105D10C0035-943")
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(1L)
                    .booking(tourBooking)
                    .category(TransactionType.PAYMENT)
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(200000.0)
                    .paidBy("Viet Travel")
                    .receivedBy(null)
                    .notes("Thanh toán")
                    .paymentMethod(PaymentMethod.BANKING)
                    .build();

            // Mock repository calls
            when(tourBookingRepository.findByBookingCode("250325VT105D10C0035-943")).thenReturn(tourBooking);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(costAccountRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

            // Act
            GeneralResponse<?> response = transactionService.createTransaction(requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.getStatus());
            assertEquals("OK", response.getMessage());
            assertEquals(requestDTO, response.getData());
        }

        @Test
        void testCreateTransaction_NullCostAccounts_ThrowsException_UTCID09() {
            // Arrange
            CreateTransactionRequestDTO requestDTO = createBaseRequestDTO();
            requestDTO.setCostAccounts(null);

            TourBooking tourBooking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("250325VT105D10C0035-943")
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(1L)
                    .booking(tourBooking)
                    .category(TransactionType.PAYMENT)
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(200000.0)
                    .paidBy("Viet Travel")
                    .receivedBy("Nhà cung cấp")
                    .notes("Thanh toán")
                    .paymentMethod(PaymentMethod.BANKING)
                    .build();

            // Mock repository calls
            lenient().when(tourBookingRepository.findByBookingCode("250325VT105D10C0035-943")).thenReturn(tourBooking);
            lenient().when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

            // Act & Assert
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                transactionService.createTransaction(requestDTO);
            });

            assertEquals("Tạo đặt tour thất bại", exception.getResponseMessage());
        }




        @Test
        void testCreateTransaction_MaxTotalAmount_Success_UTCID12() {
            // Arrange
            CreateTransactionRequestDTO requestDTO = createBaseRequestDTO();
            requestDTO.setTotalAmount(2000000.0);

            TourBooking tourBooking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("250325VT105D10C0035-943")
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(1L)
                    .booking(tourBooking)
                    .category(TransactionType.PAYMENT)
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(2000000.0)
                    .paidBy("Viet Travel")
                    .receivedBy("Nhà cung cấp")
                    .notes("Thanh toán")
                    .paymentMethod(PaymentMethod.BANKING)
                    .build();

            // Mock repository calls
            when(tourBookingRepository.findByBookingCode("250325VT105D10C0035-943")).thenReturn(tourBooking);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(costAccountRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

            // Act
            GeneralResponse<?> response = transactionService.createTransaction(requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.getStatus());
            assertEquals("OK", response.getMessage());
            assertEquals(requestDTO, response.getData());
        }

        @Test
        void testCreateTransaction_ZeroTotalAmount_Success_UTCID13() {
            // Arrange
            CreateTransactionRequestDTO requestDTO = createBaseRequestDTO();
            requestDTO.setTotalAmount(0.0);

            TourBooking tourBooking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("250325VT105D10C0035-943")
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(1L)
                    .booking(tourBooking)
                    .category(TransactionType.PAYMENT)
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(0.0)
                    .paidBy("Viet Travel")
                    .receivedBy("Nhà cung cấp")
                    .notes("Thanh toán")
                    .paymentMethod(PaymentMethod.BANKING)
                    .build();

            // Mock repository calls
            when(tourBookingRepository.findByBookingCode("250325VT105D10C0035-943")).thenReturn(tourBooking);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(costAccountRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

            // Act
            GeneralResponse<?> response = transactionService.createTransaction(requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.getStatus());
            assertEquals("OK", response.getMessage());
            assertEquals(requestDTO, response.getData());
        }

        @Test
        void testCreateTransaction_NegativeTotalAmount_Success_UTCID14() {
            // Arrange
            CreateTransactionRequestDTO requestDTO = createBaseRequestDTO();
            requestDTO.setTotalAmount(-200000.0);

            TourBooking tourBooking = TourBooking.builder()
                    .id(1L)
                    .bookingCode("250325VT105D10C0035-943")
                    .build();

            Transaction transaction = Transaction.builder()
                    .id(1L)
                    .booking(tourBooking)
                    .category(TransactionType.PAYMENT)
                    .transactionStatus(TransactionStatus.PENDING)
                    .amount(-200000.0)
                    .paidBy("Viet Travel")
                    .receivedBy("Nhà cung cấp")
                    .notes("Thanh toán")
                    .paymentMethod(PaymentMethod.BANKING)
                    .build();

            // Mock repository calls
            when(tourBookingRepository.findByBookingCode("250325VT105D10C0035-943")).thenReturn(tourBooking);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
            when(costAccountRepository.saveAll(anyList())).thenReturn(new ArrayList<>());

            // Act
            GeneralResponse<?> response = transactionService.createTransaction(requestDTO);

            // Assert
            assertNotNull(response);
            assertEquals(200, response.getStatus());
            assertEquals("OK", response.getMessage());
            assertEquals(requestDTO, response.getData());
        }
    }
}