package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.TourBasicDTO;
import com.fpt.capstone.tourism.mapper.ActivityMapper;
import com.fpt.capstone.tourism.repository.ActivityRepository;
import com.fpt.capstone.tourism.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomepageServiceImpl implements HomepageService {
    private final TourService tourService;
    private final BlogService blogService;
    private final ActivityService activityService;
    private final ServiceProviderService providerService;
    private final LocationService locationService;
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;

    @Override
    public GeneralResponse<HomepageDTO> viewHomepage(int numberTour, int numberBlog, int numberActivity, int numberLocation) {
        TourDTO topTourOfYear = tourService.findTopTourOfYear();
        List<TourDTO> trendingTours = tourService.findTrendingTours(numberTour);
        List<BlogResponseDTO> newBlogs = blogService.findNewestBlogs(numberBlog);
        List<ActivityDTO> recommendedActivities = activityService.findRecommendedActivities(numberActivity);
        List<LocationDTO> recommendedLocations = locationService.findRecommendedLocations(numberLocation);

        //Mapping to Dto
        HomepageDTO homepageDTO = HomepageDTO.builder()
                .topTourOfYear(topTourOfYear)
                .newBlogs(newBlogs)
                .trendingTours(trendingTours)
                .recommendedActivities(recommendedActivities)
                .recommendedLocations(recommendedLocations)
                .build();
        return new GeneralResponse<>(HttpStatus.OK.value(), "Homepage loaded successfully", homepageDTO);
    }

    @Override
    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> viewAllHotel(int page, int size, String keyword) {
        return providerService.getAllHotel(page, size, keyword);
    }

    @Override
    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> viewAllRestaurant(int page, int size, String keyword) {
        return providerService.getAllRestaurant(page, size, keyword);
    }

    @Override
    public GeneralResponse<PagingDTO<List<TourDTO>>> viewAllTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate) {
        return tourService.getAllPublicTour(page, size, keyword, budgetFrom, budgetTo, duration, fromDate);
    }

    @Override
    public GeneralResponse<PublicActivityDetailDTO> viewPublicActivityDetail(Long activityId, int numberActivity) {
        ActivityDTO activityDTO = activityMapper.toDTO(activityRepository.findById(activityId).orElseThrow());
        List<ActivityDTO> relatedActivities = activityService.findRelatedActivities(activityId, numberActivity);

        //Mapping to Dto
        PublicActivityDetailDTO publicActivityDetailDTO = PublicActivityDetailDTO.builder()
                .detailActivityDTO(activityDTO)
                .relatedActivities(relatedActivities)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "Activity detail loaded successfully", publicActivityDetailDTO);
    }

}
