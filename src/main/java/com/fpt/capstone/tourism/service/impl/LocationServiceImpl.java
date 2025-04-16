package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.dto.common.PublicLocationSimpleDTO;
import com.fpt.capstone.tourism.dto.common.PublicLocationSimpleProviderDTO;
import com.fpt.capstone.tourism.dto.request.GeoPositionRequestDTO;
import com.fpt.capstone.tourism.dto.request.LocationRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.GeoPositionMapper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
//import com.fpt.capstone.tourism.model.Activity;
import com.fpt.capstone.tourism.model.GeoPosition;
import com.fpt.capstone.tourism.model.Location;
import com.fpt.capstone.tourism.model.Tour;
import com.fpt.capstone.tourism.repository.LocationRepository;
import com.fpt.capstone.tourism.service.LocationService;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.stream.Collectors;

    import static com.fpt.capstone.tourism.constants.Constants.Message.*;

    @Service
    @RequiredArgsConstructor
    public class LocationServiceImpl implements LocationService {
        private final LocationRepository locationRepository;
        private final LocationMapper locationMapper;
        private final GeoPositionMapper geoPositionMapper;
        @Override
        public GeneralResponse<LocationDTO> saveLocation(LocationRequestDTO locationRequestDTO) {
            try{
                //Validate input data
                Validator.validateLocation(locationRequestDTO);

                //Check duplicate location
                if(locationRepository.findByName(locationRequestDTO.getName()) != null){
                    throw BusinessException.of(EXISTED_LOCATION);
                }

                //Save date to database
                Location location = locationMapper.toEntity(locationRequestDTO);
                location.setCreatedAt(LocalDateTime.now());
                location.setDeleted(false);
                locationRepository.save(location);

                LocationDTO locationDTO = locationMapper.toDTO(location);

                return new GeneralResponse<>(HttpStatus.OK.value(), CREATE_LOCATION_SUCCESS, locationDTO);
            }catch (BusinessException be){
                throw be;
            } catch (Exception ex){
                throw BusinessException.of(CREATE_LOCATION_FAIL, ex);
            }
        }

        @Override
        public GeneralResponse<LocationDTO> getLocationById(Long id) {
            try{
                Location location = locationRepository.findById(id).orElseThrow();

                LocationDTO locationDTO = locationMapper.toDTO(location);
                return new GeneralResponse<>(HttpStatus.OK.value(), GENERAL_SUCCESS_MESSAGE, locationDTO);
            }catch (BusinessException be){
                throw be;
            } catch (Exception ex){
                throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
            }
        }

        @Override
        public GeneralResponse<PagingDTO<List<LocationDTO>>> getAllLocation(int page, int size, String keyword, Boolean isDeleted, String orderDate) {
            try {
                // Determine sorting order dynamically
                Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                Specification<Location> spec = buildSearchSpecification(keyword, isDeleted);

                Page<Location> serviceProviderPage = locationRepository.findAll(spec, pageable);
                List<LocationDTO> serviceProviderDTOS = serviceProviderPage.getContent().stream()
                        .map(locationMapper::toDTO)
                        .collect(Collectors.toList());

                return buildPagedResponse(serviceProviderPage, serviceProviderDTOS);
            } catch (Exception ex) {
                throw BusinessException.of("Tải danh sách địa điểm thất bại", ex);
            }
        }

        @Override
        public GeneralResponse<LocationDTO> deleteLocation(Long id, boolean isDeleted) {
            try{
                Location location = locationRepository.findById(id).orElseThrow();

                location.setDeleted(isDeleted);
                location.setUpdatedAt(LocalDateTime.now());
                locationRepository.save(location);

                LocationDTO locationDTO = locationMapper.toDTO(location);
                return new GeneralResponse<>(HttpStatus.OK.value(), GENERAL_SUCCESS_MESSAGE, locationDTO);
            }catch (BusinessException be){
                throw be;
            } catch (Exception ex){
                throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
            }
        }

        @Override
        public GeneralResponse<LocationDTO> updateLocation(Long id, LocationRequestDTO locationRequestDTO) {
            try{
                //Find in database
                Location location = locationRepository.findById(id).orElseThrow();

                //Validate input data
                Validator.validateLocation(locationRequestDTO);

                //Update service provider information
                if(!locationRequestDTO.getName().equals(location.getName())){
                    //Check duplicate location
                    if(locationRepository.findByName(locationRequestDTO.getName()) != null){
                        throw BusinessException.of(EXISTED_LOCATION);
                    }
                    location.setName(locationRequestDTO.getName());
                }
                if(!locationRequestDTO.getImage().equals(location.getImage())){
                    location.setImage(locationRequestDTO.getImage());
                }
                if(!locationRequestDTO.getDescription().equals(location.getDescription())){
                    location.setDescription(locationRequestDTO.getDescription());
                }

                GeoPositionRequestDTO currentGeoPosition = GeoPositionRequestDTO.builder()
                        .latitude(location.getGeoPosition().getLatitude())
                        .longitude(location.getGeoPosition().getLongitude())
                        .build();
                if(!currentGeoPosition.equals(locationRequestDTO.getGeoPosition())){
                    location.setGeoPosition(geoPositionMapper.toEntity(locationRequestDTO.getGeoPosition()));
                }

                location.setUpdatedAt(LocalDateTime.now());

                locationRepository.save(location);

                LocationDTO locationDTO = locationMapper.toDTO(location);

                return new GeneralResponse<>(HttpStatus.OK.value(), GENERAL_SUCCESS_MESSAGE, locationDTO);
            } catch (BusinessException be){
                throw be;
            } catch (Exception ex){
                throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
            }

        }

    @Override
    public List<PublicLocationDTO> findRecommendedLocations(int numberLocation) {
        List<Location> randomLocations = locationRepository.findRandomLocation(numberLocation);
        return randomLocations.stream()
                .map(locationMapper::toPublicLocationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PublicLocationDTO> findRecommendedLocations(int numberLocation, Long locationId) {
        List<Location> randomLocations = locationRepository.findRandomLocation(numberLocation, locationId);
        return randomLocations.stream()
                .map(locationMapper::toPublicLocationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GeneralResponse<PagingDTO<List<LocationDTO>>> getLocationsByTourId(Long tourId, int page, int size, String keyword, Boolean isDeleted, String orderDate) {
        try {
            // Determine sorting order dynamically
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Create a specification that filters by tourId using the join table
            Specification<Location> spec = (root, query, cb) -> {
                // Join with the tour_location join table
                Join<Location, Tour> tourJoin = root.join("tours", JoinType.INNER);
                return cb.equal(tourJoin.get("id"), tourId);
            };

            // Add keyword filter if provided
            if (keyword != null && !keyword.trim().isEmpty()) {
                spec = spec.and((root, query, cb) ->
                        cb.or(
                                cb.like(cb.lower(root.get("name")), "%" + keyword.toLowerCase() + "%"),
                                cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase() + "%")
                        )
                );
            }

            // Add isDeleted filter - default to false if not provided
            if (isDeleted != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("deleted"), isDeleted));
            } else {
                // Default to showing only non-deleted locations
                spec = spec.and((root, query, cb) -> cb.equal(root.get("deleted"), false));
            }

            Page<Location> locationPage = locationRepository.findAll(spec, pageable);
            List<LocationDTO> locationDTOs = locationPage.getContent().stream()
                    .map(locationMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(locationPage, locationDTOs);
        } catch (Exception ex) {
            throw BusinessException.of("Tải địa điểm thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> getListLocation() {
        try{
            List<PublicLocationSimpleProviderDTO> locationSimpleDTOS = locationRepository.findLocationSimple();
            return new GeneralResponse<>(HttpStatus.OK.value(), "Thành công", locationSimpleDTOS);
        }catch (Exception ex){
            throw BusinessException.of("Tải danh sách địa điểm thất bại", ex);
        }
    }


    private Specification<Location> buildSearchSpecification(String keyword, Boolean isDeleted) {
            return (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (keyword != null && !keyword.isEmpty()) {
                    Predicate namePredicate = cb.like(root.get("name"), "%" + keyword + "%");
                    predicates.add(namePredicate);
                }

                if (isDeleted != null) {
                    predicates.add(cb.equal(root.get("deleted"), isDeleted));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }



        private GeneralResponse<PagingDTO<List<LocationDTO>>> buildPagedResponse(Page<Location> locationPage, List<LocationDTO> locationDTOS) {
            PagingDTO<List<LocationDTO>> pagingDTO = PagingDTO.<List<LocationDTO>>builder()
                    .page(locationPage.getNumber())
                    .size(locationPage.getSize())
                    .total(locationPage.getTotalElements())
                    .items(locationDTOS)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
        }
    }
