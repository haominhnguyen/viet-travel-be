package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicServiceProviderDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.PasswordGenerateImpl;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.*;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.Role;
import com.fpt.capstone.tourism.model.enums.TourBookingServiceStatus;
import com.fpt.capstone.tourism.repository.*;
import com.fpt.capstone.tourism.service.EmailConfirmationService;
import com.fpt.capstone.tourism.service.ServiceProviderService;
import com.fpt.capstone.tourism.service.UserService;
import jakarta.persistence.*;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.hibernate.grammars.hql.HqlParser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;

@Service
@RequiredArgsConstructor
public class ServiceProviderServiceImpl implements ServiceProviderService {

    private final ServiceProviderRepository serviceProviderRepository;
    private final LocationRepository locationRepository;
    private final ServiceProviderMapper serviceProviderMapper;
    private final ServiceCategoryMapper serviceCategoryMapper;
    private final LocationMapper locationMapper;
    private final GeoPositionMapper geoPositionMapper;
    private final TourBookingServiceMapper bookingServiceMapper;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final GeoPositionRepository geoPositionRepository;
    private final TourBookingServiceRepository bookingServiceRepository;
    private final EmailConfirmationService emailConfirmationService;
    private final UserService userService;
    private final PasswordGenerateImpl passwordGenerate;
    private final PasswordEncoder passwordEncoder;


