package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.BookingRequestDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.UserDTO;
import com.fpt.capstone.tourism.dto.request.ChangePaymentMethodDTO;
import com.fpt.capstone.tourism.dto.response.PublicTourDetailDTO;
import com.fpt.capstone.tourism.dto.response.TourBookingDataResponseDTO;
import com.fpt.capstone.tourism.model.enums.PaymentMethod;
import com.fpt.capstone.tourism.service.BookingService;
import com.fpt.capstone.tourism.service.UserService;
import com.fpt.capstone.tourism.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/booking")
public class BookingController {

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    private final BookingService bookingService;
    private final UserService userService;
    private final VNPayService vnPayService;

    @GetMapping("/details/{tourId}/{scheduleId}")
    public ResponseEntity<GeneralResponse<TourBookingDataResponseDTO>> viewTourDetail(@PathVariable("tourId") Long tourId, @PathVariable("scheduleId") Long scheduleId){
        return ResponseEntity.ok(bookingService.viewTourBookingDetail(tourId, scheduleId));
    }





    @GetMapping("/details/user/{userId}")
    public ResponseEntity<GeneralResponse<?>> getCustomerId(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }



    @PostMapping("/submit")
    public ResponseEntity<GeneralResponse<?>> submitBooking(@RequestBody BookingRequestDTO bookingRequestDTO){
        return ResponseEntity.ok(bookingService.createBooking(bookingRequestDTO));
    }



    @GetMapping("/details/{bookingCode}")
    public ResponseEntity<GeneralResponse<?>> getBookingDetails(@PathVariable("bookingCode") String bookingCode){
        return ResponseEntity.ok(bookingService.getTourBookingDetails(bookingCode));
    }


    @PostMapping("/change-payment-method")
    public ResponseEntity<GeneralResponse<?>> changePaymentMethod(@RequestBody ChangePaymentMethodDTO dto){
        return ResponseEntity.ok(bookingService.changePaymentMethod(dto.getBookingId(), dto.getPaymentMethod()));
    }


    @GetMapping("/vnpay")
    public RedirectView getVnPayPayment(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request);
        String orderInfo = request.getParameter("vnp_OrderInfo");


        String redirectUrl = String.format(
                frontendBaseUrl + "/tour-booking-detail/%s?status=%s",
                URLEncoder.encode(orderInfo, StandardCharsets.UTF_8),
                paymentStatus == 1 ? "success" : "fail"
        );

        bookingService.confirmPayment(paymentStatus, orderInfo);

        return new RedirectView(redirectUrl);
    }


}
