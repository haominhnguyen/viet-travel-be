package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.ServiceRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicActivityDTO;
import com.fpt.capstone.tourism.dto.response.ServiceResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.ServiceService;
import jakarta.persistence.criteria.Expression;
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
import org.springframework.security.access.method.P;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceBaseMapper serviceBaseMapper;
    private final ServiceFullMapper serviceFullMapper;
    private final ServiceDetailMapper serviceDetailMapper;
    private final TourDayServiceMapper tourDayServiceMapper;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final RoomRepository roomRepository;
    private final MealRepository mealRepository;
    private final TransportRepository transportRepository;
    private final UserRepository userRepository;
    private final TourBookingServiceRepository bookingServiceRepository;
    private final RoomMapper roomMapper;
    private final MealMapper mealMapper;
    private final ServiceMapper serviceMapper;
    private final TransportMapper transportMapper;
    private final TourBookingServiceMapper bookingServiceMapper;

    @Override
    public GeneralResponse<PagingDTO<List<ServiceBaseDTO>>> getAllServices(
            int page, int size, String keyword, Boolean isDeleted, String sortField,
            String sortDirection, Long providerId) {
        try {
            // Validate sortField to prevent invalid field names
            List<String> allowedSortFields = Arrays.asList("id", "createdAt", "name");
            if (!allowedSortFields.contains(sortField)) {
                sortField = "createdAt";
            }

            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Service> spec = buildSearchSpecification(keyword, isDeleted, providerId);

            Page<Service> servicePage = serviceRepository.findAll(spec, pageable);
            List<ServiceBaseDTO> serviceDTOs = servicePage.getContent().stream()
                    .map(serviceBaseMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(servicePage, serviceDTOs);
        } catch (Exception ex) {
            throw BusinessException.of("Failed to retrieve services", ex);
        }
    }


    public GeneralResponse<List<TourDayServiceDTO>> getTourDayServicesByServiceId(Long serviceId, Long providerId) {
        try {
            Service service = serviceRepository.findByIdAndProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            List<TourDayServiceDTO> tourDayServices = service.getTourDayServices()
                    .stream()
                    .map(tourDayServiceMapper::toDTO)
                    .collect(Collectors.toList());

            return GeneralResponse.of(tourDayServices, TOUR_DAY_SERVICES_RETRIEVED);
        } catch (BusinessException e) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, GET_TOUR_DAY_SERVICE_FAIL);
        }
    }

    public GeneralResponse<Object> getServiceDetailsByServiceId(Long serviceId, Long providerId) {
        try {
            // Fetch the service first
            Service service = serviceRepository.findByIdAndProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            String categoryName = service.getServiceCategory().getCategoryName();

            // Based on category name, fetch the appropriate details
            if (HOTEL.equalsIgnoreCase(categoryName)) {
                Room room = roomRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, ROOM_NOT_FOUND));
                return GeneralResponse.of(roomMapper.toDTO(room), SERVICE_DETAILS_RETRIEVED);
            } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                Meal meal = mealRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, MEAL_NOT_FOUND));
                return GeneralResponse.of(mealMapper.toDTO(meal), SERVICE_DETAILS_RETRIEVED);
            } else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
                Transport transport = transportRepository.findByServiceId(serviceId)
                        .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, TRANSPORT_NOT_FOUND));
                return GeneralResponse.of(transportMapper.toDTO(transport), SERVICE_DETAILS_RETRIEVED);
            } else {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, SERVICE_CATEGORY_NOT_FOUND);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, GET_SERVICE_DETAIL_FAIL);
        }
    }


    private Specification buildSearchSpecification(
            String keyword,
            Boolean isDeleted,
            Long providerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Filter by providerId
            predicates.add(cb.equal(root.get("serviceProvider").get("id"), providerId));
            // Search by name (ignoring accents and case)
            if (keyword != null && !keyword.trim().isEmpty()) {
                Expression<String> normalizedKeyword = cb.function(
                        "unaccent",
                        String.class,
                        cb.literal(keyword.toLowerCase())
                );
                Expression<String> normalizedName = cb.function(
                        "unaccent",
                        String.class,
                        cb.lower(root.get("name"))
                );

                Predicate namePredicate = cb.like(
                        normalizedName,
                        cb.concat("%", cb.concat(normalizedKeyword, "%"))
                );
                predicates.add(namePredicate);
            }

            // Filter by deletion status
            if (isDeleted != null) {
                predicates.add(cb.equal(root.get("deleted"), isDeleted));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private GeneralResponse<PagingDTO<List<ServiceBaseDTO>>> buildPagedResponse(Page<Service> servicePage, List<ServiceBaseDTO> serviceDTOs) {
        PagingDTO<List<ServiceBaseDTO>> pagingDTO = PagingDTO.<List<ServiceBaseDTO>>builder()
                .page(servicePage.getNumber())
                .size(servicePage.getSize())
                .total(servicePage.getTotalElements())
                .items(serviceDTOs)
                .build();
        return GeneralResponse.of(pagingDTO, SERVICE_RETRIEVE_SUCCESS);
    }

    @Override
    public GeneralResponse<ServiceResponseDTO> createService(ServiceRequestDTO requestDTO, Long providerId) {
        try {
            // Validate service category
            ServiceCategory category = serviceCategoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_CATEGORY_NOT_FOUND));

            String categoryName = category.getCategoryName();

            // Validate service provider
            ServiceProvider provider = serviceProviderRepository.findById(providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_PROVIDER_NOT_FOUND));

            // Validate common fields
            Validator.validateDates(requestDTO.getStartDate(), requestDTO.getEndDate());
            //Validator.validatePrices(requestDTO.getNettPrice(), requestDTO.getSellingPrice());

            // Only validate service details for categories that have detail tables
            if (!ACTIVITY.equalsIgnoreCase(categoryName)) {
                Validator.validateServiceDetails(requestDTO, categoryName);
            }

            if (serviceRepository.existsByNameAndServiceProviderId(requestDTO.getName(), providerId)) {
                throw BusinessException.of(HttpStatus.CONFLICT, SERVICE_NAME_EXISTS);
            }

            // Create and save the service entity
            Service service = serviceFullMapper.toEntity(requestDTO);
            service.setServiceCategory(category);
            service.setServiceProvider(provider);
            service.setDeleted(false);
            service.setCreatedAt(LocalDateTime.now());

            Service savedService = serviceRepository.save(service);

            // Handle category-specific details
            if (HOTEL.equalsIgnoreCase(categoryName)) {
                Room room = new Room();
                room.setService(savedService);
                room.setCapacity(requestDTO.getRoomDetails().getCapacity());
                room.setAvailableQuantity(requestDTO.getRoomDetails().getAvailableQuantity());
                room.setFacilities(requestDTO.getRoomDetails().getFacilities());
                room.setDeleted(false);
                room.setCreatedAt(LocalDateTime.now());
                roomRepository.save(room);
            } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                Meal meal = new Meal();
                meal.setService(savedService);
                meal.setType(requestDTO.getMealDetails().getType());
                meal.setMealDetail(requestDTO.getMealDetails().getMealDetail());
                meal.setDeleted(false);
                meal.setCreatedAt(LocalDateTime.now());
                mealRepository.save(meal);
            } else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
                Transport transport = new Transport();
                transport.setService(savedService);
                transport.setSeatCapacity(requestDTO.getTransportDetails().getSeatCapacity());
                transport.setDeleted(false);
                transport.setCreatedAt(LocalDateTime.now());
                transportRepository.save(transport);
            } else if (ACTIVITY.equalsIgnoreCase(categoryName)) {
            }else if(TICKET.equalsIgnoreCase(categoryName)){}
            else {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Unsupported service category");
            }
            ServiceResponseDTO responseDTO = createFullResponseDTO(savedService, categoryName);
            return GeneralResponse.of(responseDTO, SERVICE_CREATED);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(CREATE_SERVICE_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<ServiceResponseDTO> updateService(Long serviceId, ServiceRequestDTO requestDTO, Long providerId) {
        try {
            // Validate service
            Service service = serviceRepository.findByIdAndServiceProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            // Get the service category
            ServiceCategory category = serviceCategoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_CATEGORY_NOT_FOUND));

            String categoryName = category.getCategoryName();

            // Validate dates
            Validator.validateDates(requestDTO.getStartDate(), requestDTO.getEndDate());
            // Validate prices
            Validator.validatePrices(requestDTO.getNettPrice(), requestDTO.getSellingPrice());

            // Validate service type-specific details only for categories that have detail tables
            if (!ACTIVITY.equalsIgnoreCase(categoryName)) {
                Validator.validateServiceDetails(requestDTO, categoryName);
            }

            // Check if service with the same name exists (excluding current service)
            if (serviceRepository.existsByNameAndServiceProviderIdAndIdNot(
                    requestDTO.getName(), providerId, serviceId)) {
                throw BusinessException.of(HttpStatus.CONFLICT, SERVICE_NAME_EXISTS);
            }

            // Update common fields
            service.setName(requestDTO.getName());
            service.setNettPrice(requestDTO.getNettPrice());
            service.setSellingPrice(requestDTO.getSellingPrice());
            service.setImageUrl(requestDTO.getImageUrl());
            service.setStartDate(requestDTO.getStartDate());
            service.setEndDate(requestDTO.getEndDate());
            service.setServiceCategory(category);
            service.setUpdatedAt(LocalDateTime.now());

            // Save the updated service
            Service updatedService = serviceRepository.save(service);

            // Update specific service details based on category
            if (HOTEL.equalsIgnoreCase(categoryName)) {
                Room room = roomRepository.findByServiceId(serviceId)
                        .orElseGet(() -> {
                            Room newRoom = new Room();
                            newRoom.setService(updatedService);
                            newRoom.setDeleted(false);
                            newRoom.setCreatedAt(LocalDateTime.now());
                            return newRoom;
                        });

                room.setCapacity(requestDTO.getRoomDetails().getCapacity());
                room.setAvailableQuantity(requestDTO.getRoomDetails().getAvailableQuantity());
                room.setFacilities(requestDTO.getRoomDetails().getFacilities());
                room.setUpdatedAt(LocalDateTime.now());
                roomRepository.save(room);
            } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                Meal meal = mealRepository.findByServiceId(serviceId)
                        .orElseGet(() -> {
                            Meal newMeal = new Meal();
                            newMeal.setService(updatedService);
                            newMeal.setDeleted(false);
                            newMeal.setCreatedAt(LocalDateTime.now());
                            return newMeal;
                        });

                meal.setType(requestDTO.getMealDetails().getType());
                meal.setMealDetail(requestDTO.getMealDetails().getMealDetail());
                meal.setUpdatedAt(LocalDateTime.now());
                mealRepository.save(meal);
            } else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
                Transport transport = transportRepository.findByServiceId(serviceId)
                        .orElseGet(() -> {
                            Transport newTransport = new Transport();
                            newTransport.setService(updatedService);
                            newTransport.setDeleted(false);
                            newTransport.setCreatedAt(LocalDateTime.now());
                            return newTransport;
                        });

                transport.setSeatCapacity(requestDTO.getTransportDetails().getSeatCapacity());
                transport.setUpdatedAt(LocalDateTime.now());
                transportRepository.save(transport);
            } else if (ACTIVITY.equalsIgnoreCase(categoryName)) {
            } else {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Unsupported service category");
            }
            // Create a response DTO that includes all details
            ServiceResponseDTO responseDTO = createFullResponseDTO(updatedService, categoryName);

            return GeneralResponse.of(responseDTO, SERVICE_UPDATED);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(UPDATE_SERVICE_FAIL, ex);
        }
    }

    // Helper method to create a full response DTO with all details
    private ServiceResponseDTO createFullResponseDTO(Service service, String categoryName) {
        ServiceResponseDTO responseDTO = serviceFullMapper.toResponseDTO(service);

        if (HOTEL.equalsIgnoreCase(categoryName)) {
            Room room = roomRepository.findByServiceId(service.getId()).orElse(null);
            if (room != null) {
                responseDTO.setRoomDetails(roomMapper.toDTO(room));
            }
        } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
            Meal meal = mealRepository.findByServiceId(service.getId()).orElse(null);
            if (meal != null) {
                responseDTO.setMealDetails(mealMapper.toDTO(meal));
            }
        } else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
            Transport transport = transportRepository.findByServiceId(service.getId()).orElse(null);
            if (transport != null) {
                responseDTO.setTransportDetails(transportMapper.toDTO(transport));
            }
        }
        return responseDTO;
    }


    @Override
    public GeneralResponse<ServiceResponseDTO> changeServiceStatus(Long serviceId, Boolean isDeleted, Long providerId) {
        try {
            // Validate service
            Service service = serviceRepository.findByIdAndServiceProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            // Get the service category name
            String categoryName = service.getServiceCategory().getCategoryName();

            // Update service status
            service.setDeleted(isDeleted);
            service.setUpdatedAt(LocalDateTime.now());
            Service updatedService = serviceRepository.save(service);

            // Update status of associated service details based on category
            if (HOTEL.equalsIgnoreCase(categoryName)) {
                Room room = roomRepository.findByServiceId(serviceId).orElse(null);
                if (room != null) {
                    room.setDeleted(isDeleted);
                    room.setUpdatedAt(LocalDateTime.now());
                    roomRepository.save(room);
                }
            } else if (RESTAURANT.equalsIgnoreCase(categoryName)) {
                Meal meal = mealRepository.findByServiceId(serviceId).orElse(null);
                if (meal != null) {
                    meal.setDeleted(isDeleted);
                    meal.setUpdatedAt(LocalDateTime.now());
                    mealRepository.save(meal);
                }
            } else if (TRANSPORT.equalsIgnoreCase(categoryName)) {
                Transport transport = transportRepository.findByServiceId(serviceId).orElse(null);
                if (transport != null) {
                    transport.setDeleted(isDeleted);
                    transport.setUpdatedAt(LocalDateTime.now());
                    transportRepository.save(transport);
                }
            }

            String messageCode = isDeleted ? SERVICE_DELETED : SERVICE_RESTORED;

            // Create a response DTO that includes all details
            ServiceResponseDTO responseDTO = createFullResponseDTO(updatedService, categoryName);

            return GeneralResponse.of(responseDTO, messageCode);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(CHANGE_SERVICE_STATUS_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<?> getListServiceRequest(int page, int size, String keyword, TourBookingServiceStatus status, String orderDate) {
        try {
            Long currentUserId = getCurrentUserProviderId();

            //Find provider
            ServiceProvider serviceProvider = serviceProviderRepository.findByUserId(currentUserId).orElseThrow(
                    () -> BusinessException.of("Không tìm thấy nhà cung cấp")
            );
            Long currentProviderId = serviceProvider.getId();
            Sort sort = "asc".equalsIgnoreCase(orderDate) ?
                    Sort.by("requestDate").ascending() :
                    Sort.by("requestDate").descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Build Specification with providerId filter
            Specification<TourBookingService> spec = buildSearchSpecificationForService(keyword, status)
                    .and((root, query, criteriaBuilder) -> {
                        Join<TourBookingService, Service> serviceJoin = root.join("service"); // Join bảng Service
                        Join<com.fpt.capstone.tourism.model.Service, ServiceProvider> providerJoin = serviceJoin.join("serviceProvider"); // Join bảng Provider
                        return criteriaBuilder.equal(providerJoin.get("id"), currentProviderId); // Lọc theo providerId
                    });
//            Specification<TourBookingService> spec = buildSearchSpecificationForService(keyword, status);

            Page<TourBookingService> bookingServicePage = bookingServiceRepository.findAll(spec, pageable);
            List<ServiceProviderBookingServiceDTO> resultDTO = bookingServicePage.getContent().stream()
                    .map(bookingServiceMapper::toProviderBookingServiceDTO)
                    .collect(Collectors.toList());

            return buildPagedResponseService(bookingServicePage, resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> approveService(Long tourBookingServiceId) {
        try {
            TourBookingService bookingService = bookingServiceRepository.findById(tourBookingServiceId).orElseThrow(
                    () -> BusinessException.of("No booking service found")
            );

            //Kiểm tra đơn hàng có phải của nhà cung cấp không
            Long currentUserId = getCurrentUserProviderId();
            Service service = bookingService.getService();
            ServiceProvider currentProvider = serviceProviderRepository.findByUserId(currentUserId).orElseThrow(
                    () -> BusinessException.of("No provider found")
            );
            if (!service.getServiceProvider().getId().equals(currentProvider.getId())) {
                throw BusinessException.of("Unauthorized");
            }

            //Kiểm tra xem đã quá hạn ngày yêu cầu chưa
            LocalDateTime currentDateTime = LocalDateTime.now();
            if (currentDateTime.isAfter(bookingService.getRequestDate())) {
                throw BusinessException.of("Booking service has expired");
            }

            //Chỉ có thể approve khi đơn hàng là pending
            if (bookingService.getStatus().equals(TourBookingServiceStatus.PENDING)) {

                //Nếu là đơn hàng yêu cầu update số lượng
                Integer updateQuantity = bookingService.getRequestedQuantity();
                if (updateQuantity > 0) {
                    bookingService.setCurrentQuantity(updateQuantity);
                    bookingService.setRequestedQuantity(0);
                }
                bookingService.setStatus(TourBookingServiceStatus.APPROVED);
                bookingServiceRepository.save(bookingService);

                //Map to DTO
                ServiceProviderBookingServiceDTO resultDTO = bookingServiceMapper.toProviderBookingServiceDTO(bookingService);
                return new GeneralResponse<>(HttpStatus.OK.value(), "Approve service success", resultDTO);
            }

            return new GeneralResponse<>(HttpStatus.FORBIDDEN.value(), "Forbidden", tourBookingServiceId);
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> rejectService(Long tourBookingServiceId) {
        try {
            TourBookingService bookingService = bookingServiceRepository.findById(tourBookingServiceId).orElseThrow(
                    () -> BusinessException.of("No booking service found")
            );

            //Kiểm tra đơn hàng có phải của nhà cung cấp không
            Long currentUserId = getCurrentUserProviderId();
            Service service = bookingService.getService();
            ServiceProvider currentProvider = serviceProviderRepository.findByUserId(currentUserId).orElseThrow(
                    () -> BusinessException.of("No provider found")
            );
            if (!service.getServiceProvider().getId().equals(currentProvider.getId())) {
                throw BusinessException.of("Unauthorized");
            }

            //Kiểm tra xem đã quá hạn ngày yêu cầu chưa
            LocalDateTime currentDateTime = LocalDateTime.now();
            if (currentDateTime.isAfter(bookingService.getRequestDate())) {
                throw BusinessException.of("Booking service has expired");
            }

            //Chỉ có thể reject khi đơn hàng là pending
            if (bookingService.getStatus().equals(TourBookingServiceStatus.PENDING)) {

                bookingService.setStatus(TourBookingServiceStatus.REJECTED);
                bookingServiceRepository.save(bookingService);

                //Map to DTO
                ServiceProviderBookingServiceDTO resultDTO = bookingServiceMapper.toProviderBookingServiceDTO(bookingService);
                return new GeneralResponse<>(HttpStatus.OK.value(), "Reject service success", resultDTO);
            }

            return new GeneralResponse<>(HttpStatus.FORBIDDEN.value(), "Forbidden", tourBookingServiceId);
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }

    @Override
    public GeneralResponse<?> getServiceRequestDetail(Long tourBookingServiceId) {
        try {
            TourBookingService bookingService = bookingServiceRepository.findByIdWithDetails(tourBookingServiceId)
                    .orElseThrow(() -> BusinessException.of("No booking service found"));

            //Kiểm tra đơn hàng có phải của nhà cung cấp không
            Long currentUserId = getCurrentUserProviderId();
            Service service = bookingService.getService();
            ServiceProvider currentProvider = serviceProviderRepository.findByUserId(currentUserId).orElseThrow(
                    () -> BusinessException.of("No provider found")
            );
            if (!service.getServiceProvider().getId().equals(currentProvider.getId())) {
                throw BusinessException.of("Unauthorized");
            }

            //Kiểm tra trạng thái đơn hàng
            List<TourBookingServiceStatus> allowedStatuses = Arrays.asList(
                    TourBookingServiceStatus.PENDING,
                    TourBookingServiceStatus.REJECTED,
                    TourBookingServiceStatus.APPROVED
            );

            if (!allowedStatuses.contains(bookingService.getStatus())) {
                throw BusinessException.of("You are not allowed to view this order.");
            }

            TourBooking booking = bookingService.getBooking();
            Tour tour = (booking != null) ? booking.getTour() : null;
            TourSchedule tourSchedule = (booking != null) ? booking.getTourSchedule() : null;
            TourDay tourDay = bookingService.getTourDay();

            ChangeServiceDetailDTO resultDTO = ChangeServiceDetailDTO.builder()
                    .tourBookingServiceId(tourBookingServiceId)
                    .tourName((tour != null) ? tour.getName() : null)
                    .tourType((tour != null) ? tour.getTourType().toString() : null)
                    .startDate((tourSchedule != null) ? tourSchedule.getStartDate() : null)
                    .endDate((tourSchedule != null) ? tourSchedule.getEndDate() : null)
                    .dayNumber((tourDay != null) ? tourDay.getDayNumber() : null)
                    .status((bookingService.getStatus() != null) ? bookingService.getStatus().name() : null)
                    .reason(bookingService.getReason())
                    .updatedAt(bookingService.getUpdatedAt())
                    .serviceName(service.getName())
                    .nettPrice(service.getNettPrice())
                    .requestQuantity(bookingService.getRequestedQuantity())
                    .currentQuantity(bookingService.getCurrentQuantity())
                    .totalPrice(Optional.ofNullable(service)
                            .map(s -> s.getNettPrice() * bookingService.getCurrentQuantity())
                            .orElse(null))
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), "Xem chi tiết thành công", resultDTO);
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }

    @Override
    public List<PublicActivityDTO> findRecommendedActivities(int numberActivity) {
        try {
            List<Service> randomActivities = serviceRepository.findRandomActivities("Activity",
                    PageRequest.of(0, numberActivity)
            );
            return randomActivities.stream()
                    .map(serviceMapper::toPublicActivityDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw BusinessException.of("Fail", ex);
        }
    }

    private Specification<TourBookingService> buildSearchSpecificationForService(String keyword, TourBookingServiceStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                Join<TourBookingService, com.fpt.capstone.tourism.model.Service> serviceJoin =
                        root.join("service", JoinType.LEFT);
                Predicate namePredicate = cb.like(serviceJoin.get("name"), "%" + keyword + "%");
                predicates.add(namePredicate);
            }
            predicates.add(root.get("status").in(TourBookingServiceStatus.PENDING,
                    TourBookingServiceStatus.APPROVED,
                    TourBookingServiceStatus.REJECTED));
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private <T> GeneralResponse<PagingDTO<List<T>>> buildPagedResponseService(Page<TourBookingService> page, List<T> list) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .items(list)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
    }

    private Long getCurrentUserProviderId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> BusinessException.of("User not found"));
            return user.getId();
        }
        throw BusinessException.of("Không tìm thấy thông tin người dùng");
    }

}

