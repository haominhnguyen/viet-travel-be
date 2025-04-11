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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/public/booking")
public class BookingController {


    private final BookingService bookingService;
    private final UserService userService;

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


}
