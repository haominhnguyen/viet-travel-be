package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.RegisterRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.UserInfoResponseDTO;
import com.fpt.capstone.tourism.enums.RoleName;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.PasswordGenerateImpl;
import com.fpt.capstone.tourism.helper.validator.Validator;
import com.fpt.capstone.tourism.mapper.ServiceCategoryMapper;
import com.fpt.capstone.tourism.mapper.ServiceProviderMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.repository.LocationRepository;
import com.fpt.capstone.tourism.repository.RoleRepository;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import com.fpt.capstone.tourism.repository.UserRoleRepository;
import com.fpt.capstone.tourism.service.EmailConfirmationService;
import com.fpt.capstone.tourism.service.ServiceProviderService;
import com.fpt.capstone.tourism.service.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
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
            //Store data into database
            ServiceProvider serviceProvider = serviceProviderMapper.toEntity(serviceProviderDTO);
            serviceProvider.setId(null);
            serviceProvider.setDeleted(false);
            serviceProvider.setCreatedAt(LocalDateTime.now());


            User serviceUser = createAccountServiceProvider(serviceProvider.getName(),
                    serviceProvider.getEmail(),
                    serviceProvider.getPhone(),
                    serviceProvider.getAddress());

            serviceProvider.setUser(serviceUser);
            serviceProviderRepository.save(serviceProvider);

            return new GeneralResponse<>(HttpStatus.OK.value(), CREATE_SERVICE_PROVIDER_SUCCESS, serviceProviderMapper.toDTO(serviceProvider));
        } catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(CREATE_SERVICE_PROVIDER_FAIL, ex);
        }

    }

    @Transactional
    @Override
    public GeneralResponse<ServiceProviderDTO> getServiceProviderById(Long id) {
        try{
            ServiceProvider serviceProvider = serviceProviderRepository.findById(id).orElseThrow();
            ServiceProviderDTO serviceProviderDTO = serviceProviderMapper.toDTO(serviceProvider);
            return new GeneralResponse<>(HttpStatus.OK.value(), GENERAL_SUCCESS_MESSAGE, serviceProviderDTO);
        } catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(GENERAL_FAIL_MESSAGE, ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> getAllServiceProviders(int page, int size, String keyword, Boolean isDeleted) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<ServiceProvider> spec = buildSearchSpecification(keyword, isDeleted);

            Page<ServiceProvider> serviceProviderPage = serviceProviderRepository.findAll(spec, pageable);
            List<ServiceProviderDTO> serviceProviderDTOS = serviceProviderPage.getContent().stream()
                    .map(serviceProviderMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(serviceProviderPage, serviceProviderDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("not ok", ex);
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

            if(!serviceProviderDTO.getGeoPosition().getId().equals(serviceProvider.getGeoPosition().getId())){
                GeoPosition geoPosition = GeoPosition.builder()
                        .latitude(serviceProviderDTO.getGeoPosition().getLatitude())
                        .longitude(serviceProviderDTO.getGeoPosition().getLongitude()).build() ;
                serviceProvider.setGeoPosition(geoPosition);
            }

            if(!serviceProviderDTO.getLocation().getId().equals(serviceProvider.getLocation().getId())) {
                Location location = locationRepository.findById(serviceProviderDTO.getLocation().getId()).orElseThrow();
                serviceProvider.setLocation(location);
            }
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

            serviceProvider.setDeleted(isDeleted);
            serviceProvider.setUpdatedAt(LocalDateTime.now());
            serviceProviderRepository.save(serviceProvider);

            ServiceProviderDTO serviceProviderDTO = serviceProviderMapper.toDTO(serviceProvider);
            return new GeneralResponse<>(HttpStatus.OK.value(), UPDATE_SERVICE_PROVIDER_SUCCESS, serviceProviderDTO);
        }catch (BusinessException be){
            throw be;
        } catch (Exception ex){
            throw BusinessException.of(UPDATE_SERVICE_PROVIDER_FAIL, ex);
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
    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> getAllHotel(int page, int size, String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<ServiceProvider> spec = buildSearchSpecification(keyword, "Hotel");

            Page<ServiceProvider> serviceProviderPage = serviceProviderRepository.findAll(spec, pageable);
            List<ServiceProviderDTO> serviceProviderDTOS = serviceProviderPage.getContent().stream()
                    .map(serviceProviderMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(serviceProviderPage, serviceProviderDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("not ok", ex);
        }
    }



        @Override
    public GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> getAllRestaurant(int page, int size, String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Specification<ServiceProvider> spec = buildSearchSpecification(keyword, "Restaurant");

            Page<ServiceProvider> serviceProviderPage = serviceProviderRepository.findAll(spec, pageable);
            List<ServiceProviderDTO> serviceProviderDTOS = serviceProviderPage.getContent().stream()
                    .map(serviceProviderMapper::toDTO)
                    .collect(Collectors.toList());

            return buildPagedResponse(serviceProviderPage, serviceProviderDTOS);
        } catch (Exception ex) {
            throw BusinessException.of("not ok", ex);
        }
    }

    private GeneralResponse<PagingDTO<List<ServiceProviderDTO>>> buildPagedResponse(Page<ServiceProvider> serviceProviderPage, List<ServiceProviderDTO> serviceProviders) {
        PagingDTO<List<ServiceProviderDTO>> pagingDTO = PagingDTO.<List<ServiceProviderDTO>>builder()
                .page(serviceProviderPage.getNumber())
                .size(serviceProviderPage.getSize())
                .total(serviceProviderPage.getTotalElements())
                .items(serviceProviders)
                .build();

        return new GeneralResponse<>(HttpStatus.OK.value(), "ok", pagingDTO);
    }

    private Specification<ServiceProvider> buildSearchSpecification(String keyword, String categoryName) {
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

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{M}"); // Removes diacritics (accents)
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
            System.out.println(randomPassword);
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
            System.out.println("create new user finish");
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
            throw BusinessException.of("Create account service provider fail");
        }
    }
}

