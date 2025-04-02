package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class TourProcessDetailDTO {
    private Long id;
    private String name;
    private String highlights;
    private int numberDays;
    private int numberNights;
    private String note;
    private List<PublicLocationSimpleDTO> locations;
    private List<TagDTO> tags;
    private TourType tourType;
    private TourStatus tourStatus;
    private PublicLocationSimpleDTO departLocation;
    private String privacy;
    private String createdBy;
    private List<TourImageDTO> tourImages;
    private List<TourDayProcessDTO> tourDays;
}
