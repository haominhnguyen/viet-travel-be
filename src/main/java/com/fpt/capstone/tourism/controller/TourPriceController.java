package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.TourPriceConfigRequestDTO;
import com.fpt.capstone.tourism.dto.response.TourPriceConfigResponseDTO;
import com.fpt.capstone.tourism.dto.response.TourPriceListResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.TourPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.fpt.capstone.tourism.constants.Constants.Message.USER_NOT_AUTHENTICATED;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.USER_NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head-of-business/tour/{tourId}/price-configurations")
public class TourPriceController {

    private final TourPriceService tourPriceService;
    private final UserRepository userRepository;

    @PostMapping("/manage")
    public ResponseEntity<GeneralResponse<TourPriceConfigResponseDTO>> configureTourPrice(
            @RequestBody TourPriceConfigRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getLoggedInUser(userDetails);
        GeneralResponse<TourPriceConfigResponseDTO> response = tourPriceService.updateTourPrice(requestDTO, user);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<TourPriceListResponseDTO>> getTourPriceConfigurations(
            @PathVariable Long tourId) {
        GeneralResponse<TourPriceListResponseDTO> response = tourPriceService.getTourPriceConfigurations(tourId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/details/{configId}")
    public ResponseEntity<GeneralResponse<TourPriceConfigResponseDTO>> getTourPriceConfigurationById(
            @PathVariable Long tourId,
            @PathVariable Long configId) {
        GeneralResponse<TourPriceConfigResponseDTO> response = tourPriceService.getTourPriceConfigurationById(tourId, configId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

//    @DeleteMapping("/{tourId}/{configId}")
//    public ResponseEntity<GeneralResponse<String>> deleteTourPriceConfiguration(
//            @PathVariable Long tourId,
//            @PathVariable Long configId) {
//        GeneralResponse<String> response = tourPriceService.deleteTourPriceConfiguration(tourId, configId);
//        return ResponseEntity.status(response.getCode()).body(response);
//    }

    private User getLoggedInUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw BusinessException.of(HttpStatus.UNAUTHORIZED, USER_NOT_AUTHENTICATED);
        }

        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }
}
