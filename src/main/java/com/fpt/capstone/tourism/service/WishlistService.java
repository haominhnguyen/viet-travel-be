package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;

public interface WishlistService {
    GeneralResponse<?> getUserListWishlist();

    GeneralResponse<?> addWishlist(Long itemId);

    GeneralResponse<?> deleteWishlist(Long wishlistId);
}
