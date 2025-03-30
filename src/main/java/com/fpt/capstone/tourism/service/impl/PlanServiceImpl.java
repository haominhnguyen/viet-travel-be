package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.PlanHelper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.repository.LocationRepository;
import com.fpt.capstone.tourism.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final LocationRepository locationRepository;

    private final LocationMapper locationMapper;

    private final PlanHelper planHelper;



    @Override
    public GeneralResponse<?> getLocations() {
        try {
            List<Location> locations = locationRepository.findRandomLocation(6);
            return GeneralResponse.of(locations.stream().map(locationMapper::toPublicLocationDTO).toList());
        } catch (Exception ex) {
            throw BusinessException.of("Cannot Locations for plan", ex);
        }
    }

    @Override
    public GeneralResponse<?> getLocations(String name) {
        try {
            String normalizedName = removeAccents(name).toLowerCase();
            List<Location> locations = locationRepository.findAll(planHelper.searchLocationByName(normalizedName));
            return GeneralResponse.of(locations.stream().map(locationMapper::toPublicLocationDTO).toList());
        } catch (Exception ex) {
            throw BusinessException.of("Cannot Get All Locations for plan", ex);
        }
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);//Chuyển chữ có dấu thành ký tự gốc + dấu (ví dụ: Đà → Da + dấu huyền).
        Pattern pattern = Pattern.compile("\\p{M}"); //  Xóa tất cả các dấu khỏi ký tự.
        return pattern.matcher(normalized).replaceAll("");
    }
}
