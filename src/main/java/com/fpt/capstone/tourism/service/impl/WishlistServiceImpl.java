package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.WishlistDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.Wishlist;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.repository.WishlistRepository;
import com.fpt.capstone.tourism.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    @Override
    public GeneralResponse<?> getUserListWishlist() {
        try {
            Long userId = getCurrentUser().getId();
            List<Wishlist> wishlists = wishlistRepository.findByUserId(userId);
            List<WishlistDTO> resultDTOs = wishlists.stream()
                    .map(wishlist -> {
                        Tour tour = tourRepository.findById(wishlist.getItemId()).orElseThrow(
                                () -> BusinessException.of("Tour not found")
                        );
                        WishlistDTO wishlistDTO = WishlistDTO.builder()
                                .id(wishlist.getId())
                                .itemId(wishlist.getItemId())
                                .itemType(wishlist.getItemType())
                                .tourName(Optional.ofNullable(tour.getName()).orElseThrow(null))
                                .build();
                        return wishlistDTO;
                    }).collect(Collectors.toList());
            return new GeneralResponse<>(HttpStatus.OK.value(), "Thành công", resultDTOs);
        } catch (Exception ex) {
            throw BusinessException.of("Faild", ex);
        }
    }

    @Override
    public GeneralResponse<?> addWishlist(Long itemId) {
        try {
            User user = getCurrentUser();
            Wishlist dbWishlist = wishlistRepository.findByItemId(itemId);
            if(dbWishlist != null){
                throw BusinessException.of("Tour đã có trong danh sách yêu thích");
            }
            Wishlist wishlist = Wishlist.builder()
                    .itemId(itemId)
                    .itemType("Tour")
                    .user(user)
                    .build();
            wishlistRepository.save(wishlist);
            Tour tour = tourRepository.findById(itemId).orElseThrow(
                    () -> BusinessException.of("Tour not found")
            );
            WishlistDTO wishlistDTO = WishlistDTO.builder()
                    .itemId(wishlist.getItemId())
                    .itemType(wishlist.getItemType())
                    .tourName(Optional.ofNullable(tour.getName()).orElseThrow(null))
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Thành công", wishlistDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Faild", ex);
        }
    }

    @Override
    public GeneralResponse<?> deleteWishlist(Long wishlistId) {
        try {
            User user = getCurrentUser();
            Wishlist wishlist = wishlistRepository.findById(wishlistId).orElseThrow(
                    () -> BusinessException.of("Wishlist not found")
            );
            if(user.getId() != wishlist.getUser().getId()){
                throw BusinessException.of("Bạn không có quyền xóa dữ liệu này");
            }
            wishlistRepository.deleteById(wishlistId);
            Tour tour = tourRepository.findById(wishlist.getItemId()).orElseThrow(
                    () -> BusinessException.of("Tour not found")
            );
            WishlistDTO wishlistDTO = WishlistDTO.builder()
                    .id(wishlist.getId())
                    .itemId(wishlist.getItemId())
                    .itemType(wishlist.getItemType())
                    .tourName(Optional.ofNullable(tour.getName()).orElseThrow(null))
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Thành công", wishlistDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Faild", ex);
        }
    }

    private User getCurrentUser() {
        User user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> BusinessException.of("User not found"));
        }
        return user;
    }
}
