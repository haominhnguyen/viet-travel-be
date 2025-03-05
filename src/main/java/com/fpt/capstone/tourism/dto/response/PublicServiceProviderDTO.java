package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.GeoPositionDTO;
import com.fpt.capstone.tourism.model.GeoPosition;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.ServiceCategory;
import com.fpt.capstone.tourism.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PublicServiceProviderDTO {
    private Long id;
    private String imageUrl;
    private String name;
    private String abbreviation;
    private String website;
    private String email;
    private int star;
    private String phone;
    private String address;
    private PublicLocationDTO location;
    private GeoPositionDTO geoPosition;
    private Double minRoomPrice;
}
