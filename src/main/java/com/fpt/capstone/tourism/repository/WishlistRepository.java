package com.fpt.capstone.tourism.repository;

import com.fpt.capstone.tourism.dto.common.WishlistDTO;
import com.fpt.capstone.tourism.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
//    List<Wishlist> findByUserId(Long id);

    @Query( value = """
                    SELECT\s
                     wl.id AS id,
                     wl.item_id AS itemId,
                     wl.item_type AS itemType,
                     t.name AS tourName
                     FROM wishlist wl
                     JOIN tour t ON t.id = wl.item_id
                     WHERE wl.user_id = :id
            """, nativeQuery = true)
    List<WishlistDTO> findByUserId(Long id);

    @Query("""
    SELECT wl FROM Wishlist wl
    WHERE wl.itemId = :itemId
    AND wl.user.id = :userId
""")
    Wishlist findByItemIdAndUserId(Long userId, Long itemId);

    @Query(value = """
    SELECT wl.id, wl.item_id, wl.item_type
    FROM wishlist wl
    WHERE wl.user_id = :userId
    """, nativeQuery = true)
    List<Object[]> findWishlistByUserId(@Param("userId") Long userId);

}


