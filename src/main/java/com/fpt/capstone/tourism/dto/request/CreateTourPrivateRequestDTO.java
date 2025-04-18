package com.fpt.capstone.tourism.dto.request;


import com.fpt.capstone.tourism.dto.common.LocationShortDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateTourPrivateRequestDTO {
    private String name;
    private int numberDays;
    private int numberNights;
    private Long departLocation;
    private List<LocationShortDTO> locations;
    private String highlights;
    private String note;
    private Long createdBy;
    private int pax;
    private List<String> tourImages;
}
