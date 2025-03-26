package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.MealType;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.ServiceProviderService;
import com.fpt.capstone.tourism.service.TourDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class TourDiscountServiceImpl implements TourDiscountService {
    private final TourRepository tourRepository;
    private final TourDayRepository tourDayRepository;
    private final TourDayServiceRepository tourDayServiceRepository;
    private final TourPaxRepository tourPaxRepository;
    private final ServiceRepository serviceRepository;
    private final RoomRepository roomRepository;
    private final MealRepository mealRepository;
    private final TransportRepository transportRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final LocationRepository locationRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    @Override
    public GeneralResponse<ServiceByCategoryDTO> getServiceDetail(Long tourId, Long serviceId) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Get service
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND + " with id: " + serviceId));

            // 3. Find the TourDayService entry
            TourDayService tourDayService = tourDayServiceRepository.findByServiceIdAndTourDayTourId(serviceId, tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_ASSOCIATED));

            TourDay tourDay = tourDayService.getTourDay();

            // 4. Get pax options and calculate adjusted prices
            List<TourPax> paxOptions = tourPaxRepository.findByTourIdOrderByMinPax(tourId);
            Map<String, PaxPriceInfoDTO> paxPrices = new HashMap<>();

            for (TourPax pax : paxOptions) {
                // Calculate adjusted price based on pax configuration
                Double adjustedPrice = calculatePriceForPax(tourDayService.getSellingPrice(), pax);

                paxPrices.put(pax.getId().toString(), PaxPriceInfoDTO.builder()
                        .paxId(pax.getId())
                        .minPax(pax.getMinPax())
                        .maxPax(pax.getMaxPax())
                        .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                        .price(adjustedPrice)
                        .build());
            }

            // 5. Determine service status
            String status = determineServiceStatus(service.getStartDate(), service.getEndDate());

            // 6. Get type-specific details based on service category
            RoomDetailDTO roomDetail = null;
            MealDetailDTO mealDetail = null;
            TransportDetailDTO transportDetail = null;

            String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;

            if (HOTEL.equalsIgnoreCase(categoryName)) {
                Optional<Room> roomOpt = roomRepository.findByServiceId(serviceId);
                if (roomOpt.isPresent()) {
                    Room room = roomOpt.get();
                    roomDetail = RoomDetailDTO.builder()
                            .id(room.getId())
                            .capacity(room.getCapacity())
                            .availableQuantity(room.getAvailableQuantity())
                            .facilities(room.getFacilities())
                            .build();
                }
            } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                Optional<Meal> mealOpt = mealRepository.findByServiceId(serviceId);
                if (mealOpt.isPresent()) {
                    Meal meal = mealOpt.get();
                    mealDetail = MealDetailDTO.builder()
                            .id(meal.getId())
                            .type(meal.getType().name())
                            .mealDetail(meal.getMealDetail())
                            .build();
                }
            } else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
                Optional<Transport> transportOpt = transportRepository.findByServiceId(serviceId);
                if (transportOpt.isPresent()) {
                    Transport transport = transportOpt.get();
                    transportDetail = TransportDetailDTO.builder()
                            .id(transport.getId())
                            .seatCapacity(transport.getSeatCapacity())
                            .build();
                }
            }

            // 7. Build response
            ServiceByCategoryDTO response = ServiceByCategoryDTO.builder()
                    .id(service.getId())
                    .name(service.getName())
                    .dayNumber(tourDay.getDayNumber())
                    .status(status)
                    .nettPrice(service.getNettPrice())
                    .sellingPrice(tourDayService.getSellingPrice())
                    .locationId(tourDay.getLocation() != null ? tourDay.getLocation().getId() : null)
                    .locationName(tourDay.getLocation() != null ? tourDay.getLocation().getName() : null)
                    .serviceProviderId(service.getServiceProvider() != null ? service.getServiceProvider().getId() : null)
                    .serviceProviderName(service.getServiceProvider() != null ? service.getServiceProvider().getName() : null)
                    .categoryName(categoryName)
                    .startDate(service.getStartDate())
                    .endDate(service.getEndDate())
                    .paxPrices(paxPrices)
                    .roomDetail(roomDetail)
                    .mealDetail(mealDetail)
                    .transportDetail(transportDetail)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), SERVICE_DETAIL_LOAD_SUCCESS, response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, SERVICE_DETAIL_LOAD_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<ServiceProviderServicesDTO> getServiceProviderServices(Long providerId, Long locationId) {
        try {
            // 1. Validate service provider exists
            ServiceProvider provider = serviceProviderRepository.findById(providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_PROVIDER_NOT_FOUND + " with id: " + providerId));

            // 2. Validate location exists
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, LOCATION_NOT_FOUND + " with id: " + locationId));

            // 3. Get services provided by this provider at this location
            List<Service> services = serviceRepository.findByServiceProviderIdAndLocationId(providerId, locationId);

            // 4. Convert to DTOs with type-specific details
            List<AvailableServiceDTO> availableServices = new ArrayList<>();

            for (Service service : services) {
                String status = determineServiceStatus(service.getStartDate(), service.getEndDate());
                String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;

                // Get type-specific details based on service category
                RoomDetailDTO roomDetail = null;
                MealDetailDTO mealDetail = null;
                TransportDetailDTO transportDetail = null;

                if ("Hotel".equalsIgnoreCase(categoryName)) {
                    Optional<Room> roomOpt = roomRepository.findByServiceIdAndDeletedFalse(service.getId());
                    if (roomOpt.isPresent()) {
                        Room room = roomOpt.get();
                        roomDetail = RoomDetailDTO.builder()
                                .id(room.getId())
                                .capacity(room.getCapacity())
                                .availableQuantity(room.getAvailableQuantity())
                                .facilities(room.getFacilities())
                                .build();
                    }
                } else if ("Restaurant".equalsIgnoreCase(categoryName)) {
                    Optional<Meal> mealOpt = mealRepository.findByServiceIdAndDeletedFalse(service.getId());
                    if (mealOpt.isPresent()) {
                        Meal meal = mealOpt.get();
                        mealDetail = MealDetailDTO.builder()
                                .id(meal.getId())
                                .type(meal.getType().name())
                                .mealDetail(meal.getMealDetail())
                                .build();
                    }
                } else if ("Transport".equalsIgnoreCase(categoryName)) {
                    Optional<Transport> transportOpt = transportRepository.findByServiceIdAndDeletedFalse(service.getId());
                    if (transportOpt.isPresent()) {
                        Transport transport = transportOpt.get();
                        transportDetail = TransportDetailDTO.builder()
                                .id(transport.getId())
                                .seatCapacity(transport.getSeatCapacity())
                                .build();
                    }
                }

                AvailableServiceDTO serviceDTO = AvailableServiceDTO.builder()
                        .id(service.getId())
                        .name(service.getName())
                        .categoryName(categoryName)
                        .nettPrice(service.getNettPrice())
                        .sellingPrice(service.getSellingPrice())
                        .status(status)
                        .startDate(service.getStartDate())
                        .endDate(service.getEndDate())
                        .roomDetail(roomDetail)
                        .mealDetail(mealDetail)
                        .transportDetail(transportDetail)
                        .build();

                availableServices.add(serviceDTO);
            }

            // 5. Build response
            ServiceProviderServicesDTO response = ServiceProviderServicesDTO.builder()
                    .providerId(providerId)
                    .providerName(provider.getName())
                    .locationId(locationId)
                    .locationName(location.getName())
                    .availableServices(availableServices)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), PROVIDER_SERVICES_LOAD_SUCCESS, response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, PROVIDER_SERVICES_LOAD_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<ServiceProviderOptionsDTO> getServiceProviderOptions(Long locationId, String categoryName) {
        try {
            // 1. Validate location exists
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, LOCATION_NOT_FOUND + " with id: " + locationId));

            // 2. Validate category exists
            ServiceCategory category = serviceCategoryRepository.findByCategoryName(categoryName)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Service category not found: " + categoryName));

            // 3. Get service providers for this location and category
            List<ServiceProvider> providers = serviceProviderRepository.findByLocationIdAndServiceCategoryId(locationId, category.getId());

            // 4. Convert to DTOs
            List<ServiceProviderOptionDTO> providerDTOs = providers.stream()
                    .map(provider -> ServiceProviderOptionDTO.builder()
                            .id(provider.getId())
                            .name(provider.getName())
                            .imageUrl(provider.getImageUrl())
                            .star(provider.getStar())
                            .phone(provider.getPhone())
                            .email(provider.getEmail())
                            .address(provider.getAddress())
                            .build())
                    .collect(Collectors.toList());

            // 5. Build response
            ServiceProviderOptionsDTO response = ServiceProviderOptionsDTO.builder()
                    .serviceProviders(providerDTOs)
                    .locationId(locationId)
                    .locationName(location.getName())
                    .categoryName(categoryName)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), "Service providers retrieved successfully", response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve service providers", ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<ServiceByCategoryDTO> createServiceDetail(Long tourId, ServiceCreateRequestDTO request) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Validate service exists
            if (request.getServiceId() == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, SERVICE_ID_REQUIRED);
            }

            Service service = serviceRepository.findById(request.getServiceId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND + " with id: " + request.getServiceId()));

            // 3. Validate day number is provided
            if (request.getDayNumber() == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, DAY_NUMBER_REQUIRED);
            }

            // 4. Find the tour day
            TourDay tourDay = tourDayRepository.findByTourIdAndDayNumber(tourId, request.getDayNumber())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND + ": " + request.getDayNumber()));

            // 5. Check if the service is already associated with this tour day
            if (tourDayServiceRepository.existsByServiceIdAndTourDayId(request.getServiceId(), tourDay.getId())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, SERVICE_ALREADY_ASSOCIATED);
            }

            // 6. Create a new tour day service
            TourDayService tourDayService = new TourDayService();
            tourDayService.setTourDay(tourDay);
            tourDayService.setService(service);
            tourDayService.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1); // Default quantity is 1

            // 7. Set selling price if provided, otherwise use service's default price
            if (request.getSellingPrice() != null) {
                tourDayService.setSellingPrice(request.getSellingPrice());
            } else {
                tourDayService.setSellingPrice(service.getSellingPrice());
            }

            // 8. Update service-specific details
            String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;

            if ("Hotel".equalsIgnoreCase(categoryName) && request.getRoomDetail() != null) {
                Room room = roomRepository.findByServiceId(service.getId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Room not found for service id: " + service.getId()));

                if (request.getRoomDetail().getCapacity() != null) {
                    room.setCapacity(request.getRoomDetail().getCapacity());
                }

                if (request.getRoomDetail().getAvailableQuantity() != null) {
                    room.setAvailableQuantity(request.getRoomDetail().getAvailableQuantity());
                }

                if (request.getRoomDetail().getFacilities() != null) {
                    room.setFacilities(request.getRoomDetail().getFacilities());
                }

                roomRepository.save(room);
            }
            else if ("Restaurant".equalsIgnoreCase(categoryName) && request.getMealDetail() != null) {
                Meal meal = mealRepository.findByServiceId(service.getId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Meal not found for service id: " + service.getId()));

                if (request.getMealDetail().getType() != null) {
                    meal.setType(MealType.valueOf(request.getMealDetail().getType()));
                }

                if (request.getMealDetail().getMealDetail() != null) {
                    meal.setMealDetail(request.getMealDetail().getMealDetail());
                }

                mealRepository.save(meal);
            }
            else if ("Transport".equalsIgnoreCase(categoryName) && request.getTransportDetail() != null) {
                Transport transport = transportRepository.findByServiceId(service.getId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Transport not found for service id: " + service.getId()));

                if (request.getTransportDetail().getSeatCapacity() != null) {
                    transport.setSeatCapacity(request.getTransportDetail().getSeatCapacity());
                }

                transportRepository.save(transport);
            }

            // 9. Update pax-specific pricing if provided
            if (request.getPaxPrices() != null && !request.getPaxPrices().isEmpty()) {
                // Calculate an average price from all the pax prices to use as the base price
                Double avgPrice = request.getPaxPrices().values().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(tourDayService.getSellingPrice() != null ? tourDayService.getSellingPrice() : service.getSellingPrice());

                tourDayService.setSellingPrice(avgPrice);
            }

            // 10. Save the tour day service entry
            tourDayService = tourDayServiceRepository.save(tourDayService);

            // 11. Return service details
            return getServiceDetail(tourId, tourDayService.getService().getId());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, SERVICE_CREATE_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<Void> changeServiceStatus(Long tourId, Long serviceId, Boolean delete) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Verify the service exists
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND + " with id: " + serviceId));

            // 3. Find the TourDayService entry to verify association
            TourDayService tourDayService = tourDayServiceRepository.findByServiceIdAndTourDayTourId(serviceId, tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_ASSOCIATED));

            // 4. Update status based on service category
            String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;
            boolean statusUpdated = false;

            if (HOTEL.equalsIgnoreCase(categoryName)) {
                Room room = roomRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Room not found for service id: " + serviceId));

                room.setDeleted(delete);
                roomRepository.save(room);
                statusUpdated = true;
            }
            else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                Meal meal = mealRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Meal not found for service id: " + serviceId));

                meal.setDeleted(delete);
                mealRepository.save(meal);
                statusUpdated = true;
            }
            else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
                Transport transport = transportRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Transport not found for service id: " + serviceId));

                transport.setDeleted(delete);
                transportRepository.save(transport);
                statusUpdated = true;
            }

            if (!statusUpdated) {
                // If no specific service detail was found, update the service itself
                service.setDeleted(delete);
                serviceRepository.save(service);
            }

            // 5. Return success response
            String serviceType = categoryName != null ? categoryName : "Service";
            String message = delete ? serviceType + " marked as deleted successfully" : serviceType + " restored successfully";

            return GeneralResponse.<Void>builder()
                    .code(HttpStatus.OK.value())
                    .message(message)
                    .build();
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            String errorMessage = delete ? SERVICE_DELETE_FAIL : "Failed to change service status";
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<ServiceByCategoryDTO> updateServiceDetail(Long tourId, Long serviceId, ServiceUpdateRequestDTO request) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Get current service if we're updating an existing one
            Service currentService = null;
            if (serviceId != null) {
                currentService = serviceRepository.findById(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND + " with id: " + serviceId));
            }

            // 3. Find or create the TourDayService entry
            TourDayService tourDayService;
            TourDay tourDay;
            Service service;

            // If we're updating an existing service
            if (currentService != null) {
                tourDayService = tourDayServiceRepository.findByServiceIdAndTourDayTourId(serviceId, tourId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_ASSOCIATED));

                tourDay = tourDayService.getTourDay();
                service = currentService;

                // Update day number if provided
                if (request.getDayNumber() != null && !request.getDayNumber().equals(tourDay.getDayNumber())) {
                    TourDay newTourDay = tourDayRepository.findByTourIdAndDayNumber(tourId, request.getDayNumber())
                            .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND + ": " + request.getDayNumber()));

                    tourDayService.setTourDay(newTourDay);
                    tourDay = newTourDay;
                }
            }
            // If we're creating a new service
            else if (request.getServiceId() != null) {
                // Get the new service
                service = serviceRepository.findById(request.getServiceId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND + " with id: " + request.getServiceId()));

                // Find the tour day
                if (request.getDayNumber() == null) {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST, "Day number is required when creating a new service");
                }

                tourDay = tourDayRepository.findByTourIdAndDayNumber(tourId, request.getDayNumber())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND + ": " + request.getDayNumber()));

                // Create a new tour day service
                tourDayService = new TourDayService();
                tourDayService.setTourDay(tourDay);
                tourDayService.setService(service);
                tourDayService.setQuantity(1); // Default quantity
            } else {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Either serviceId parameter or serviceId in request body is required");
            }

            // 4. If service provider or location changed and we're updating an existing service
            if (currentService != null &&
                    ((request.getServiceProviderId() != null && !request.getServiceProviderId().equals(service.getServiceProvider().getId())) ||
                            (request.getLocationId() != null && tourDay.getLocation() != null &&
                                    !request.getLocationId().equals(tourDay.getLocation().getId())))) {

                // Find a new service with the requested provider and category
                Long categoryId = service.getServiceCategory().getId();

                // Get services from the specified provider in the specified category
                List<Service> availableServices = serviceRepository.findByServiceProviderIdAndCategoryId(
                        request.getServiceProviderId(), categoryId);

                if (!availableServices.isEmpty()) {
                    // Select the first available service
                    Service newService = availableServices.get(0);
                    tourDayService.setService(newService);

                    // Update service for subsequent operations
                    service = newService;
                } else {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST, NO_SERVICES_AVAILABLE);
                }
            }

            // 5. Update service-specific details
            String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;

            if ("Hotel".equalsIgnoreCase(categoryName) && request.getRoomDetail() != null) {
                final Service hotelService = service; // Create a final copy
                Room room = roomRepository.findByServiceId(hotelService.getId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Room not found for service id: " + hotelService.getId()));

                if (request.getRoomDetail().getCapacity() != null) {
                    room.setCapacity(request.getRoomDetail().getCapacity());
                }

                if (request.getRoomDetail().getAvailableQuantity() != null) {
                    room.setAvailableQuantity(request.getRoomDetail().getAvailableQuantity());
                }

                if (request.getRoomDetail().getFacilities() != null) {
                    room.setFacilities(request.getRoomDetail().getFacilities());
                }

                roomRepository.save(room);
            }
            else if ("Restaurant".equalsIgnoreCase(categoryName) && request.getMealDetail() != null) {
                final Service restaurantService = service;
                Meal meal = mealRepository.findByServiceId(restaurantService.getId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Meal not found for service id: " + restaurantService.getId()));

                if (request.getMealDetail().getType() != null) {
                    meal.setType(MealType.valueOf(request.getMealDetail().getType()));
                }

                if (request.getMealDetail().getMealDetail() != null) {
                    meal.setMealDetail(request.getMealDetail().getMealDetail());
                }

                mealRepository.save(meal);
            }
            else if ("Transport".equalsIgnoreCase(categoryName) && request.getTransportDetail() != null) {
                final Service transportService = service; // Create a final copy
                Transport transport = transportRepository.findByServiceId(transportService.getId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Transport not found for service id: " + transportService.getId()));

                if (request.getTransportDetail().getSeatCapacity() != null) {
                    transport.setSeatCapacity(request.getTransportDetail().getSeatCapacity());
                }

                transportRepository.save(transport);
            }

            // 6. Update pricing
            if (request.getNettPrice() != null) {
                service.setNettPrice(request.getNettPrice());
                serviceRepository.save(service);
            }

            if (request.getSellingPrice() != null) {
                tourDayService.setSellingPrice(request.getSellingPrice());
            }

            // 7. Update pax-specific pricing if provided
            if (request.getPaxPrices() != null && !request.getPaxPrices().isEmpty()) {
                // Since we're not using a separate ServicePrice model, we'll store the base price
                // and use the TourPax information to calculate dynamic prices

                // Calculate an average price from all the pax prices to use as the base price
                Double avgPrice = request.getPaxPrices().values().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(tourDayService.getSellingPrice() != null ? tourDayService.getSellingPrice() : service.getSellingPrice());

                tourDayService.setSellingPrice(avgPrice);
            }

            // 8. Save the tour day service entry
            tourDayService = tourDayServiceRepository.save(tourDayService);

            // 9. Return updated service details
            return getServiceDetail(tourId, tourDayService.getService().getId());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, SERVICE_UPDATE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<TourServiceListDTO> getTourServicesList(Long tourId, Integer paxCount) {
        try {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND));

            // Get all tour days for this tour
            List<TourDay> tourDays = tourDayRepository.findByTourIdAndDeletedFalseOrderByDayNumber(tourId);
            if (tourDays.isEmpty()) {
                throw BusinessException.of(HttpStatus.NOT_FOUND, NO_TOUR_DAYS_FOUND + " for tour with id: " + tourId);
            }

            List<Long> tourDayIds = tourDays.stream()
                    .map(TourDay::getId)
                    .collect(Collectors.toList());

            // Get all tour day services
            List<TourDayService> allTourDayServices = tourDayServiceRepository.findByTourDayIdIn(tourDayIds);

            // Get pax options
            List<TourPax> paxOptions = paxCount != null
                    ? tourPaxRepository.findByTourIdAndPaxRange(tourId, paxCount)
                    : tourPaxRepository.findByTourIdOrderByMinPax(tourId);

            List<TourPaxOptionDTO> paxOptionDTOs = paxOptions.stream()
                    .map(pax -> TourPaxOptionDTO.builder()
                            .id(pax.getId())
                            .minPax(pax.getMinPax())
                            .maxPax(pax.getMaxPax())
                            .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                            .build())
                    .collect(Collectors.toList());

            // Create a map to group services by category name
            Map<String, List<ServiceSummaryDTO>> servicesByCategoryName = new HashMap<>();

            for (TourDayService tds : allTourDayServices) {
                Service service = tds.getService();
                TourDay tourDay = tds.getTourDay();

                if (service != null && service.getServiceCategory() != null) {
                    String categoryName = service.getServiceCategory().getCategoryName();

                    // Determine service status
                    String status = determineServiceStatus(service.getStartDate(), service.getEndDate());

                    // Calculate pax prices for this service using TourPax information
                    Map<String, PaxPriceInfoDTO> paxPrices = new HashMap<>();
                    for (TourPax pax : paxOptions) {
                        // Calculate price based on TourPax settings and service selling price
                        Double adjustedPrice = calculatePriceForPax(tds.getSellingPrice(), pax);

                        paxPrices.put(pax.getId().toString(), PaxPriceInfoDTO.builder()
                                .paxId(pax.getId())
                                .minPax(pax.getMinPax())
                                .maxPax(pax.getMaxPax())
                                .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                                .price(adjustedPrice)
                                .build());
                    }

                    ServiceSummaryDTO serviceSummary = ServiceSummaryDTO.builder()
                            .id(service.getId())
                            .name(service.getName())
                            .dayNumber(tourDay.getDayNumber())
                            .status(status)
                            .nettPrice(service.getNettPrice())
                            .sellingPrice(tds.getSellingPrice())
                            .locationName(tourDay.getLocation() != null ? tourDay.getLocation().getName() : null)
                            .locationId(tourDay.getLocation() != null ? tourDay.getLocation().getId() : null)
                            .serviceProviderName(service.getServiceProvider() != null ? service.getServiceProvider().getName() : null)
                            .serviceProviderId(service.getServiceProvider() != null ? service.getServiceProvider().getId() : null)
                            .paxPrices(paxPrices)
                            .build();

                    // Add to the category list by category name
                    if (!servicesByCategoryName.containsKey(categoryName)) {
                        servicesByCategoryName.put(categoryName, new ArrayList<>());
                    }
                    servicesByCategoryName.get(categoryName).add(serviceSummary);
                }
            }

            // Convert map to list of ServiceCategoryDTO
            List<TourServiceCategoryDTO> categoryDTOs = new ArrayList<>();
            for (Map.Entry<String, List<ServiceSummaryDTO>> entry : servicesByCategoryName.entrySet()) {
                TourServiceCategoryDTO categoryDTO = TourServiceCategoryDTO.builder()
                        .categoryName(entry.getKey())
                        .services(entry.getValue())
                        .build();
                categoryDTOs.add(categoryDTO);
            }

            // Build response
            TourServiceListDTO response = TourServiceListDTO.builder()
                    .tourId(tourId)
                    .tourName(tour.getName())
                    .serviceCategories(categoryDTOs)
                    .paxOptions(paxOptionDTOs)
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), SERVICES_LOAD_SUCCESS, response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, SERVICES_LOAD_FAIL, ex);
        }
    }

    private Double calculatePriceForPax(Double basePrice, TourPax pax) {
        double fixedCostPerPerson = pax.getFixedCost() / Math.max(pax.getMinPax(), 1);
        double extraCostPerPerson = pax.getExtraHotelCost() / Math.max(pax.getMinPax(), 1);

        // Apply tiered pricing based on pax range
        if (pax.getMinPax() <= 2) {
            // Higher price for lower number of person
            return basePrice * 1.2 + fixedCostPerPerson + extraCostPerPerson;
        } else if (pax.getMinPax() <= 5) {
            // Standard price for medium number of people
            return basePrice + fixedCostPerPerson + (extraCostPerPerson * 0.8);
        } else {
            // Discount for high number people
            return basePrice * 0.9 + fixedCostPerPerson * 0.8;
        }
    }

    private String determineServiceStatus(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        LocalDateTime now = LocalDateTime.now();
        if (startDateTime == null || endDateTime == null) {
            return "UNKNOWN";
        }
        if (now.isBefore(startDateTime)) {
            return "UPCOMING";
        } else if (now.isAfter(endDateTime)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }
}
