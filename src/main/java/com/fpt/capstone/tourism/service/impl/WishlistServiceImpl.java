package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.WishlistDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.model.Wishlist;
import com.fpt.capstone.tourism.repository.TourImageRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.UserRepository;
import com.fpt.capstone.tourism.repository.WishlistRepository;
import com.fpt.capstone.tourism.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final TourImageRepository tourImageRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    @Override
    public GeneralResponse<?> getUserListWishlist() {
        try {
            User user = null;
            Long userId = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                userId = userRepository.findIdByUsername(auth.getName());
            }

            List<Object[]> rawWishlist = wishlistRepository.findWishlistByUserId(userId);
            List<Long> tourIds = rawWishlist.stream()
                    .map(row -> ((Number) row[1]).longValue())
                    .distinct()
                    .toList();
            // Map: tourId -> name
            Map<Long, String> tourNameMap = tourRepository.findToursByIds(tourIds).stream()
                    .collect(Collectors.toMap(
                            row -> ((Number) row[0]).longValue(),
                            row -> (String) row[1]
                    ));
            // Map: tourId -> List<image_url>
            Map<Long, List<String>> tourImageMap = new HashMap<>();
            for (Object[] row : tourImageRepository.findImagesByTourIds(tourIds)) {
                Long tourId = ((Number) row[0]).longValue();
                String imageUrl = (String) row[1];
                tourImageMap.computeIfAbsent(tourId, k -> new ArrayList<>()).add(imageUrl);
            }
            List<WishlistDTO>resultDTOs =  rawWishlist.stream().map(row -> {
                Long wishlistId = ((Number) row[0]).longValue();
                Long itemId = ((Number) row[1]).longValue();
                String itemType = (String) row[2];
                return WishlistDTO.builder()
                        .id(wishlistId)
                        .itemId(itemId)
                        .itemType(itemType)
                        .tourName(tourNameMap.getOrDefault(itemId, ""))
                        .tourImageUrl(tourImageMap.getOrDefault(itemId, List.of()).stream().findFirst().orElse(null)) // hoặc get list luôn nếu muốn
                        .build();
            }).toList();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Lấy wishlist thành công", resultDTOs);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy wishlist thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> addWishlist(Long itemId) {
        try {
            User user = getCurrentUser();
            Wishlist dbWishlist = wishlistRepository.findByItemIdAndUserId(user.getId(), itemId);
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
                    .tourImageUrl(Optional.ofNullable(tour.getTourImages().get(0).getImageUrl()).orElseThrow(null))
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Lấy wishlist thành công", wishlistDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Thêm wishlist thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> deleteWishlist(Long wishlistId) {
        try {
            User user = getCurrentUser();
            Wishlist wishlist = wishlistRepository.findById(wishlistId).orElseThrow(
                    () -> BusinessException.of(WISHLIST_NOT_FOUND)
            );
            if(user.getId() != wishlist.getUser().getId()){
                throw BusinessException.of(NO_PERMISSION_TO_DELETE);
            }
            wishlistRepository.deleteById(wishlistId);
            Tour tour = tourRepository.findById(wishlist.getItemId()).orElseThrow(
                    () -> BusinessException.of(TOUR_NOT_FOUND)
            );
            WishlistDTO wishlistDTO = WishlistDTO.builder()
                    .id(wishlist.getId())
                    .itemId(wishlist.getItemId())
                    .itemType(wishlist.getItemType())
                    .tourName(Optional.ofNullable(tour.getName()).orElseThrow(null))
                    .tourImageUrl(Optional.ofNullable(tour.getTourImages().get(0).getImageUrl()).orElseThrow(null))
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), DELETE_WISHLIST_SUCCESS, wishlistDTO);
        } catch (Exception ex) {
            throw BusinessException.of(DELETE_WISHLIST_FAIL, ex);
        }
    }

    private User getCurrentUser() {
        User user = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> BusinessException.of("Không tìm thấy người dùng"));
        }
        return user;
    }
}
