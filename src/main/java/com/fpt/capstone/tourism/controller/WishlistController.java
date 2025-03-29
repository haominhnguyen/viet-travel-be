package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.UserProfileResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.service.CloudinaryService;
import com.fpt.capstone.tourism.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;
    private final UserRepository userRepository;

    @GetMapping("/list-wishlist")
    public ResponseEntity<GeneralResponse<?>> getUserListWishlist(){
        return ResponseEntity.ok(wishlistService.getUserListWishlist());
    }

    @PutMapping("/add-wishlist")
    public ResponseEntity<GeneralResponse<?>> addWishlist(@RequestParam Long itemId){
        return ResponseEntity.ok(wishlistService.addWishlist(itemId));
    }

    @DeleteMapping("/delete-wishlist")
    public ResponseEntity<GeneralResponse<?>> deleteWishlist(@RequestParam Long wishlistId){
        return ResponseEntity.ok(wishlistService.deleteWishlist(wishlistId));
    }
}