    @Override
    public GeneralResponse<ServiceProviderDTO> save(ServiceProviderDTO serviceProviderDTO) {
        try{
            //Validate input data
            Validator.validateServiceProvider(serviceProviderDTO);

            //Check duplicate email and phone number
            if(serviceProviderRepository.findByEmail(serviceProviderDTO.getEmail()) != null){
                throw BusinessException.of(EMAIL_ALREADY_EXISTS_MESSAGE);
            }
            if(serviceProviderRepository.findByPhone(serviceProviderDTO.getPhone()) != null){
                throw BusinessException.of(PHONE_ALREADY_EXISTS_MESSAGE);
            }

            Location location = locationRepository.findById(serviceProviderDTO.getLocationId()).orElseThrow(null);
            GeoPosition geoPosition = GeoPosition.builder()
                    .longitude(serviceProviderDTO.getGeoPosition().getLongitude())
                    .latitude(serviceProviderDTO.getGeoPosition().getLatitude())
                    .deleted(false)
                    .build();
            geoPosition = geoPositionRepository.save(geoPosition);

            //Store data into database
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.setImageUrl( serviceProviderDTO.getImageUrl() );
            serviceProvider.setName( serviceProviderDTO.getName() );
            serviceProvider.setAbbreviation( serviceProviderDTO.getAbbreviation() );
            serviceProvider.setWebsite( serviceProviderDTO.getWebsite() );
            serviceProvider.setEmail( serviceProviderDTO.getEmail() );
            serviceProvider.setStar( serviceProviderDTO.getStar() );
            serviceProvider.setPhone( serviceProviderDTO.getPhone() );
            serviceProvider.setAddress( serviceProviderDTO.getAddress() );
            serviceProvider.setDeleted(false);
            serviceProvider.setLocation(location);
            serviceProvider.setGeoPosition( geoPosition);
            if (serviceProviderDTO.getServiceCategories() != null) {
                List<ServiceCategory> serviceCategory = serviceProviderDTO.getServiceCategories().stream()
                        .map(serviceCategoryMapper::toEntity
//                            ServiceCategoryDTO dto = new ServiceCategoryDTO();
//                            dto.setId(category.getId());
//                            dto.setCategoryName(category.getCategoryName());
//                            dto.setDeleted(category.getDeleted());
//                            return dto;
                        )
                        .toList();
                serviceProvider.setServiceCategories(serviceCategory);
            }
            serviceProvider.setId(null);
            serviceProvider.setCreatedAt(LocalDateTime.now());


            //Create account for service provider
            User serviceUser = createAccountServiceProvider(serviceProvider.getName(),
                    serviceProvider.getEmail(),
                    serviceProvider.getPhone(),
                    serviceProvider.getAddress());

            serviceProvider.setUser(serviceUser);
            serviceProviderRepository.save(serviceProvider);

            //Map to DTO
            ServiceProviderDTO resultDTO = ServiceProviderDTO.builder()
                    .id(serviceProvider.getId())
                    .imageUrl(serviceProvider.getImageUrl())
                    .name(serviceProvider.getName())
                    .abbreviation(serviceProvider.getAbbreviation())
                    .website(serviceProvider.getWebsite())
                    .email(serviceProvider.getEmail())
                    .star(serviceProvider.getStar())
                    .phone(serviceProvider.getPhone())
                    .address(serviceProvider.getAddress())
                    .deleted(serviceProvider.getDeleted())
                    .locationId(serviceProvider.getLocation().getId())
                    .geoPosition(geoPositionMapper.toDTO(serviceProvider.getGeoPosition()))
                    .serviceCategories(serviceProvider.getServiceCategories().stream().map(serviceCategoryMapper::toDTO).collect(Collectors.toList()))
                    .build();

            return new GeneralResponse<>(HttpStatus.OK.value(), CREATE_SERVICE_PROVIDER_SUCCESS, resultDTO);
        } catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(CREATE_SERVICE_PROVIDER_FAIL, ex);
        }

    }

    @Transactional
    @Override
    public GeneralResponse<?> getServiceProviderById(Long id) {
        try{
            ServiceProvider serviceProvider = serviceProviderRepository.findById(id).orElseThrow();
            ServiceProviderDetailDTO resultDTO = ServiceProviderDetailDTO.builder()
                    .id(serviceProvider.getId())
                    .imageUrl(serviceProvider.getImageUrl())
                    .name(serviceProvider.getName())
                    .abbreviation(serviceProvider.getAbbreviation())
                    .website(serviceProvider.getWebsite())
                    .email(serviceProvider.getEmail())
                    .star(serviceProvider.getStar())
                    .phone(serviceProvider.getPhone())
                    .address(serviceProvider.getAddress())
                    .deleted(serviceProvider.getDeleted())
                    .location(locationMapper.toPublicLocationSimpleDTO(serviceProvider.getLocation()))
                    .geoPosition(geoPositionMapper.toDTO(serviceProvider.getGeoPosition()))
                    .serviceCategories(serviceProvider.getServiceCategories().stream().map(serviceCategoryMapper::toDTO).collect(Collectors.toList()))
                    .build();
//            ServiceProviderDTO serviceProviderDTO = serviceProviderMapper.toDTO(serviceProvider);
            return new GeneralResponse<>(HttpStatus.OK.value(), GENERAL_SUCCESS_MESSAGE, resultDTO);
        } catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> getAllServiceProviders(int page, int size, String keyword, Boolean isDeleted, String orderDate) {
        try {
            Sort sort = "asc".equalsIgnoreCase(orderDate) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Specification<ServiceProvider> spec = buildSearchSpecification(keyword, isDeleted);

            Page<ServiceProvider> serviceProviderPage = serviceProviderRepository.findAll(spec, pageable);
            List<ServiceProviderDTO> serviceProviderDTOS = serviceProviderPage.getContent().stream()
                    .map(serviceProviderMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(serviceProviderPage, serviceProviderDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Tải tất cả nhà cung cấp thất bại", ex);
        }
    }


    @Override
    @Transactional
    public GeneralResponse<ServiceProviderDTO> updateServiceProvider(Long id, ServiceProviderDTO serviceProviderDTO) {
        try{
            //Find in database
            ServiceProvider serviceProvider = serviceProviderRepository.findById(id).orElseThrow();

            //Validate input data
            Validator.validateServiceProvider(serviceProviderDTO);

            //Update service provider information
            if(!serviceProviderDTO.getImageUrl().equals(serviceProvider.getImageUrl())){
                serviceProvider.setImageUrl(serviceProviderDTO.getImageUrl());
            }
            if(!serviceProviderDTO.getAbbreviation().equals(serviceProvider.getAbbreviation())){
                serviceProvider.setAbbreviation(serviceProviderDTO.getAbbreviation());
            }
            if(!serviceProviderDTO.getWebsite().equals(serviceProvider.getWebsite())){
                serviceProvider.setWebsite(serviceProviderDTO.getWebsite());
            }
            if(serviceProviderDTO.getStar() != serviceProvider.getStar()){
                serviceProvider.setStar(serviceProviderDTO.getStar());
            }
            if(!serviceProviderDTO.getName().equals(serviceProvider.getName())){
                serviceProvider.setName(serviceProviderDTO.getName());
            }
            if(!serviceProviderDTO.getEmail().equals(serviceProvider.getEmail())){
                //Check duplicate email
                if(serviceProviderRepository.findByEmail(serviceProviderDTO.getEmail()) != null){
                    throw BusinessException.of(EMAIL_ALREADY_EXISTS_MESSAGE);
                }
                serviceProvider.setEmail(serviceProviderDTO.getEmail());
            }
            if(!serviceProviderDTO.getPhone().equals(serviceProvider.getPhone())){
                if(serviceProviderRepository.findByPhone(serviceProviderDTO.getPhone()) != null){
                    throw BusinessException.of(PHONE_ALREADY_EXISTS_MESSAGE);
                }
                serviceProvider.setPhone(serviceProviderDTO.getPhone());
            }
            if(!serviceProviderDTO.getAddress().equals(serviceProvider.getAddress())){
                serviceProvider.setAddress(serviceProviderDTO.getAddress());
            }
            if(!serviceProviderDTO.getLocationId().equals(serviceProvider.getLocation().getId())){
                Location location = locationRepository.findById(serviceProviderDTO.getLocationId()).orElseThrow();
                serviceProvider.setLocation(location);
            }

//            if(!serviceProviderDTO.getGeoPosition().getId().equals(serviceProvider.getGeoPosition().getId())){
//                GeoPosition geoPosition = GeoPosition.builder()
//                        .latitude(serviceProviderDTO.getGeoPosition().getLatitude())
//                        .longitude(serviceProviderDTO.getGeoPosition().getLongitude()).build() ;
//                serviceProvider.setGeoPosition(geoPosition);
//            }
            if (serviceProviderDTO.getGeoPosition() != null) {
                Double newLatitude = serviceProviderDTO.getGeoPosition().getLatitude();
                Double newLongitude = serviceProviderDTO.getGeoPosition().getLongitude();

                // Kiểm tra nếu tọa độ thay đổi
                if (!newLatitude.equals(serviceProvider.getGeoPosition().getLatitude()) ||
                        !newLongitude.equals(serviceProvider.getGeoPosition().getLongitude())) {

                    // Kiểm tra xem GeoPosition đã tồn tại trong DB chưa
                    List<GeoPosition> existingGeo = geoPositionRepository.findByLatitudeAndLongitude(newLatitude, newLongitude);

                    GeoPosition geoPosition;
                    if (existingGeo.get(0) != null) {
                        geoPosition = existingGeo.get(0); // Nếu đã có, lấy ra dùng
                    } else {
                        geoPosition = GeoPosition.builder()
                                .latitude(newLatitude)
                                .longitude(newLongitude)
                                .build();
                        geoPosition = geoPositionRepository.save(geoPosition); // Lưu mới vào DB
                    }

                    serviceProvider.setGeoPosition(geoPosition);
                }
            }


//            String normalizedName = removeAccents(serviceProviderDTO.getLocationName().toLowerCase());
//            List<Location> location = locationRepository.findAll().stream()
//                    .filter(loc -> removeAccents(loc.getName().toLowerCase()).contains(normalizedName)).collect(Collectors.toList());

//            if(location.get(0) != null && (!location.get(0).getName().equals(serviceProvider.getLocation().getName()))) {
//                serviceProvider.setLocation(location.get(0));
//            }
            serviceProvider.setServiceCategories(serviceProviderDTO.getServiceCategories()
                    .stream().map(serviceCategoryMapper::toEntity).collect(Collectors.toList()));

            serviceProvider.setUpdatedAt(LocalDateTime.now());

            serviceProviderRepository.save(serviceProvider);

            serviceProviderDTO.setId(serviceProvider.getId());
            return new GeneralResponse<>(HttpStatus.OK.value(), UPDATE_SERVICE_PROVIDER_SUCCESS, serviceProviderDTO);
        } catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(UPDATE_SERVICE_PROVIDER_FAIL, ex);
        }

    }

    @Override
    @Transactional
    public GeneralResponse<ServiceProviderDTO> deleteServiceProvider(Long id, boolean isDeleted) {
        try{
            ServiceProvider serviceProvider = serviceProviderRepository.findById(id).orElseThrow();

            //Soft delete account of service provider
            User user = userRepository.findUserById(serviceProvider.getUser().getId()).orElseThrow();
            user.setDeleted(isDeleted);

            //Soft delete service that is provided by this service provider
            serviceRepository.findAllServicesByProviderId(serviceProvider.getId()).forEach(service ->
                    service.setDeleted(isDeleted)
            );

            //Soft delete service provider
            serviceProvider.setDeleted(isDeleted);
            serviceProvider.setUpdatedAt(LocalDateTime.now());
            serviceProviderRepository.save(serviceProvider);

            ServiceProviderDTO serviceProviderDTO = serviceProviderMapper.toDTO(serviceProvider);
            return new GeneralResponse<>(HttpStatus.OK.value(), "Chuyển đổi trạng thái thành công", serviceProviderDTO);
        }catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of("Chuyển đổi trạng thái thất bại", ex);
        }
    }

    private Specification<ServiceProvider> buildSearchSpecification(String keyword, Boolean isDeleted) {
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
    @Override
    public GeneralResponse<PagingDTO<List<PublicServiceProviderDTO>>> getAllHotel(int page, int size, String keyword, Integer star) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<ServiceProvider> spec = buildSearchSpecification(keyword, "Hotel", star);

            Page<ServiceProvider> serviceProviderPage = serviceProviderRepository.findAll(spec, pageable);

            //Find min room price of each hotel
            Map<Long, Double> minPriceMap = serviceProviderRepository.findMinRoomPricesByHotelIds(serviceProviderPage.getContent().stream().map(ServiceProvider::getId).toList())
                    .stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],  // tourId
                            row -> (Double) row[1] // priceFrom
                    ));;

            List<PublicServiceProviderDTO> serviceProviderDTOS = serviceProviderPage.getContent().stream()
                    .map(serviceProvider -> new PublicServiceProviderDTO(
                            serviceProvider.getId(),
                            serviceProvider.getImageUrl(),
                            serviceProvider.getName(),
                            serviceProvider.getAbbreviation(),
                            serviceProvider.getWebsite(),
                            serviceProvider.getEmail(),
                            serviceProvider.getStar(),
                            serviceProvider.getPhone(),
                            serviceProvider.getAddress(),
                            locationMapper.toPublicLocationDTO(serviceProvider.getLocation()),
                            geoPositionMapper.toDTO(serviceProvider.getGeoPosition()),
                            minPriceMap.getOrDefault(serviceProvider.getId(), 0.0)
                    ))
                    .collect(Collectors.toList());

            return buildPagedResponse(serviceProviderPage, serviceProviderDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Tải các Khách sạn thất bại", ex);
        }
    }



        @Override
    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> getAllRestaurant(int page, int size, String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<ServiceProvider> spec = buildSearchSpecification(keyword, "Restaurant", 1);

            Page<ServiceProvider> serviceProviderPage = serviceProviderRepository.findAll(spec, pageable);
            List<ServiceProviderDTO> serviceProviderDTOS = serviceProviderPage.getContent().stream()
                    .map(serviceProviderMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(serviceProviderPage, serviceProviderDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("Tải nhà hàng thất bại", ex);
        }
    }

    private <T>GeneralResponse<PagingDTO<List<T>>> buildPagedResponse(Page<ServiceProvider> serviceProviderPage, List<T> serviceProviders) {
        PagingDTO<List<T>> pagingDTO = PagingDTO.<List<T>>builder()
                .page(serviceProviderPage.getNumber())
                .size(serviceProviderPage.getSize())
                .total(serviceProviderPage.getTotalElements())
                .items(serviceProviders)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "Thành công", pagingDTO);
    }


    private Specification<ServiceProvider> buildSearchSpecification(String keyword, String categoryName, Integer star) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Ensure only the specified category is selected
            Join<ServiceProvider, ServiceCategory> categoryJoin = root.join("serviceCategories");
            predicates.add(cb.equal(categoryJoin.get("categoryName"), categoryName));

            // Always filter out deleted records
            predicates.add(cb.equal(root.get("deleted"), false));

            // Normalize text for search (Ignore case & accents)
            if (keyword != null && !keyword.trim().isEmpty()) {
                // Normalize the keyword before passing it into the query
                String normalizedKeyword = removeAccents(keyword.toLowerCase());

                // Use unaccent in DB for Service Provider name
                Expression<String> normalizedServiceName = cb.function("unaccent", String.class, cb.lower(root.get("name")));

                // Use unaccent in DB for Location name
                Join<ServiceProvider, Location> locationJoin = root.join("location", JoinType.LEFT);
                Expression<String> normalizedLocationName = cb.function("unaccent", String.class, cb.lower(locationJoin.get("name")));

                // Compare using LIKE
                Predicate serviceNamePredicate = cb.like(normalizedServiceName, "%" + normalizedKeyword + "%");
                Predicate locationNamePredicate = cb.like(normalizedLocationName, "%" + normalizedKeyword + "%");

                // Match either ServiceProvider name or Location name
                predicates.add(cb.or(serviceNamePredicate, locationNamePredicate));
            }

            // Filter by star
            if (star != null && star > 0) {
                predicates.add(cb.equal(root.get("star"), star));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);//Chuyển chữ có dấu thành ký tự gốc + dấu (ví dụ: Đà → Da + dấu huyền).
        Pattern pattern = Pattern.compile("\\p{M}"); //  Xóa tất cả các dấu khỏi ký tự.
        return pattern.matcher(normalized).replaceAll("");
    }

    public User createAccountServiceProvider(String fullName, String email, String phone, String address) {
        try {

            // Ensure "CUSTOMER" role exists, otherwise create it
            Role userRole = roleRepository.findByRoleName("SERVICE_PROVIDER")
                    .orElseGet(() -> {
                        Role newRole = Role.builder()
                                .roleName("SERVICE_PROVIDER")
                                .deleted(false)
                                .build();
                        return roleRepository.save(newRole);
                    });

            String randomPassword = passwordGenerate.generatePassword();

            // Create new user
            User user = User.builder()
                    .username(email.trim().toLowerCase())
                    .fullName(fullName)
                    .email(email.trim().toLowerCase())
                    .password(passwordEncoder.encode(randomPassword))
                    .phone(phone)
                    .address(address)
                    .deleted(false)
                    .emailConfirmed(true)
                    .build();

            User savedUser = userService.saveUser(user);

            // Assign role to user
            UserRole newUserRole = UserRole.builder()
                    .user(savedUser)
                    .role(userRole)
                    .deleted(false)
                    .build();

            userRoleRepository.save(newUserRole);

            // Send email account
            emailConfirmationService.sendAccountServiceProvider(user, randomPassword);
            return savedUser;
        } catch (BusinessException be){
            throw be;
        } catch (Exception e) {
            throw BusinessException.of("Tạo account nhà cung cấp thành công");
        }
    }

}

