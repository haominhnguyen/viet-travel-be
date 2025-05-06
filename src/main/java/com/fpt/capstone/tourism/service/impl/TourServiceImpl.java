package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.TourRequestDTO;
import com.fpt.capstone.tourism.dto.response.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.*;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.TourService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final LocationMapper locationMapper;
    private final TourImageMapper tourImageMapper;
    private final TourMapper tourMapper;
    private final TourImageRepository tourImageRepository;
    private final TagRepository tagRepository;
    private final TagMapper tagMapper;
    private final TourDayMapper tourDayMapper;
    private final LocationRepository locationRepository;
    private final TourBookingRepository tourBookingRepository;
    private final CostAccountRepository costAccountRepository;
    private final UserRepository userRepository;
    private final TourDayRepository tourDayRepository;
    private final TourPaxRepository tourPaxRepository;

    @Override
    public PublicTourDTO findTopTourOfYear() {
        try {
            List<Long> topTourIds = tourRepository.findTopTourIdsOfCurrentYear();

            if (topTourIds.isEmpty()) {
                Tour tempTour = tourRepository.findNewestTour();
                return PublicTourDTO.builder()
                        .id(tempTour.getId())
                        .name(tempTour.getName())
                        .numberNight(tempTour.getNumberNights())
                        .numberDays(tempTour.getNumberDays())
                        .tags(tempTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                        .departLocation(locationMapper.toPublicLocationDTO(tempTour.getDepartLocation()))
                        .tourImages(tempTour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).collect(Collectors.toList()))
                        .priceFrom(tourRepository.findMinSellingPriceForTours(tempTour.getId()))
                        .build();
            }

            Long topTourId = topTourIds.get(0);

            // Fetch and convert the tour to DTO
            Tour topTour = tourRepository.findById(topTourIds.get(0)).orElseThrow();
            return PublicTourDTO.builder()
                    .id(topTour.getId())
                    .name(topTour.getName())
                    .numberNight(topTour.getNumberNights())
                    .numberDays(topTour.getNumberDays())
                    .tags(topTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                    .departLocation(locationMapper.toPublicLocationDTO(topTour.getDepartLocation()))
                    .tourImages(topTour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).collect(Collectors.toList()))
                    .priceFrom(tourRepository.findMinSellingPriceForTours(topTour.getId()))
                    .build();
        } catch (Exception ex) {
            throw BusinessException.of("Lỗi khi lấy tour hàng đầu trong năm", ex);
        }
    }

    @Override
    public List<PublicTourDTO> findTrendingTours(int numberTour) {
        try {
            Pageable pageable = PageRequest.of(0, numberTour);
            List<Long> trendingTourIds = tourRepository.findTrendingTourIds();

            // Lấy danh sách các tour từ database theo danh sách ID
            List<Tour> trendingTours = tourRepository.findPublicTourByIds(trendingTourIds, pageable);

            // Lấy giá thấp nhất từ bảng TourPax
            Map<Long, Double> priceMap = tourRepository.findMinSellingPrices(trendingTourIds)
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // tourId
                            row -> (Double) row[1] // priceFrom
                    ));


            // Fetch all tours by their IDs and convert to DTOs
            return trendingTours.stream()
                    .map(tour -> new PublicTourDTO(
                            tour.getId(),
                            tour.getName(),
                            tour.getNumberDays(),
                            tour.getNumberNights(),
                            tour.getTags().stream().map(tagMapper::toDTO).toList(),  // Convert tags
                            locationMapper.toPublicLocationDTO(tour.getDepartLocation()),  // Convert depart location
                            tourScheduleRepository.findTourScheduleBasicByTourId(tour.getId()),
                            tour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).toList(), // Convert images
                            priceMap.getOrDefault(tour.getId(), 0.0) // Giá thấp nhất
                    ))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw BusinessException.of(ERROR_RETRIEVING_TRENDING_TOURS, ex);
        }

    }

    @Override
    public GeneralResponse<PagingDTO<List<PublicTourDTO>>> getAllPublicTour(int page, int size, String keyword, Double budgetFrom, Double budgetTo, Integer duration, LocalDate fromDate, Long departLocationId, String sortByPrice) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<Tour> spec = buildSearchSpecification(keyword, budgetFrom, budgetTo, duration, fromDate, departLocationId);

            //Find TOur satisfying conditions
            Page<Tour> tourPage = tourRepository.findAll(spec, pageable);

            //Find min price of each tour
            Map<Long, Double> minPriceMap = tourRepository.findMinSellingPrices(tourPage.getContent().stream().map(Tour::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // tourId
                            row -> (Double) row[1] // priceFrom
                    ));
            ;

            List<PublicTourDTO> publicTourDTOS = tourPage.getContent().stream()
                    .map(tour -> new PublicTourDTO(
                            tour.getId(),
                            tour.getName(),
                            tour.getNumberDays(),
                            tour.getNumberNights(),
                            tour.getTags().stream().map(tagMapper::toDTO).toList(),
                            locationMapper.toPublicLocationDTO(tour.getDepartLocation()),
                            tourScheduleRepository.findTourScheduleBasicByTourId(tour.getId()),
                            tour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).toList(),
                            minPriceMap.getOrDefault(tour.getId(), 0.0)  // Giá thấp nhất
                    ))
                    .collect(Collectors.toList());

            //Sort by min Price
            if ("asc".equalsIgnoreCase(sortByPrice)) {
                publicTourDTOS.sort(Comparator.comparing(PublicTourDTO::getPriceFrom));
            } else if ("desc".equalsIgnoreCase(sortByPrice)) {
                publicTourDTOS.sort(Comparator.comparing(PublicTourDTO::getPriceFrom).reversed());
            }

            return buildPagedResponse(tourPage, publicTourDTOS);
        } catch (Exception ex) {
            throw BusinessException.of(GET_ALL_PUBLIC_TOUR_FAIL, ex);
        }
    }

    @Override
    public List<PublicTourDTO> findSameLocationPublicTour(List<Long> locationIds) {
        try {
            List<PublicTourDTO> publicTourDTOS = new ArrayList<>();
            //Get list id of list same location tour
            List<Long> tourIds = tourRepository.findSameLocationTourIds(locationIds);
            for (Long tourId : tourIds) {
                //Get tour information
                Tour tour = tourRepository.findByIdAndTourStatusAndTourType(tourId, TourStatus.OPENED, TourType.SIC);


                List<Tag> tagEntities = tagRepository.findTagsByTourId(tourId);
                List<TagDTO> tags = new ArrayList<>();

                if (!tagEntities.isEmpty()) {
                    tags = tagEntities.stream().map(tagMapper::toDTO).toList();
                }


                //Get min price for each tour
                Double minPrice = tourRepository.findMinSellingPriceForTours(tourId);

                // Get list image for each tour
                List<PublicTourImageDTO> images = tourImageRepository.findTourImagesByTourId(tourId)
                        .stream().map(tourImageMapper::toPublicTourImageDTO).toList();
                ;


                PublicTourDTO publicTourDTO = PublicTourDTO.builder()
                        .id(tourId)
                        .name(tour.getName())
                        .numberDays(tour.getNumberDays())
                        .numberNight(tour.getNumberNights())
                        .tags(tags)
                        .departLocation(locationMapper.toPublicLocationDTO(tour.getDepartLocation()))
                        .tourImages(images)
                        .priceFrom(minPrice)
                        .build();

                publicTourDTOS.add(publicTourDTO);
            }
            return publicTourDTOS;
        } catch (Exception ex) {
            throw BusinessException.of(ERROR_RETRIEVING_SAME_LOCATION_PUBLIC_TOURS, ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<TourBasicDTO>>> getAllTours(String keyword, Boolean isDeleted, Boolean isOpened, Pageable pageable) {
        Specification<Tour> spec = buildSimpleSearchSpecification(keyword, isDeleted, isOpened);
        Page<Tour> tourPage = tourRepository.findAll(spec, pageable);
        List<TourBasicDTO> tourDTOs = tourPage.getContent().stream()
                .map(this::convertToTourBasicDTO)
                .collect(Collectors.toList());
        return buildSimplePagedResponse(tourPage, tourDTOs);
    }

    @Override
    public GeneralResponse<TourDetailDTO> getTourDetail(Long id) {
        try {
            Tour currentTour = tourRepository.findById(id).orElseThrow();
            List<Long> locationIds = currentTour.getLocations().stream().map(location -> location.getId()).collect(Collectors.toList());
            List<PublicTourScheduleDTO> tourScheduleBasicDTO = tourScheduleRepository.findTourScheduleBasicByTourId(id);

            UserBasicDTO createdByDTO = null;
            if (currentTour.getCreatedBy() != null) {
                createdByDTO = UserBasicDTO.builder()
                        .id(currentTour.getCreatedBy().getId())
                        .username(currentTour.getCreatedBy().getUsername())
                        .fullName(currentTour.getCreatedBy().getFullName())
                        .email(currentTour.getCreatedBy().getEmail())
                        .build();
            }

            // Get the tour type as a string
            String tourTypeStr = currentTour.getTourType() != null ? currentTour.getTourType().name() : null;

            // Mapping to DTO
            TourDetailDTO tourBasicDTO = TourDetailDTO.builder()
                    .id(currentTour.getId())
                    .name(currentTour.getName())
                    .highlights(currentTour.getHighlights())
                    .numberDays(currentTour.getNumberDays())
                    .numberNight(currentTour.getNumberNights())
                    .note(currentTour.getNote())
                    .privacy(currentTour.getPrivacy())
                    .tourType(tourTypeStr) // Include the tour type
                    .locations(currentTour.getLocations().stream().map(locationMapper::toPublicLocationDTO).collect(Collectors.toList()))
                    .tags(currentTour.getTags().stream().map(tagMapper::toDTO).collect(Collectors.toList()))
                    .departLocation(locationMapper.toPublicLocationDTO(currentTour.getDepartLocation()))
                    .tourSchedules(tourScheduleBasicDTO)
                    .tourImages(currentTour.getTourImages().stream().map(tourImageMapper::toPublicTourImageDTO).collect(Collectors.toList()))
                    .tourDays(currentTour.getTourDays().stream().map(tourDayMapper::toPublicTourDayDTO).collect(Collectors.toList()))
                    .createdAt(currentTour.getCreatedAt())
                    .updatedAt(currentTour.getUpdatedAt())
                    .createdBy(createdByDTO)
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_DETAIL_LOAD_SUCCESS, tourBasicDTO);
        } catch (Exception ex) {
            throw BusinessException.of(TOUR_DETAIL_LOAD_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourResponseDTO> createTour(TourRequestDTO tourRequestDTO, User currentUser) {
        try {
            // Validate input
            Validator.validateTourRequest(tourRequestDTO);

            // Create tour entity
            Tour tour = new Tour();
            tour.setName(tourRequestDTO.getName());
            tour.setHighlights(tourRequestDTO.getHighlights());
            tour.setNumberDays(tourRequestDTO.getNumberDays());
            tour.setNumberNights(tourRequestDTO.getNumberNights());
            tour.setNote(tourRequestDTO.getNote());
            tour.setDeleted(false);

            // Set locations
            List<Location> locations = locationRepository.findAllById(tourRequestDTO.getLocationIds());
            tour.setLocations(locations);

            // Set tags
            if (tourRequestDTO.getTagIds() != null && !tourRequestDTO.getTagIds().isEmpty()) {
                List<Tag> tags = tagRepository.findAllById(tourRequestDTO.getTagIds());
                tour.setTags(tags);
            } else {
                tour.setTags(new ArrayList<>());
            }

            // Set tour type
            tour.setTourType(TourType.valueOf(tourRequestDTO.getTourType()));

            // Always set tour status to DRAFT when creating a new tour
            tour.setTourStatus(TourStatus.DRAFT);

            // Set departure location
            Location departLocation = locationRepository.findById(tourRequestDTO.getDepartLocationId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, DEPART_LOCATION_NOT_FOUND));
            tour.setDepartLocation(departLocation);

            // Set markup percent and privacy
            tour.setMarkUpPercent(tourRequestDTO.getMarkUpPercent());
            tour.setPrivacy(tourRequestDTO.getPrivacy());

            // Set created by - using the user passed from controller
            tour.setCreatedBy(currentUser);

            // Save tour first to get ID
            Tour savedTour = tourRepository.save(tour);

            // Create and save tour images
            if (tourRequestDTO.getTourImages() != null && !tourRequestDTO.getTourImages().isEmpty()) {
                List<TourImage> tourImages = tourRequestDTO.getTourImages().stream()
                        .map(imageDTO -> {
                            TourImage tourImage = new TourImage();
                            tourImage.setImageUrl(imageDTO.getImageUrl());
                            tourImage.setDeleted(false);
                            tourImage.setTour(savedTour);
                            return tourImage;
                        })
                        .collect(Collectors.toList());
                tourImageRepository.saveAll(tourImages);
                savedTour.setTourImages(tourImages);
            } else {
                savedTour.setTourImages(new ArrayList<>());
            }
            // Map to response DTO
            TourResponseDTO tourResponseDTO = mapToTourResponseDTO(savedTour);

            return new GeneralResponse<>(HttpStatus.CREATED.value(), TOUR_CREATE_SUCCESS, tourResponseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(TOUR_CREATE_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourResponseDTO> updateTour(Long id, TourRequestDTO tourRequestDTO, User currentUser) {
        try {
            // Validate input
            Validator.validateTourRequest(tourRequestDTO);

            // Get existing tour
            Tour existingTour = tourRepository.findById(id)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            if (Boolean.TRUE.equals(existingTour.getDeleted())) {
                throw BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND);
            }

            // Check if tour status is DRAFT or REJECTED, otherwise do not allow update
            if (existingTour.getTourStatus() != TourStatus.DRAFT && existingTour.getTourStatus() != TourStatus.REJECTED) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, ONLY_DRAFT_OR_REJECTED_CAN_BE_UPDATED);
            }

            // Update tour entity
            existingTour.setName(tourRequestDTO.getName());
            existingTour.setHighlights(tourRequestDTO.getHighlights());
            existingTour.setNumberDays(tourRequestDTO.getNumberDays());
            existingTour.setNumberNights(tourRequestDTO.getNumberNights());
            existingTour.setNote(tourRequestDTO.getNote());

            // Update locations
            List<Location> locations = locationRepository.findAllById(tourRequestDTO.getLocationIds());
            existingTour.setLocations(locations);

            // Update tags
            if (tourRequestDTO.getTagIds() != null && !tourRequestDTO.getTagIds().isEmpty()) {
                List<Tag> tags = tagRepository.findAllById(tourRequestDTO.getTagIds());
                existingTour.setTags(tags);
            } else {
                existingTour.setTags(new ArrayList<>());
            }

            // Update tour type
            existingTour.setTourType(TourType.valueOf(tourRequestDTO.getTourType()));

            // Update tour status if provided (but still ensure it's a valid status transition)
            TourStatus requestedStatus = TourStatus.valueOf(tourRequestDTO.getTourStatus());
            existingTour.setTourStatus(requestedStatus);

            // Update departure location
            Location departLocation = locationRepository.findById(tourRequestDTO.getDepartLocationId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, DEPART_LOCATION_NOT_FOUND));
            existingTour.setDepartLocation(departLocation);

            // Update markup percent and privacy
            existingTour.setMarkUpPercent(tourRequestDTO.getMarkUpPercent());
            existingTour.setPrivacy(tourRequestDTO.getPrivacy());

            // Save updated tour
            Tour updatedTour = tourRepository.save(existingTour);

            // Update tour images (mark existing as deleted and add new ones)
            if (tourRequestDTO.getTourImages() != null && !tourRequestDTO.getTourImages().isEmpty()) {
                // Mark existing images as deleted
                updatedTour.getTourImages().forEach(image -> image.setDeleted(true));

                // Add new images
                List<TourImage> newTourImages = tourRequestDTO.getTourImages().stream()
                        .map(imageDTO -> {
                            TourImage tourImage = new TourImage();
                            tourImage.setImageUrl(imageDTO.getImageUrl());
                            tourImage.setDeleted(false);
                            tourImage.setTour(updatedTour);
                            return tourImage;
                        })
                        .collect(Collectors.toList());
                updatedTour.getTourImages().addAll(newTourImages);
            }
            // Save tour with updated relationships
            Tour completeTour = tourRepository.save(updatedTour);
            // Map to response DTO
            TourResponseDTO tourResponseDTO = mapToTourResponseDTO(completeTour);
            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_UPDATE_SUCCESS, tourResponseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(TOUR_UPDATE_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourResponseDTO> updateTourMarkupPercentage(Long tourId, Double markUpPercent) {
        try {
            if (markUpPercent == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, MARK_UP_REQUIRED);
            }

            // Check if the value is a valid number (not NaN or Infinity)
            if (Double.isNaN(markUpPercent) || Double.isInfinite(markUpPercent)) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, MARK_UP_MUST_BE_NUMBER);
            }

            // Check range
            if (markUpPercent < 0) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, MARK_UP_POSITIVE);
            }

            if (markUpPercent > 100) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, MARK_UP_LIMIT);
            }
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " id: " + tourId));

            // 2. Update markup percentage only, without calculating any prices
            tour.setMarkUpPercent(markUpPercent);

            // 3. Save the updated tour
            tour = tourRepository.save(tour);

            // 4. Map to TourResponseDTO using the specified function
            TourResponseDTO tourResponseDTO = mapToTourResponseDTO(tour);

            return new GeneralResponse<>(HttpStatus.OK.value(), MARKUP_UPDATE_SUCCESS, tourResponseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, MARKUP_UPDATE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<TourDetailDTO> getTourWithActiveSchedule(Long id) {
        try {
            // Find the tour
            Tour currentTour = tourRepository.findById(id)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            // Get only active tour schedules
            List<TourSchedule> activeSchedules = tourScheduleRepository.findActiveTourSchedulesByTourId(id);

            // Map entities to DTOs
            List<PublicTourScheduleDTO> schedulesDTOs = activeSchedules.stream()
                    .map(schedule -> {
                        TourPax pax = schedule.getTourPax();
                        // Get booked seats count
                        Integer bookedSeats = tourBookingRepository.countByTourScheduleIdAndStatusNot(schedule.getId());
                        if (bookedSeats == null) {
                            bookedSeats = 0;
                        }
                        // Calculate available seats
                        Integer availableSeats = pax.getMaxPax() - bookedSeats;

                        // Create and return the DTO
                        return PublicTourScheduleDTO.builder()
                                .scheduleId(schedule.getId())
                                .startDate(schedule.getStartDate())
                                .endDate(schedule.getEndDate())
                                .sellingPrice(pax.getSellingPrice())
                                .minPax(pax.getMinPax())
                                .maxPax(pax.getMaxPax())
                                .availableSeats(availableSeats)
                                .meetingLocation(schedule.getMeetingLocation())
                                .departureTime(schedule.getDepartureTime())
                                .extraHotelCost(pax.getExtraHotelCost())
                                .build();
                    })
                    .collect(Collectors.toList());

            // Build created by DTO
            UserBasicDTO createdByDTO = null;
            if (currentTour.getCreatedBy() != null) {
                createdByDTO = UserBasicDTO.builder()
                        .id(currentTour.getCreatedBy().getId())
                        .username(currentTour.getCreatedBy().getUsername())
                        .fullName(currentTour.getCreatedBy().getFullName())
                        .email(currentTour.getCreatedBy().getEmail())
                        .build();
            }

            // Get the tour type as a string
            String tourTypeStr = currentTour.getTourType() != null ? currentTour.getTourType().name() : null;

            // Build the complete DTO
            TourDetailDTO tourBasicDTO = TourDetailDTO.builder()
                    .id(currentTour.getId())
                    .name(currentTour.getName())
                    .highlights(currentTour.getHighlights())
                    .numberDays(currentTour.getNumberDays())
                    .numberNight(currentTour.getNumberNights())
                    .note(currentTour.getNote())
                    .privacy(currentTour.getPrivacy())
                    .tourType(tourTypeStr)
                    .locations(currentTour.getLocations().stream()
                            .map(locationMapper::toPublicLocationDTO)
                            .collect(Collectors.toList()))
                    .tags(currentTour.getTags().stream()
                            .map(tagMapper::toDTO)
                            .collect(Collectors.toList()))
                    .departLocation(locationMapper.toPublicLocationDTO(currentTour.getDepartLocation()))
                    .tourSchedules(schedulesDTOs) // Use our filtered and mapped schedules
                    .tourImages(currentTour.getTourImages().stream()
                            .map(tourImageMapper::toPublicTourImageDTO)
                            .collect(Collectors.toList()))
                    .tourDays(currentTour.getTourDays().stream()
                            .map(tourDayMapper::toPublicTourDayDTO)
                            .collect(Collectors.toList()))
                    .createdAt(currentTour.getCreatedAt())
                    .updatedAt(currentTour.getUpdatedAt())
                    .createdBy(createdByDTO)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_DETAIL_LOAD_SUCCESS, tourBasicDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(TOUR_DETAIL_LOAD_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<TourProcessDTO>>> getAllTourNeedToProcess(int page, int size, String keyword, TourStatus tourStatus, String orderDate) {
        try {
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<Tour> spec = buildSearchSpecificationAdmin(keyword, tourStatus);


            Page<Tour> tourPage = tourRepository.findAll(spec, pageable);

            // Map to DTO
            List<TourProcessDTO> resultDTO = tourPage.getContent().stream()
                    .map(tourMapper::toTourProcessDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(tourPage, resultDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<?> getDetailTourNeedToProcess(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow(
                    () -> BusinessException.of(TOUR_NOT_FOUND)
            );
            //Check tour id
            TourStatus status = tour.getTourStatus();
            List<TourStatus> statuses = new ArrayList<>();
            statuses.add(TourStatus.PENDING);
            statuses.add(TourStatus.APPROVED);
            statuses.add(TourStatus.REJECTED);
            if (!statuses.contains(status)) {
                throw BusinessException.of(TOUR_STATUS_NOT_VALID);
            }
            TourProcessDetailDTO resultDTO = tourMapper.toTourProcessDetailDTO(tour);

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_DETAIL_TOUR_SUCCESS, resultDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getDetailTourDay(Long tourId, Long tourDayId) {
        try {
            TourDay tourDay = tourDayRepository.findById(tourDayId).orElseThrow(
                    () -> BusinessException.of(TOUR_DAY_NOT_FOUND)
            );

            //Check tourDay belong to tour or not
            if (!tourDay.getTour().getId().equals(tourId)) {
                throw BusinessException.of(TOUR_DAY_NOT_BELONG);
            }
            TourDayProcessDetailDTO resultDTO = tourDayMapper.toTourDayProcessDetailDTO(tourDay);

            return new GeneralResponse<>(HttpStatus.OK.value(), GET_DETAIL_TOUR_DAY_SUCCESS, resultDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> approveTourProcess(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow(
                    () -> BusinessException.of(TOUR_NOT_FOUND)
            );

            //check status of tour before approval
            if (!tour.getTourStatus().equals(TourStatus.PENDING)) {
                throw BusinessException.of(TOUR_STATUS_NOT_PENDING);
            }

            tour.setTourStatus(TourStatus.APPROVED);
            tourRepository.save(tour);
            TourProcessDetailDTO resultDTO = tourMapper.toTourProcessDetailDTO(tour);

            return new GeneralResponse<>(HttpStatus.OK.value(), APPROVE_SUCCESS, resultDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> rejectTourProcess(Long tourId) {
        try {
            Tour tour = tourRepository.findById(tourId).orElseThrow(
                    () -> BusinessException.of(TOUR_NOT_FOUND)
            );

            //check status of tour before reject
            if (!tour.getTourStatus().equals(TourStatus.PENDING)) {
                throw BusinessException.of(TOUR_STATUS_NOT_PENDING);
            }

            tour.setTourStatus(TourStatus.REJECTED);
            tourRepository.save(tour);
            TourProcessDetailDTO resultDTO = tourMapper.toTourProcessDetailDTO(tour);

            return new GeneralResponse<>(HttpStatus.OK.value(), REJECT_SUCCESS, resultDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(GENERAL_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> viewDashboard(LocalDate fromDate, LocalDate toDate) {
        try {
            LocalDate now = LocalDate.now();
            //Phần này dùng chung cho các tiêu chí
            LocalDate startDate = now.minusMonths(11).withDayOfMonth(1); // Lấy ngày đầu tiên của tháng 12 tháng trước
            LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth()); // Ngày cuối cùng của tháng hiện tại

            //Phần riêng cho tính doanh thu
            LocalDate revenueStartDate;
            LocalDate revenueEndDate;
            if (fromDate == null && toDate == null) {
                revenueStartDate = startDate;
                revenueEndDate = endDate;
            } else {
                revenueStartDate = (fromDate != null) ? fromDate
                        : toDate.minusMonths(11).withDayOfMonth(1);

                revenueEndDate = (toDate != null) ? toDate : endDate;
            }

            //Tính doanh thu từng tháng (12 tháng gần nhất)
            List<TransactionType> transactionTypes = new ArrayList<>();
            transactionTypes.add(TransactionType.RECEIPT);
            transactionTypes.add(TransactionType.COLLECTION);
            List<RevenueChartDTO> monthlyRevenue = costAccountRepository.getRevenueByMonth(
                    revenueStartDate,
                    revenueEndDate,
                    transactionTypes,
                    CostAccountStatus.PAID);

            //Tính số lượng tài khoản mới từng tháng (12 tháng gần nhất)
            List<NewUsersChartDTO> monthlyNewUsers = userRepository.getNewUserByMonth(startDate, endDate);

            //Tính tỉ lệ tour SIC với Private từng tháng (12 tháng gần nhất)
            List<TourTypeRatioDTO> tourTypeRatios = tourBookingRepository.getTourTypeRatioByMonth(
                    startDate,
                    endDate,
                    TourBookingStatus.SUCCESS);

            //Tìm danh sách booking gần đây
            Pageable pageable = PageRequest.of(0, 10);
            List<RecentBookingDTO> recentBookings = tourBookingRepository.getRecentBooking(startDate, endDate, pageable);

            //Tìm top tour có doanh thu cao nhất (12 tháng gần nhất)
            pageable = PageRequest.of(0, 20);
            List<TopRevenueTourDTO> topRevenueTours = tourRepository.getTopRevenueTourByMonth(
                    startDate,
                    endDate,
                    transactionTypes,
                    CostAccountStatus.PAID,
                    pageable);

            //Tìm số lượng booking bị hủy (12 tháng gần nhất)
            List<TourBookingStatus> bookingStatuses = new ArrayList<>();
            bookingStatuses.add(TourBookingStatus.CANCELLED_WITH_REFUND);
            bookingStatuses.add(TourBookingStatus.CANCELLED_WITHOUT_REFUND);
            Integer cancelBookingNumber = tourBookingRepository.getBookingNumberByStatus(
                    startDate,
                    endDate,
                    bookingStatuses);

            //Tính số lượng booking qua kênh online (12 tháng gần nhất)
            Integer onlineBookingNumber = tourBookingRepository.getBookingNumberByType(
                    startDate,
                    endDate,
                    TourBookingCategory.ONLINE);

            //Tính số lượng booking qua kênh offline (12 tháng gần nhất)
            Integer offlineBookingNumber = tourBookingRepository.getBookingNumberByType(
                    startDate,
                    endDate,
                    TourBookingCategory.SALE);

            //Tính số lượng khách hàng quay lại (12 tháng gần nhất)
            Integer returnCustomerNumber = tourBookingRepository.getReturnCustomerNumber(startDate, endDate);

            //Map to DTO
            DashboardDTO resultDTO = DashboardDTO.builder()
                    .monthlyRevenue(monthlyRevenue)
                    .monthlyNewUsers(monthlyNewUsers)
                    .tourTypeRatios(tourTypeRatios)
                    .recentBookings(recentBookings)
                    .topRevenueTours(topRevenueTours)
                    .cancelBookingNumber(cancelBookingNumber)
                    .onlineBookingNumber(onlineBookingNumber)
                    .offlineBookingNumber(offlineBookingNumber)
                    .returnCustomerNumber(returnCustomerNumber)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), "Dashboard tải lên thành công", resultDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of("Dashboard tải lên thất bại", ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<TourResponseDTO> sendTourForApproval(Long tourId, User currentUser) {
        try {
            // Find the tour by ID
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
            // Check if the tour is in DRAFT status
            if (tour.getTourStatus() != TourStatus.DRAFT &&
                    tour.getTourStatus() != TourStatus.REJECTED) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST,
                        ONLY_DRAFT_CAN_BE_SENT + tour.getTourStatus());
            }
            // Check if the current user is the creator of the tour or has admin privileges
            boolean isCreator = tour.getCreatedBy() != null &&
                    tour.getCreatedBy().getId().equals(currentUser.getId());
            if (!isCreator) {
                throw BusinessException.of(HttpStatus.FORBIDDEN,
                        ONLY_CREATOR_CAN_SEND);
            }
            // Validate tour data before sending for approval
            validateTourForApproval(tour);
            // Update the tour status to PENDING
            tour.setTourStatus(TourStatus.PENDING);
            // Save the updated tour
            Tour updatedTour = tourRepository.save(tour);
            // Map to response DTO
            TourResponseDTO tourResponseDTO = mapToTourResponseDTO(updatedTour);
            return new GeneralResponse<>(HttpStatus.OK.value(), TOUR_SENT_FOR_APPROVAL_SUCCESS, tourResponseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    FAILED_TO_SEND_TOUR + ex.getMessage(), ex);
        }
    }

    @Override
    public GeneralResponse<TourResponseDTO> openTour(Long tourId, User currentUser) {
        try {
            // Find the tour by ID
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
            // Validate tour data before sending for approval
            validateTourForApproval(tour);
            // Update the tour status to PENDING
            tour.setTourStatus(TourStatus.OPENED);
            // Save the updated tour
            Tour updatedTour = tourRepository.save(tour);
            // Map to response DTO
            TourResponseDTO tourResponseDTO = mapToTourResponseDTO(updatedTour);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Tour đã mở bán thành công", tourResponseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Mở bán tour thất bại: " + ex.getMessage(), ex);
        }
    }

    @Override
    public GeneralResponse<TourResponseDTO> changeToPendingPricing(Long tourId, User currentUser) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));
            if (tour.getTourStatus() != TourStatus.OPENED) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Tour không ở trạng thái đang mở bán");
            }
            tour.setTourStatus(TourStatus.PENDING_PRICING);
            Tour updatedTour = tourRepository.save(tour);

            TourResponseDTO tourResponseDTO = mapToTourResponseDTO(updatedTour);

            return new GeneralResponse<>(HttpStatus.OK.value(), "Đã chuyển tour sang trạng thái chờ chiết tính giá", tourResponseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Chuyển trạng thái tour thất bại: " + ex.getMessage(), ex);
        }
    }

    private void validateTourForApproval(Tour tour) {
        List<String> missingFields = new ArrayList<>();

        // Check required fields
        if (StringUtils.isEmpty(tour.getName())) {
            missingFields.add("name");
        }
        if (tour.getLocations() == null || tour.getLocations().isEmpty()) {
            missingFields.add("locations");
        }
        if (tour.getDepartLocation() == null) {
            missingFields.add("departLocation");
        }

        if (tour.getTourType() == null) {
            missingFields.add("tourType");
        }

        // Check if tour days are created
        List<TourDay> tourDays = tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(tour.getId());
        if (tourDays.isEmpty() || tourDays.size() != tour.getNumberDays()) {
            missingFields.add("tourDays");
        }

        // Check if tour has at least one valid pax configuration
        List<TourPax> tourPaxes = tourPaxRepository.findByTourIdAndDeletedFalse(tour.getId());
        if (tourPaxes.isEmpty()) {
            missingFields.add("paxConfigurations");
        }

        if (!missingFields.isEmpty()) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST,
                    TOUR_MISSING_REQUIRED_INFO + String.join(", ", missingFields));
        }
    }

    private Specification<Tour> buildSearchSpecificationAdmin(String keyword, TourStatus tourStatus) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("deleted"), false));

            // Search by tour name
            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Ensure PostgreSQL has UNACCENT enabled
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(root.get("name")));

                // Remove accents from the input keyword
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                Predicate tourNamePredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                // Combine both conditions
                predicates.add(tourNamePredicate);
            }

            List<TourStatus> statuses = new ArrayList<>();
            statuses.add(TourStatus.PENDING);
            statuses.add(TourStatus.APPROVED);
            statuses.add(TourStatus.REJECTED);
            if (tourStatus == null) {
                predicates.add(root.get("tourStatus").in(statuses));
            }
            // Filter by status
            if (tourStatus != null) {
                predicates.add(cb.equal(root.get("tourStatus"), tourStatus));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public GeneralResponse<TourMarkupResponseDTO> getTourMarkupPercentage(Long tourId) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Create response DTO
            TourMarkupResponseDTO responseDTO = mapTourToMarkupResponseDTO(tour);

            return new GeneralResponse<>(HttpStatus.OK.value(), MARKUP_RETRIEVE_SUCCESS, responseDTO);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, MARKUP_RETRIEVE_FAIL, ex);
        }
    }

    private TourResponseDTO mapToTourResponseDTO(Tour tour) {
        return TourResponseDTO.builder()
                .id(tour.getId())
                .name(tour.getName())
                .highlights(tour.getHighlights())
                .numberDays(tour.getNumberDays())
                .numberNights(tour.getNumberNights())
                .note(tour.getNote())
                .locations(tour.getLocations().stream()
                        .map(locationMapper::toPublicLocationDTO)
                        .collect(Collectors.toList()))
                .tags(tour.getTags().stream()
                        .map(tagMapper::toDTO)
                        .collect(Collectors.toList()))
                .tourType(tour.getTourType().name())
                .tourStatus(tour.getTourStatus().name())
                .departLocation(locationMapper.toPublicLocationDTO(tour.getDepartLocation()))
                .markUpPercent(tour.getMarkUpPercent())
                .privacy(tour.getPrivacy())
                .createdDate(tour.getCreatedAt())
                .updatedDate(tour.getUpdatedAt())
                .createdBy(mapToUserBasicDTO(tour.getCreatedBy()))
                .tourImages(tour.getTourImages().stream()
                        .filter(image -> !Boolean.TRUE.equals(image.getDeleted()))
                        .map(tourImageMapper::toPublicTourImageDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    private UserBasicDTO mapToUserBasicDTO(User user) {
        return UserBasicDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatarImage(user.getAvatarImage())
                .build();
    }

    private TourBasicDTO convertToTourBasicDTO(Tour tour) {
        List<TourImageFullDTO> tourImageDTOs = tour.getTourImages().stream()
                .filter(image -> image.getDeleted() == null || !image.getDeleted())
                .map(image -> TourImageFullDTO.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .deleted(false)
                        .build())
                .collect(Collectors.toList());

        return TourBasicDTO.builder()
                .id(tour.getId())
                .name(tour.getName())
                .highlights(tour.getHighlights())
                .numberDays(tour.getNumberDays())
                .numberNight(tour.getNumberNights())
                .note(tour.getNote())
                .deleted(tour.getDeleted())
                .tourStatus(tour.getTourStatus())
                .tourType(tour.getTourType())
                .markUpPercent(tour.getMarkUpPercent())
                .privacy(tour.getPrivacy())
                .createdUserId(tour.getCreatedBy().getId())
                .createdUserName(tour.getCreatedBy().getFullName())
                .tourImages(tourImageDTOs)
                .build();
    }

    private <T> GeneralResponse<PagingDTO<List<T>>> buildPagedResponse(Page<Tour> tourPage, List<T> tours) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tours)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "Thành công", pagingDTO);
    }

    private Specification<Tour> buildSearchSpecification(String keyword, Double budgetFrom, Double budgetTo, Integer duration, LocalDate fromDate, Long departLocationId) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            // Always filter out deleted tours
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("tourType"), "SIC"));
            predicates.add(cb.equal(root.get("tourStatus"), "OPENED"));

            // Search by tour name OR depart location name
            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Ensure PostgreSQL has UNACCENT enabled
                Expression<String> normalizedTourName = cb.function("unaccent", String.class, cb.lower(root.get("name")));
                Expression<String> normalizedLocationName = cb.function("unaccent", String.class, cb.lower(root.join("locations", JoinType.LEFT).get("name")));

                // Remove accents from the input keyword
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                Predicate tourNamePredicate = cb.like(normalizedTourName, cb.concat("%", cb.concat(normalizedKeyword, "%")));
                Predicate locationNamePredicate = cb.like(normalizedLocationName, cb.concat("%", cb.concat(normalizedKeyword, "%")));

                // Combine both conditions
                predicates.add(cb.or(tourNamePredicate, locationNamePredicate));
            }
            // Filter by duration (number of days)
            if (duration != null && duration > 0) {
                predicates.add(cb.equal(root.get("numberDays"), duration));
            }

            LocalDate currentDate = LocalDate.now();

            // Filter by tour schedule date
//            Join<Tour, TourSchedule> scheduleJoin = root.join("tourSchedules", JoinType.LEFT);
//            predicates.add(cb.equal(scheduleJoin.get("status"), TourScheduleStatus.OPEN));
//            predicates.add(cb.greaterThan(scheduleJoin.get("startDate"), currentDate.plusDays(1)));
//            if (fromDate != null) {
//                predicates.add(cb.greaterThan(scheduleJoin.get("startDate"), fromDate));
//            }

            //Filter by price of tour
//            Join<Tour, TourPax> paxJoin = root.join("tourPax", JoinType.INNER);
//            Predicate validToPredicate = cb.greaterThan(paxJoin.get("validTo"), currentDate);
//            predicates.add(validToPredicate);
            // Join with TourPax
            Join<Tour, TourPax> paxJoin = root.join("tourPax", JoinType.INNER);

            // Join with TourSchedule through TourPax
            Join<TourPax, TourSchedule> schedulePaxJoin = paxJoin.join("tourSchedule", JoinType.INNER);

            // Filter by TourSchedule status and date
            predicates.add(cb.equal(schedulePaxJoin.get("status"), TourScheduleStatus.OPEN));
            predicates.add(cb.greaterThan(schedulePaxJoin.get("startDate"), currentDate.plusDays(1)));
            predicates.add(cb.isFalse(schedulePaxJoin.get("deleted"))); // Ensure TourSchedule is not deleted

            // Apply fromDate filter if provided
            if (fromDate != null) {
                predicates.add(cb.greaterThan(schedulePaxJoin.get("startDate"), fromDate));
            }

            // Filter by TourPax validity and price
            predicates.add(cb.greaterThan(paxJoin.get("validTo"), currentDate));
            predicates.add(cb.isFalse(paxJoin.get("deleted"))); // Ensure TourPax is not deleted

            if (budgetFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(paxJoin.get("sellingPrice"), budgetFrom));
            }

            if (budgetTo != null) {
                predicates.add(cb.lessThanOrEqualTo(paxJoin.get("sellingPrice"), budgetTo));
            }

            if (departLocationId != null) {
                predicates.add(cb.equal(root.get("departLocation").get("id"), departLocationId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    private Specification<Tour> buildSimpleSearchSpecification(String keyword, Boolean isDeleted, Boolean isOpened) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Normalize Vietnamese text for search (ignore case and accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedName = cb.function("unaccent", String.class, cb.lower(root.get("name")));
                Expression<String> normalizedKeyword = cb.function("unaccent", String.class, cb.literal(keyword.toLowerCase()));

                Predicate namePredicate = cb.like(normalizedName, cb.concat("%", cb.concat(normalizedKeyword, "%")));
                predicates.add(namePredicate);
            }
            // Filter by deletion status
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("deleted"), isDeleted));
            }
            if (isOpened != null) {
                predicates.add(cb.equal(root.get("opened"), isOpened));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private GeneralResponse<PagingDTO<List<TourBasicDTO>>> buildSimplePagedResponse(Page<Tour> tourPage, List<TourBasicDTO> tourDTOs) {
        PagingDTO<List<TourBasicDTO>> pagingDTO = PagingDTO.<List<TourBasicDTO>>builder()
                .page(tourPage.getNumber())
                .size(tourPage.getSize())
                .total(tourPage.getTotalElements())
                .items(tourDTOs)
                .build();
        return new GeneralResponse<>(HttpStatus.OK.value(), "Thành công", pagingDTO);
    }

    private TourMarkupResponseDTO mapTourToMarkupResponseDTO(Tour tour) {
        if (tour == null) {
            return null;
        }

        return TourMarkupResponseDTO.builder()
                .tourId(tour.getId())
                .tourName(tour.getName())
                .markUpPercent(tour.getMarkUpPercent())
                .build();
    }
}
