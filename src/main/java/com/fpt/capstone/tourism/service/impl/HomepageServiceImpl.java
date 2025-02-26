package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.repository.ActivityRepository;
import com.fpt.capstone.tourism.repository.TourRepository;
import com.fpt.capstone.tourism.repository.TourScheduleRepository;
import com.fpt.capstone.tourism.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomepageServiceImpl implements HomepageService {
    private final TourService tourService;
    private final BlogService blogService;
    private final ActivityService activityService;
    private final ServiceProviderService providerService;
    private final LocationService locationService;
    private final ActivityRepository activityRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final ActivityMapper activityMapper;
    private final LocationMapper locationMapper;
    private final TagMapper tagMapper;
    private final TourImageMapper tourImageMapper;
    private final TourDayMapper tourDayMapper;

    @Override
    public GeneralResponse<HomepageDTO> viewHomepage(int numberTour, int numberBlog, int numberActivity, int numberLocation) {
        try {
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
        } catch (Exception ex){
            throw BusinessException.of("Homepage loaded fail", ex);
        }

    }

    @Override
    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> viewAllHotel(int page, int size, String keyword) {
        return providerService.getAllHotel(page, size, keyword);
    }

//    @Override
//    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> viewAllRestaurant(int page, int size, String keyword) {
//        return providerService.getAllRestaurant(page, size, keyword);
//    }

    @Override
    public GeneralResponse<PagingDTO<List<TourDTO>>> viewAllTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate) {
        return tourService.getAllPublicTour(page, size, keyword, budgetFrom, budgetTo, duration, fromDate);
    }

//    @Override
//    public GeneralResponse<PublicActivityDetailDTO> viewPublicActivityDetail(Long activityId, int numberActivity) {
//        ActivityDTO activityDTO = activityMapper.toDTO(activityRepository.findById(activityId).orElseThrow());
//        List<ActivityDTO> relatedActivities = activityService.findRelatedActivities(activityId, numberActivity);
//
//        //Mapping to Dto
//        PublicActivityDetailDTO publicActivityDetailDTO = PublicActivityDetailDTO.builder()
//                .detailActivityDTO(activityDTO)
//                .relatedActivities(relatedActivities)
//                .build();
//
//        return new GeneralResponse<>(HttpStatus.OK.value(), "Activity detail loaded successfully", publicActivityDetailDTO);
//    }

    @Override
    public GeneralResponse<PublicTourDetailDTO> viewTourDetail(Long id) {
        try{
            Tour currentTour = tourRepository.findById(id).orElseThrow();
            List<Long> locationIds = currentTour.getLocations().stream().map(location -> location.getId()).collect(Collectors.toList());
            List<PublicTourDTO> otherTour = tourService.findSameLocationPublicTour(locationIds);
            List<PublicTourScheduleDTO> tourScheduleBasicDTO = tourScheduleRepository.findTourScheduleBasicByTourId(id);

            //Mapping to DTO
            PublicTourDetailDTO tourBasicDTO = PublicTourDetailDTO.builder()
                    .id(currentTour.getId())
                    .name(currentTour.getName())
                    .highlights(currentTour.getHighlights())
                    .numberDays(currentTour.getNumberDays())
                    .numberNight(currentTour.getNumberNight())
                    .note(currentTour.getNote())
                    .privacy(currentTour.getPrivacy())
                    .locations(currentTour.getLocations().stream().map(locationMapper::toPublicLocationDTO).collect(Collectors.toList()))
                    .tags(currentTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                    .depart_location(locationMapper.toPublicLocationDTO(currentTour.getDepart_location()))
                    .tourSchedules(tourScheduleBasicDTO)
                    .tourImages(currentTour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).collect(Collectors.toList()))
                    .tourDays(currentTour.getTourDays().stream().map(tourDayMapper::toPublicTourDayDTO).collect(Collectors.toList()))
                    .otherTours(otherTour)
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Tour detail loaded successfully", tourBasicDTO);
        } catch (Exception ex){
            throw BusinessException.of("Tour detail loaded fail", ex);
        }

    }

}
