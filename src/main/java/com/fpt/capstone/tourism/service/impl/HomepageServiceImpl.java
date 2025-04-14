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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomepageServiceImpl implements HomepageService {
    private final TourService tourService;
    private final BlogService blogService;
    //private final ActivityService activityService;
    private final ServiceProviderService providerService;
    private final LocationService locationService;
    private final ServiceService serviceService;
    private final ServiceRepository serviceRepository;
    //private final ActivityRepository activityRepository;
    private final TourRepository tourRepository;
    private final LocationRepository locationRepository;
    private final BlogRepository blogRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    //private final ActivityMapper activityMapper;
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
            List<PublicActivityDTO> recommendedActivities = serviceService.findRecommendedActivities(numberActivity);
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
    public GeneralResponse<PagingDTO<List<PublicTourDTO>>> viewAllTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, LocalDate fromDate, Long departLocationId, String sortByPrice) {
        return tourService.getAllPublicTour(page, size, keyword, budgetFrom, budgetTo, duration, fromDate, departLocationId, sortByPrice);
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
            log.info("Start find tour booking detail with ID: {}", id);
            Tour currentTour = tourRepository.findTourByTourId(id);
            log.info("Start find tour booking detail with ID: {}", id);
            List<Long> locationIds = currentTour.getLocations().stream().map(Location::getId).collect(Collectors.toList());
            List<PublicTourDTO> otherTour = tourService.findSameLocationPublicTour(locationIds);
            List<PublicTourScheduleDTO> tourScheduleBasicDTO = tourScheduleRepository.findTourScheduleBasicByTourId(id);

            //Mapping to DTO
            PublicTourDetailDTO tourBasicDTO = PublicTourDetailDTO.builder()
                    .id(currentTour.getId())
                    .name(currentTour.getName())
                    .highlights(currentTour.getHighlights())
                    .numberDays(currentTour.getNumberDays())
                    .numberNight(currentTour.getNumberNights())
                    .note(currentTour.getNote())
                    .privacy(currentTour.getPrivacy())
                    .locations(currentTour.getLocations().stream().map(locationMapper::toPublicLocationDTO).collect(Collectors.toList()))
                    .tags(currentTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                    .departLocation(locationMapper.toPublicLocationDTO(currentTour.getDepartLocation()))
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
            List<PublicActivityDTO> activities = serviceRepository.findRelatedActivities(id, "Activity", PageRequest.of(0, 6))
                    .stream().map(serviceMapper::toPublicActivityDTO).collect(Collectors.toList());

            //Find other locations
            List<PublicLocationDTO> publicLocations = locationService.findRecommendedLocations(6, id);

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
    public GeneralResponse<PublicHotelDetailDTO> viewPublicHotelDetail(Long serviceProviderId) {
        try {
            //Find service provider
            ServiceProvider serviceProvider = serviceProviderRepository.findById(serviceProviderId).orElseThrow(
                    () -> BusinessException.of("Service provider not found")
            );

            //Find list rooms of the service provider
            List<PublicServiceDTO> rooms = serviceRepository.findRoomsByProviderId(serviceProviderId);

            //Find list other hotel in the same location
            List<ServiceProvider> otherHotels = serviceProviderRepository
                    .findOtherHotelsInSameLocationByProviderId(serviceProviderId, serviceProvider.getLocation().getId());

            List<PublicServiceProviderDTO> otherHotelsDTO = otherHotels.stream()
                    .map(serviceProviderMapper::toPublicServiceProviderDTO).collect(Collectors.toList());
            //Mapping to Dto
            PublicHotelDetailDTO publicHotelDetailDTO = PublicHotelDetailDTO.builder()
                    .serviceProvider(serviceProviderMapper.toPublicServiceProviderDTO(serviceProvider))
                    .rooms(rooms)
                    .otherHotels(otherHotelsDTO)
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Hotel detail loaded successfully", publicHotelDetailDTO);
        } catch (Exception ex){
            throw BusinessException.of("Hotel detail loaded fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> search(String keyword) {
        try {
            String normalizedName = removeAccents(keyword.toLowerCase());
            List<Tour> tours = tourRepository.findAllPublicTour().stream()
                    .filter(t -> removeAccents(t.getName().toLowerCase()).contains(normalizedName)).collect(Collectors.toList());

            List<TourSearchDTO> results = tours.stream().map(tour -> {
                return TourSearchDTO.builder()
                       .id(tour.getId())
                       .name(tour.getName())
                        .tourImages(tour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).collect(Collectors.toList()))
                       .build();
            }).collect(Collectors.toList());

            return new GeneralResponse<>(HttpStatus.OK.value(), "Search successfully", results);
        } catch (Exception ex){
            throw BusinessException.of("Search fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> getListLocation() {
        try{
            List<Location> locations = locationRepository.findByDeletedFalse();
            List<PublicLocationSimpleDTO> publicLocations =
                    locations.stream().map(locationMapper::toPublicLocationSimpleDTO
                    ).collect(Collectors.toList());
            return new GeneralResponse<>(HttpStatus.OK.value(), "Thành công", publicLocations);
        }catch (Exception ex){
            throw BusinessException.of("Fail", ex);
        }
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        // Chuyển Đ -> D, đ -> d trước khi chuẩn hóa
        text = text.replace("Đ", "D").replace("đ", "d");
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);//Chuyển chữ có dấu thành ký tự gốc + dấu (ví dụ: Đà → Da + dấu huyền).
        Pattern pattern = Pattern.compile("\\p{M}"); //  Xóa tất cả các dấu khỏi ký tự.
        return pattern.matcher(normalized).replaceAll("");
    }

}
