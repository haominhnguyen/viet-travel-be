package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceCreateRequestDTO;
import com.fpt.capstone.tourism.dto.request.ServiceUpdateRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.MealType;
import com.fpt.capstone.tourism.model.enums.ServiceCategoryEnum;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.ServiceProviderService;
import com.fpt.capstone.tourism.service.TourDiscountService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
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
    private final ServicePaxPricingRepository servicePaxPricingRepository;

    @PersistenceContext
    private EntityManager entityManager;

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

            // Get all tourDayService IDs
            List<Long> tourDayServiceIds = allTourDayServices.stream()
                    .map(TourDayService::getId)
                    .collect(Collectors.toList());

            // Get all service-specific pax associations from the join table
            List<ServicePaxPricing> allServicePaxPricings = servicePaxPricingRepository.findByTourDayServiceIdIn(tourDayServiceIds);

            // Create a map for quick lookup of service pax associations
            Map<Long, Map<Long, ServicePaxPricing>> serviceToPaxPricingMap = new HashMap<>();

            for (ServicePaxPricing pricing : allServicePaxPricings) {
                Long serviceId = pricing.getTourDayService().getId();
                Long paxId = pricing.getTourPax().getId();

                if (!serviceToPaxPricingMap.containsKey(serviceId)) {
                    serviceToPaxPricingMap.put(serviceId, new HashMap<>());
                }

                serviceToPaxPricingMap.get(serviceId).put(paxId, pricing);
            }

            // Get pax options with a fresh database query to ensure we have the latest data
            List<TourPax> paxOptions = paxCount != null
                    ? tourPaxRepository.findByTourIdAndPaxRange(tourId, paxCount)
                    : tourPaxRepository.findByTourIdOrderByMinPax(tourId);

            // Create a map to easily find TourPax by ID
            Map<Long, TourPax> paxMap = paxOptions.stream()
                    .collect(Collectors.toMap(TourPax::getId, Function.identity()));

            // Updated to include fixedCost and extraHotelCost
            List<TourPaxOptionDTO> paxOptionDTOs = paxOptions.stream()
                    .map(pax -> TourPaxOptionDTO.builder()
                            .id(pax.getId())
                            .minPax(pax.getMinPax())
                            .maxPax(pax.getMaxPax())
                            .price(pax.getNettPricePerPax())
                            .sellingPrice(pax.getSellingPrice())
                            .fixedCost(pax.getFixedCost())
                            .extraHotelCost(pax.getExtraHotelCost())
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

                    // Check if this service has any specific pax associations
                    Map<Long, ServicePaxPricing> paxPricingMap = serviceToPaxPricingMap.getOrDefault(tds.getId(), new HashMap<>());

                    // Calculate pax prices for this service
                    Map<String, PaxPriceInfoDTO> paxPrices = new HashMap<>();

                    // If no specific pax associations exist for this service, create them
                    if (paxPricingMap.isEmpty()) {
                        // Create service-specific pax associations for each pax option
                        for (TourPax pax : paxOptions) {
                            // Create a new association entry for this service and pax
                            ServicePaxPricing newAssociation = ServicePaxPricing.builder()
                                    .tourDayService(tds)
                                    .tourPax(pax)
                                    .build();

                            // Save the new association
                            newAssociation = servicePaxPricingRepository.save(newAssociation);

                            // Add to our maps for use in this request
                            if (!serviceToPaxPricingMap.containsKey(tds.getId())) {
                                serviceToPaxPricingMap.put(tds.getId(), new HashMap<>());
                            }
                            serviceToPaxPricingMap.get(tds.getId()).put(pax.getId(), newAssociation);
                            paxPricingMap = serviceToPaxPricingMap.get(tds.getId());
                        }
                    }

                    // Now build the DTO with pax-specific and service-specific pricing
                    for (TourPax pax : paxOptions) {
                        // Check if this pax is associated with this service
                        if (paxPricingMap.containsKey(pax.getId())) {
                            // Get pricing from TourPax and Service
                            Double nettPricePerPax = pax.getNettPricePerPax();
                            Double sellingPrice = pax.getSellingPrice();
                            Double serviceNettPrice = service.getNettPrice();

                            // Build the DTO with the appropriate pricing
                            paxPrices.put(pax.getId().toString(), PaxPriceInfoDTO.builder()
                                    .paxId(pax.getId())
                                    .minPax(pax.getMinPax())
                                    .maxPax(pax.getMaxPax())
                                    .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                                    .price(nettPricePerPax)         // Using nett price per pax from TourPax
                                    .serviceNettPrice(serviceNettPrice) // Using nett price from Service
                                    .sellingPrice(sellingPrice)     // Using selling price from TourPax
                                    .fixedCost(pax.getFixedCost())
                                    .extraHotelCost(pax.getExtraHotelCost())
                                    .build());
                        }
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

    @Override
    public GeneralResponse<ServiceByCategoryDTO> getServiceDetail(Long tourId, Long serviceId) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Get service
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND + " with id: " + serviceId));

            // 3. Find all TourDayService entries related to this service and tour
            List<TourDayService> allTourDayServices = entityManager.createQuery(
                            "SELECT tds FROM TourDayService tds " +
                                    "JOIN tds.tourDay td " +
                                    "WHERE tds.service.id = :serviceId " +
                                    "AND td.tour.id = :tourId", TourDayService.class)
                    .setParameter("serviceId", serviceId)
                    .setParameter("tourId", tourId)
                    .getResultList();

            if (allTourDayServices.isEmpty()) {
                throw BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_ASSOCIATED);
            }

            // For general service details, use the first TourDayService
            TourDayService primaryTourDayService = allTourDayServices.get(0);
            TourDay tourDay = primaryTourDayService.getTourDay();

            // 4. Get all pax associations for this service
            List<ServicePaxPricing> paxAssociations = new ArrayList<>();
            for (TourDayService tds : allTourDayServices) {
                List<ServicePaxPricing> associations = servicePaxPricingRepository.findByTourDayServiceId(tds.getId());
                paxAssociations.addAll(associations);
            }

            // 5. Create a map of pax IDs to their pax objects
            Map<Long, TourPax> paxMap = new HashMap<>();
            for (ServicePaxPricing association : paxAssociations) {
                TourPax pax = association.getTourPax();
                if (pax != null) {
                    paxMap.put(pax.getId(), pax);
                }
            }

            // 6. Get pax options with a fresh query to ensure we have the latest data
            List<TourPax> paxOptions = tourPaxRepository.findByTourIdOrderByMinPax(tourId);
            Map<String, PaxPriceInfoDTO> paxPrices = new HashMap<>();

            for (TourPax pax : paxOptions) {
                // Check if this pax is associated with the service
                boolean isAssociated = paxMap.containsKey(pax.getId());

                // If associated, include it in the response with pricing from TourPax
                if (isAssociated) {
                    paxPrices.put(pax.getId().toString(), PaxPriceInfoDTO.builder()
                            .paxId(pax.getId())
                            .minPax(pax.getMinPax())
                            .maxPax(pax.getMaxPax())
                            .price(pax.getNettPricePerPax())
                            .serviceNettPrice(service.getNettPrice()) // Include service nett price
                            .sellingPrice(pax.getSellingPrice())
                            .fixedCost(pax.getFixedCost())
                            .extraHotelCost(pax.getExtraHotelCost())
                            .paxRange(pax.getMinPax() + "-" + pax.getMaxPax())
                            .build());
                }
            }

            // 7. Determine service status
            String status = determineServiceStatus(service.getStartDate(), service.getEndDate());

            // 8. Get type-specific details based on service category
            RoomDetailDTO roomDetail = null;
            MealDetailDTO mealDetail = null;
            TransportDetailDTO transportDetail = null;
            String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;

            if (ServiceCategoryEnum.HOTEL.name().equalsIgnoreCase(categoryName)) {
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
            } else if (ServiceCategoryEnum.RESTAURANT.name().equalsIgnoreCase(categoryName)) {
                Optional<Meal> mealOpt = mealRepository.findByServiceId(serviceId);
                if (mealOpt.isPresent()) {
                    Meal meal = mealOpt.get();
                    mealDetail = MealDetailDTO.builder()
                            .id(meal.getId())
                            .type(meal.getType().name())
                            .mealDetail(meal.getMealDetail())
                            .build();
                }
            } else if (ServiceCategoryEnum.TRANSPORT.name().equalsIgnoreCase(categoryName)) {
                Optional<Transport> transportOpt = transportRepository.findByServiceId(serviceId);
                if (transportOpt.isPresent()) {
                    Transport transport = transportOpt.get();
                    transportDetail = TransportDetailDTO.builder()
                            .id(transport.getId())
                            .seatCapacity(transport.getSeatCapacity())
                            .build();
                }
            }

            // 9. Build response
            ServiceByCategoryDTO response = ServiceByCategoryDTO.builder()
                    .id(service.getId())
                    .name(service.getName())
                    .dayNumber(tourDay.getDayNumber())
                    .status(status)
                    .nettPrice(service.getNettPrice())
                    .sellingPrice(primaryTourDayService.getSellingPrice())
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

                if (HOTEL.equalsIgnoreCase(categoryName)) {
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
                } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                    Optional<Meal> mealOpt = mealRepository.findByServiceIdAndDeletedFalse(service.getId());
                    if (mealOpt.isPresent()) {
                        Meal meal = mealOpt.get();
                        mealDetail = MealDetailDTO.builder()
                                .id(meal.getId())
                                .type(meal.getType().name())
                                .mealDetail(meal.getMealDetail())
                                .build();
                    }
                } else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
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

            // Create a final reference that will be used with lambda expressions
            final Long requestServiceId = request.getServiceId();

            Service service = serviceRepository.findById(requestServiceId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND + " with id: " + requestServiceId));

            // 3. Validate day number is provided
            if (request.getDayNumber() == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, DAY_NUMBER_REQUIRED);
            }

            // 4. Find the tour day
            TourDay tourDay = tourDayRepository.findByTourIdAndDayNumber(tourId, request.getDayNumber())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND + ": " + request.getDayNumber()));

            // 5. Check if the service is already associated with this tour day
            List<TourDayService> existingAssociations = entityManager.createQuery(
                            "SELECT tds FROM TourDayService tds " +
                                    "WHERE tds.service.id = :serviceId AND tds.tourDay.id = :tourDayId", TourDayService.class)
                    .setParameter("serviceId", requestServiceId)
                    .setParameter("tourDayId", tourDay.getId())
                    .getResultList();

            if (!existingAssociations.isEmpty()) {
                // Clear existing associations to prevent conflicts
                for (TourDayService tds : existingAssociations) {
                    // Also delete any service pax pricing entries
                    List<ServicePaxPricing> pricings = servicePaxPricingRepository.findByTourDayServiceId(tds.getId());
                    if (!pricings.isEmpty()) {
                        servicePaxPricingRepository.deleteAll(pricings);
                    }
                    tourDayServiceRepository.delete(tds);
                }
                // Flush the changes to ensure they're committed
                servicePaxPricingRepository.flush();
                tourDayServiceRepository.flush();
            }

            // 6. Handle service provider change if requested
            if (request.getServiceProviderId() != null &&
                    (service.getServiceProvider() == null || !request.getServiceProviderId().equals(service.getServiceProvider().getId()))) {

                // Create a final copy of variables needed in lambda
                final Long categoryId = service.getServiceCategory().getId();
                final Long providerIdToUse = request.getServiceProviderId();

                List<Service> availableServices = serviceRepository.findByServiceProviderIdAndCategoryId(
                        providerIdToUse, categoryId);

                if (!availableServices.isEmpty()) {
                    // Select the first available service
                    service = availableServices.get(0);
                } else {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST, NO_SERVICES_AVAILABLE);
                }
            }

            // Store service ID for lambda use
            final Long serviceId = service.getId();

            // 7. Update tour day location if requested
            if (request.getLocationId() != null &&
                    (tourDay.getLocation() == null || !request.getLocationId().equals(tourDay.getLocation().getId()))) {

                final Long locationIdToUse = request.getLocationId();

                Location location = locationRepository.findById(locationIdToUse)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Location not found with id: " + locationIdToUse));

                tourDay.setLocation(location);
                tourDayRepository.save(tourDay);
            }

            // 8. Update service-specific details
            String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;

            if (HOTEL.equalsIgnoreCase(categoryName) && request.getRoomDetail() != null) {
                Room room = roomRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Room not found for service id: " + serviceId));

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
            else if (RESTAURANT.equalsIgnoreCase(categoryName) && request.getMealDetail() != null) {
                Meal meal = mealRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Meal not found for service id: " + serviceId));

                if (request.getMealDetail().getType() != null) {
                    meal.setType(MealType.valueOf(request.getMealDetail().getType()));
                }

                if (request.getMealDetail().getMealDetail() != null) {
                    meal.setMealDetail(request.getMealDetail().getMealDetail());
                }

                mealRepository.save(meal);
            }
            else if (TRANSPORT.equalsIgnoreCase(categoryName) && request.getTransportDetail() != null) {
                Transport transport = transportRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Transport not found for service id: " + serviceId));

                if (request.getTransportDetail().getSeatCapacity() != null) {
                    transport.setSeatCapacity(request.getTransportDetail().getSeatCapacity());
                }

                transportRepository.save(transport);
            }

            // 9. Handle creating tour day service and pax associations
            TourDayService mainTourDayService = null;

            // First, create a main TourDayService without specific pax association
            TourDayService tourDayService = new TourDayService();
            tourDayService.setTourDay(tourDay);
            tourDayService.setService(service);
            tourDayService.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);

            // Set selling price if provided, otherwise use service's default price
            if (request.getSellingPrice() != null) {
                tourDayService.setSellingPrice(request.getSellingPrice());
            } else {
                tourDayService.setSellingPrice(service.getSellingPrice());
            }

            mainTourDayService = tourDayServiceRepository.save(tourDayService);

            // Get all pax options for this tour
            List<TourPax> allPaxOptions = tourPaxRepository.findByTourIdOrderByMinPax(tourId);

            // If specific pax pricing is provided
            if (request.getPaxPrices() != null && !request.getPaxPrices().isEmpty()) {
                // Process each pax price and create associations in service_pax_pricing
                for (Map.Entry<String, Double> entry : request.getPaxPrices().entrySet()) {
                    Long paxId;
                    try {
                        paxId = Long.parseLong(entry.getKey());
                    } catch (NumberFormatException e) {
                        throw BusinessException.of(HttpStatus.BAD_REQUEST, "Invalid pax ID format: " + entry.getKey());
                    }

                    // Find the tourPax entity by ID
                    TourPax tourPax = tourPaxRepository.findById(paxId)
                            .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Tour pax not found with id: " + paxId));

                    // Verify this tourPax belongs to the current tour
                    if (!tourPax.getTour().getId().equals(tourId)) {
                        throw BusinessException.of(HttpStatus.BAD_REQUEST,
                                "Tour pax with id " + paxId + " does not belong to tour id " + tourId);
                    }

                    // Create association in service_pax_pricing table
                    ServicePaxPricing paxPricing = ServicePaxPricing.builder()
                            .tourDayService(mainTourDayService)
                            .tourPax(tourPax)
                            .build();

                    servicePaxPricingRepository.save(paxPricing);

                    // Update the TourPax selling price if needed
                    tourPax.setSellingPrice(entry.getValue());
                    tourPaxRepository.save(tourPax);
                }
            } else {
                // If no specific pricing, create associations for all pax options
                for (TourPax pax : allPaxOptions) {
                    ServicePaxPricing paxPricing = ServicePaxPricing.builder()
                            .tourDayService(mainTourDayService)
                            .tourPax(pax)
                            .build();

                    servicePaxPricingRepository.save(paxPricing);
                }
            }
            // Flush all changes to ensure they're committed to the database
            entityManager.flush();
            // Return the updated service details
            return getServiceDetail(tourId, service.getId());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, SERVICE_CREATE_FAIL, ex);
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
                // Use EntityManager to find all tourDayServices for this service and tour
                List<TourDayService> allTourDayServices = entityManager.createQuery(
                                "SELECT tds FROM TourDayService tds " +
                                        "JOIN tds.tourDay td " +
                                        "WHERE tds.service.id = :serviceId " +
                                        "AND td.tour.id = :tourId", TourDayService.class)
                        .setParameter("serviceId", serviceId)
                        .setParameter("tourId", tourId)
                        .getResultList();

                if (allTourDayServices.isEmpty()) {
                    throw BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_ASSOCIATED);
                }

                // Find the main TourDayService
                tourDayService = allTourDayServices.get(0);
                tourDay = tourDayService.getTourDay();
                service = currentService;

                // Update day number if provided
                if (request.getDayNumber() != null && !request.getDayNumber().equals(tourDay.getDayNumber())) {
                    TourDay newTourDay = tourDayRepository.findByTourIdAndDayNumber(tourId, request.getDayNumber())
                            .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_DAY_NOT_FOUND + ": " + request.getDayNumber()));

                    // Update the tour day service to the new tour day
                    tourDayService.setTourDay(newTourDay);
                    tourDayServiceRepository.save(tourDayService);

                    tourDay = newTourDay;
                }

                // Update pax-specific pricing if provided
                if (request.getPaxPrices() != null && !request.getPaxPrices().isEmpty()) {
                    // First, check and delete existing service pax pricing entries
                    List<ServicePaxPricing> existingPricings = servicePaxPricingRepository.findByTourDayServiceId(tourDayService.getId());
                    if (!existingPricings.isEmpty()) {
                        servicePaxPricingRepository.deleteAll(existingPricings);
                        servicePaxPricingRepository.flush();
                    }

                    // Create new service pax pricing entries for each pax
                    for (Map.Entry<Long, Double> entry : request.getPaxPrices().entrySet()) {
                        Long paxId = entry.getKey();

                        // Find the tourPax entity
                        TourPax tourPax = tourPaxRepository.findById(paxId)
                                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Tour pax not found with id: " + paxId));

                        // Verify this tourPax belongs to the current tour
                        if (!tourPax.getTour().getId().equals(tourId)) {
                            throw BusinessException.of(HttpStatus.BAD_REQUEST,
                                    "Tour pax with id " + paxId + " does not belong to tour id " + tourId);
                        }

                        // Create a new association in service_pax_pricing
                        ServicePaxPricing paxPricing = ServicePaxPricing.builder()
                                .tourDayService(tourDayService)
                                .tourPax(tourPax)
                                .build();

                        servicePaxPricingRepository.save(paxPricing);

                        // Also update the selling price in the TourPax entity
                        tourPax.setSellingPrice(entry.getValue());
                        tourPaxRepository.save(tourPax);
                    }

                    // Calculate average price for the TourDayService
                    Double avgPrice = request.getPaxPrices().values().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(tourDayService.getSellingPrice() != null ?
                                    tourDayService.getSellingPrice() : service.getSellingPrice());

                    tourDayService.setSellingPrice(avgPrice);
                } else if (request.getSellingPrice() != null) {
                    // Update the selling price of the TourDayService
                    tourDayService.setSellingPrice(request.getSellingPrice());
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

                // Set selling price if provided
                if (request.getSellingPrice() != null) {
                    tourDayService.setSellingPrice(request.getSellingPrice());
                } else {
                    tourDayService.setSellingPrice(service.getSellingPrice());
                }

                // Save the base service
                tourDayService = tourDayServiceRepository.save(tourDayService);

                // Get all pax options for this tour
                List<TourPax> allPaxOptions = tourPaxRepository.findByTourIdOrderByMinPax(tourId);

                // Handle pax-specific pricing if provided
                if (request.getPaxPrices() != null && !request.getPaxPrices().isEmpty()) {
                    for (Map.Entry<Long, Double> entry : request.getPaxPrices().entrySet()) {
                        Long paxId = entry.getKey();

                        // Find the tourPax entity
                        TourPax tourPax = tourPaxRepository.findById(paxId)
                                .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Tour pax not found with id: " + paxId));

                        // Verify this tourPax belongs to the current tour
                        if (!tourPax.getTour().getId().equals(tourId)) {
                            throw BusinessException.of(HttpStatus.BAD_REQUEST,
                                    "Tour pax with id " + paxId + " does not belong to tour id " + tourId);
                        }

                        // Create association in service_pax_pricing
                        ServicePaxPricing paxPricing = ServicePaxPricing.builder()
                                .tourDayService(tourDayService)
                                .tourPax(tourPax)
                                .build();

                        servicePaxPricingRepository.save(paxPricing);

                        // Update the TourPax selling price
                        tourPax.setSellingPrice(entry.getValue());
                        tourPaxRepository.save(tourPax);
                    }
                } else {
                    // If no specific pricing provided, create associations for all pax options
                    for (TourPax pax : allPaxOptions) {
                        ServicePaxPricing paxPricing = ServicePaxPricing.builder()
                                .tourDayService(tourDayService)
                                .tourPax(pax)
                                .build();

                        servicePaxPricingRepository.save(paxPricing);
                    }
                }
            } else {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Either serviceId parameter or serviceId in request body is required");
            }

            // 4. If service provider or location changed, updating an existing service
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

            if (HOTEL.equalsIgnoreCase(categoryName) && request.getRoomDetail() != null) {
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
            else if (RESTAURANT.equalsIgnoreCase(categoryName) && request.getMealDetail() != null) {
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
            else if (TRANSPORT.equalsIgnoreCase(categoryName) && request.getTransportDetail() != null) {
                final Service transportService = service;
                Transport transport = transportRepository.findByServiceId(transportService.getId())
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, "Transport not found for service id: " + transportService.getId()));

                if (request.getTransportDetail().getSeatCapacity() != null) {
                    transport.setSeatCapacity(request.getTransportDetail().getSeatCapacity());
                }
                transportRepository.save(transport);
            }

            // 6. Save the tour day service entry
            tourDayService = tourDayServiceRepository.save(tourDayService);

            // 7. Flush all changes to ensure they're committed to the database
            entityManager.flush();

            // 8. Return updated service details
            return getServiceDetail(tourId, tourDayService.getService().getId());
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, SERVICE_UPDATE_FAIL, ex);
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
    public GeneralResponse<List<Integer>> getDayNumbersByServiceAndTour(Long tourId, Long serviceId) {
        try {
            // 1. Validate tour exists
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TOUR_NOT_FOUND + " with id: " + tourId));

            // 2. Validate service exists
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND + " with id: " + serviceId));

            // 3. Get all TourDayService entries for this service and tour
            Optional<TourDayService> tourDayServices = tourDayServiceRepository.findByServiceIdAndTourDayTourId(serviceId, tourId);

            // 4. If no services found, return empty result
            if (tourDayServices.isEmpty()) {
                return new GeneralResponse<>(HttpStatus.OK.value(), "No day numbers found for this service in the tour", List.of());
            }

            // 5. Extract day numbers and sort them
            List<Integer> dayNumbers = tourDayServices.stream()
                    .map(tds -> tds.getTourDay().getDayNumber())
                    .sorted()
                    .collect(Collectors.toList());

            // 6. Build response
            return new GeneralResponse<>(HttpStatus.OK.value(), "Day numbers retrieved successfully", dayNumbers);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve day numbers", ex);
        }
    }

    @Override
    public GeneralResponse<ServiceProviderServicesDTO> getServicesByProviderAndCategory(Long providerId, String categoryName, Long locationId) {
        try {
            // 1. Validate service provider exists
            ServiceProvider provider = serviceProviderRepository.findById(providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND,
                            SERVICE_PROVIDER_NOT_FOUND + " with id: " + providerId));
            // 2. Validate service category exists
            ServiceCategory category = serviceCategoryRepository.findByCategoryName(categoryName)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND,
                            SERVICE_CATEGORY_NOT_FOUND + " with name: " + categoryName));
            // 3. Validate location exists
            Location location = locationRepository.findById(locationId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND,
                            LOCATION_NOT_FOUND + " with id: " + locationId));
            // 4. Get services by provider, category, and location
            List<Service> services = serviceRepository.findByServiceCategoryNameAndProviderIdAndLocationId(
                    categoryName, providerId, locationId);
            // 5. Convert to DTOs with type-specific details
            List<AvailableServiceDTO> availableServices = buildAvailableServicesDTO(services);
            // 6. Build response
            ServiceProviderServicesDTO response = ServiceProviderServicesDTO.builder()
                    .providerId(providerId)
                    .providerName(provider.getName())
                    .categoryId(category.getId())
                    .categoryName(category.getCategoryName())
                    .locationId(locationId)
                    .locationName(location.getName())
                    .availableServices(availableServices)
                    .build();
            return new GeneralResponse<>(HttpStatus.OK.value(), PROVIDER_CATEGORY_SERVICES_LOAD_SUCCESS, response);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.of(HttpStatus.INTERNAL_SERVER_ERROR, PROVIDER_CATEGORY_SERVICES_LOAD_FAIL, ex);
        }
    }

    private List<AvailableServiceDTO> buildAvailableServicesDTO(List<Service> services) {
        List<AvailableServiceDTO> availableServices = new ArrayList<>();

        for (Service service : services) {
            String status = determineServiceStatus(service.getStartDate(), service.getEndDate());
            String categoryName = service.getServiceCategory() != null ? service.getServiceCategory().getCategoryName() : null;

            // Get type-specific details based on service category
            RoomDetailDTO roomDetail = null;
            MealDetailDTO mealDetail = null;
            TransportDetailDTO transportDetail = null;

            if (HOTEL.equalsIgnoreCase(categoryName)) {
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
            } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                Optional<Meal> mealOpt = mealRepository.findByServiceIdAndDeletedFalse(service.getId());
                if (mealOpt.isPresent()) {
                    Meal meal = mealOpt.get();
                    mealDetail = MealDetailDTO.builder()
                            .id(meal.getId())
                            .type(meal.getType().name())
                            .mealDetail(meal.getMealDetail())
                            .build();
                }
            } else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
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
                    .providerId(service.getServiceProvider() != null ? service.getServiceProvider().getId() : null)
                    .providerName(service.getServiceProvider() != null ? service.getServiceProvider().getName() : null)
                    .roomDetail(roomDetail)
                    .mealDetail(mealDetail)
                    .transportDetail(transportDetail)
                    .build();

            availableServices.add(serviceDTO);
        }

        return availableServices;
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
