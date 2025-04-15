package com.fpt.capstone.tourism.mapper.custom;

import com.fpt.capstone.tourism.dto.response.PublicTourImageDTO;
import com.fpt.capstone.tourism.mapper.TourImageMapper;
import com.fpt.capstone.tourism.model.TourImage;
import lombok.RequiredArgsConstructor;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;

@Named("TourImageTranslator")
@RequiredArgsConstructor
@Component
public class TourImageCustom {

    private final TourImageMapper mapper;

    @Named("mapFirstImage")
    public  PublicTourImageDTO mapFirstImage(List<TourImage> tourImages) {
        if(!tourImages.isEmpty()) {
            return mapper.toPublicTourImageDTO(tourImages.get(0));

        } else {
            return null;
        }
    }
}
