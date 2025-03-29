package com.fpt.capstone.tourism.dto.response;


import com.fpt.capstone.tourism.model.Service;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PublicHotelDetailDTO {
    PublicServiceProviderDTO serviceProvider;
    List<PublicServiceDTO> rooms;
    List<PublicServiceProviderDTO> otherHotels;
}
