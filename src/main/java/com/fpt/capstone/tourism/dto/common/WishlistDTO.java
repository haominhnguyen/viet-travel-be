package com.fpt.capstone.tourism.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDTO {
    private Long id;
    private Long itemId;
    private String itemType;
    private String tourName;
    private String tourImageUrl;
}
