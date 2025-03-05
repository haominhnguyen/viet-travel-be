package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.ServiceProvider;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    private final ServiceRepository serviceRepository;
    private final ActivityRepository activityRepository;
    private final TourRepository tourRepository;
    private final LocationRepository locationRepository;
    private final BlogRepository blogRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ActivityMapper activityMapper;
    private final LocationMapper locationMapper;
    private final BlogMapper blogMapper;
    private final ServiceProviderMapper serviceProviderMapper;
    private final ServiceMapper serviceMapper;
    private final TagMapper tagMapper;
    private final TourImageMapper tourImageMapper;
    private final TourDayMapper tourDayMapper;

    @Override
    public GeneralResponse<HomepageDTO> viewHomepage(int numberTour, int numberBlog, int numberActivity, int numberLocation) {
        try {
            PublicTourDTO topTourOfYear = tourService.findTopTourOfYear();
            List<PublicTourDTO> trendingTours = tourService.findTrendingTours(numberTour);
            List<BlogResponseDTO> newBlogs = blogService.findNewestBlogs(numberBlog);
            List<ActivityDTO> recommendedActivities = activityService.findRecommendedActivities(numberActivity);
            List<PublicLocationDTO> recommendedLocations = locationService.findRecommendedLocations(numberLocation);

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
    public GeneralResponse<PagingDTO<List<PublicServiceProviderDTO>>> viewAllHotel(int page, int size, String keyword, Integer star) {
        return providerService.getAllHotel(page, size, keyword, star);
    }

//    @Override
//    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> viewAllRestaurant(int page, int size, String keyword) {
//        return providerService.getAllRestaurant(page, size, keyword);
//    }

    @Override
    public GeneralResponse<PagingDTO<List<PublicTourDTO>>> viewAllTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, Date fromDate) {
        return tourService.getAllPublicTour(page, size, keyword, budgetFrom, budgetTo, duration, fromDate);
    }

//    @Override
//    public GeneralResponse<PublicActivityDetailDTO> viewPublicActivityDetail(Long activityId, int numberActivity) {
//        ActivityDTO activityDTO = activityMapper.toEntity(activityRepository.findById(activityId).orElseThrow());
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

    @Override
    public GeneralResponse<PublicLocationDetailDTO> viewPublicLocationDetail(Long id) {
        try {
            //Find location
            Location location = locationRepository.findById(id).orElseThrow();

            //Find tour in the same location
            List<PublicTourDTO> tours = tourService.findSameLocationPublicTour(Collections.singletonList(id));

            //Find blog related to the location
            List<BlogResponseDTO> blogs = blogRepository.findBlogRelatedLocations(location.getName())
                    .stream().map(blogMapper::toDTO).collect(Collectors.toList())
                     ;

            //Find activities related to the location
            List<PublicActivityDTO> activities = activityRepository.findRelatedActivities(id, 6)
                    .stream().map(activityMapper::toPublicActivityDTO).collect(Collectors.toList());

            //Find other locations
            List<PublicLocationDTO> publicLocations = locationService.findRecommendedLocations(6);

            //Find hotel related to the location
            List<PublicServiceProviderDTO> hotels = serviceProviderRepository.getHotelByLocationId(id)
                    .stream().map(serviceProviderMapper::toPublicServiceProviderDTO).collect(Collectors.toList());


            //Mapping to Dto
            PublicLocationDetailDTO publicLocationDetailDTO = PublicLocationDetailDTO.builder()
                    .id(id)
                    .name(location.getName())
                    .description(location.getDescription())
                    .image(location.getImage())
                    .tours(tours)
                    .blogs(blogs)
                    .activities(activities)
                    .locations(publicLocations)
                    .hotels(hotels)
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Location detail loaded successfully", publicLocationDetailDTO);
        } catch (Exception ex){
            throw BusinessException.of("Location detail loaded fail", ex);
        }
    }

    @Override
    public GeneralResponse<PublicHotelDetailDTO> viewPublicHotelDetail(Long id) {
        try {
            //Find service provider
            ServiceProvider serviceProvider = serviceProviderRepository.findById(id).orElseThrow();

            //Find list rooms of the service provider
            List<PublicServiceDTO> rooms = serviceRepository.findRoomsByProviderId(id)
                    .stream().map(serviceMapper::toPublicServiceDTO).collect(Collectors.toList());;

            //Find list other services of the service provider
            List<PublicServiceDTO> otherServices = serviceRepository.findOtherServicesByProviderId(id)
                    .stream().map(serviceMapper::toPublicServiceDTO).collect(Collectors.toList());

            //Mapping to Dto
            PublicHotelDetailDTO publicHotelDetailDTO = PublicHotelDetailDTO.builder()
                    .serviceProvider(serviceProviderMapper.toPublicServiceProviderDTO(serviceProvider))
                    .rooms(rooms)
                    .otherServices(otherServices)
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Hotel detail loaded successfully", publicHotelDetailDTO);
        } catch (Exception ex){
            throw BusinessException.of("Hotel detail loaded fail", ex);
        }
    }

}
