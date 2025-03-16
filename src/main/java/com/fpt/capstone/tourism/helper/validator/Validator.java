package com.fpt.capstone.tourism.helper.validator;
import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.dto.request.*;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.model.enums.TourStatus;
import com.fpt.capstone.tourism.model.enums.TourType;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.LocationDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import static com.fpt.capstone.tourism.constants.Constants.Message.*;
import static com.fpt.capstone.tourism.constants.Constants.Regex.*;
import static com.fpt.capstone.tourism.constants.Constants.UserExceptionInformation.*;

public class Validator {

    // General Validation
    public static void validateRegex(String value, String regex, String errorMessage) {
        if (!value.matches(regex)) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    public static void isNullOrEmpty(String value, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }
    public static boolean isPasswordValid(String value){
        return value.matches(REGEX_PASSWORD);
    }

    // Authentication Validation
    public static void validateLogin(String username, String password) {
        isNullOrEmpty(username, EMPTY_USERNAME);
        validateRegex(username, REGEX_USERNAME, USERNAME_INVALID);
        isNullOrEmpty(password, EMPTY_PASSWORD);
        validateRegex(password, REGEX_PASSWORD, PASSWORD_INVALID);
    }

    public static void validateRegister(String username, String password, String rePassword, String fullName,
                                        String phone, String address, String email) {
        isNullOrEmpty(username, EMPTY_USERNAME);
        validateRegex(username, REGEX_USERNAME, USERNAME_INVALID);

        isNullOrEmpty(password, EMPTY_PASSWORD);
        validateRegex(password, REGEX_PASSWORD, PASSWORD_INVALID);

        isNullOrEmpty(rePassword, EMPTY_REPASSWORD);
        if (!password.equals(rePassword)) {
            throw BusinessException.of(PASSWORDS_DO_NOT_MATCH_MESSAGE);
        }

        isNullOrEmpty(fullName, EMPTY_FULL_NAME);
        validateRegex(fullName, REGEX_FULLNAME, FULL_NAME_INVALID);

        isNullOrEmpty(phone, EMPTY_PHONE_NUMBER);
        validateRegex(phone, REGEX_PHONE, PHONE_INVALID);

        isNullOrEmpty(address, EMPTY_ADDRESS);

        isNullOrEmpty(email, EMPTY_EMAIL);
        validateRegex(email, REGEX_EMAIL, EMAIL_INVALID);
    }

    // Common Feature Validation
    public static void validateProfile(String fullName, String email, String phone, String address) {
        isNullOrEmpty(fullName, EMPTY_FULL_NAME);
        validateRegex(fullName, REGEX_FULLNAME, FULL_NAME_INVALID);

        isNullOrEmpty(email, EMPTY_EMAIL);
        validateRegex(email, REGEX_EMAIL, EMAIL_INVALID);

        isNullOrEmpty(phone, EMPTY_PHONE_NUMBER);
        validateRegex(phone, REGEX_PHONE, PHONE_INVALID);

        isNullOrEmpty(address, USER_INFORMATION_NULL_OR_EMPTY);
    }

    // User Management Validation
    public static void validateUserCreation(String fullName, String username, String password, String rePassword,
                                            String email, String gender, String phone, String address,
                                            String avatarImage, List<String> roleNames) {
        validateUserFields(fullName, username, password, rePassword, email, gender, phone, address, avatarImage, roleNames);
    }

    public static void validateUserUpdate(String fullName, String username, String password, String rePassword,
                                          String email, String gender, String phone, String address,
                                          String avatarImage, List<String> roleNames) {


        isNullOrEmpty(fullName, EMPTY_FULL_NAME);
        validateRegex(fullName, REGEX_FULLNAME, FULL_NAME_INVALID);

        isNullOrEmpty(username, EMPTY_USERNAME);
        validateRegex(username, REGEX_USERNAME, USERNAME_INVALID);

        //Only validate password if it's not empty
        if (password != null && !password.isEmpty()) {
            validateRegex(password, REGEX_PASSWORD, PASSWORD_INVALID);
            isNullOrEmpty(rePassword, EMPTY_REPASSWORD);
            if (!password.equals(rePassword)) {
                throw BusinessException.of(PASSWORDS_DO_NOT_MATCH_MESSAGE);
            }
        }
        isNullOrEmpty(email, EMPTY_EMAIL);
        validateRegex(email, REGEX_EMAIL, EMAIL_INVALID);

        isNullOrEmpty(phone, EMPTY_PHONE_NUMBER);
        validateRegex(phone, REGEX_PHONE, PHONE_INVALID);

        isNullOrEmpty(address, EMPTY_ADDRESS);

        if (!"male".equalsIgnoreCase(gender) && !"female".equalsIgnoreCase(gender)) {
            throw BusinessException.of(GENDER_INVALID);
        }

        if (roleNames == null || roleNames.isEmpty()) {
            throw BusinessException.of(ROLES_NAME_INVALID);
        }
    }

    private static void validateUserFields(String fullName, String username, String password, String rePassword,
                                           String email, String gender, String phone, String address,
                                           String avatarImage, List<String> roleNames) {
        isNullOrEmpty(fullName, EMPTY_FULL_NAME);
        validateRegex(fullName, REGEX_FULLNAME, FULL_NAME_INVALID);

        isNullOrEmpty(username, EMPTY_USERNAME);
        validateRegex(username, REGEX_USERNAME, USERNAME_INVALID);

        isNullOrEmpty(password, EMPTY_PASSWORD);
        validateRegex(password, REGEX_PASSWORD, PASSWORD_INVALID);

        isNullOrEmpty(rePassword, EMPTY_REPASSWORD);
        if (!password.equals(rePassword)) {
            throw BusinessException.of(PASSWORDS_DO_NOT_MATCH_MESSAGE);
        }
        isNullOrEmpty(email, EMPTY_EMAIL);
        validateRegex(email, REGEX_EMAIL, EMAIL_INVALID);

        isNullOrEmpty(phone, EMPTY_PHONE_NUMBER);
        validateRegex(phone, REGEX_PHONE, PHONE_INVALID);

        isNullOrEmpty(address, EMPTY_ADDRESS);
        //isNullOrEmpty(avatarImage, USER_INFORMATION_NULL_OR_EMPTY);

        if (!"male".equalsIgnoreCase(gender) && !"female".equalsIgnoreCase(gender)) {
            throw BusinessException.of(GENDER_INVALID);
        }

        if (roleNames == null || roleNames.isEmpty()) {
            throw BusinessException.of(ROLES_NAME_INVALID);
        }
    }

    public static void validateCreateTourGuideFields(String fullName, String username, String password,
                                           String email, String gender, String phone, String address) {
        isNullOrEmpty(fullName, EMPTY_FULL_NAME);
        validateRegex(fullName, REGEX_FULLNAME, FULL_NAME_INVALID);

        isNullOrEmpty(username, EMPTY_USERNAME);
        validateRegex(username, REGEX_USERNAME, USERNAME_INVALID);

        isNullOrEmpty(password, EMPTY_PASSWORD);
        validateRegex(password, REGEX_PASSWORD, PASSWORD_INVALID);

        isNullOrEmpty(email, EMPTY_EMAIL);
        validateRegex(email, REGEX_EMAIL, EMAIL_INVALID);

        isNullOrEmpty(phone, EMPTY_PHONE_NUMBER);
        validateRegex(phone, REGEX_PHONE, PHONE_INVALID);

        isNullOrEmpty(address, EMPTY_ADDRESS);
        //isNullOrEmpty(avatarImage, USER_INFORMATION_NULL_OR_EMPTY);

        if (!"male".equalsIgnoreCase(gender) && !"female".equalsIgnoreCase(gender)) {
            throw BusinessException.of(GENDER_INVALID);
        }
    }

    public static void validateUpdateTourGuideFields(String fullName, String username, String password,
                                               String email, String gender, String phone, String address) {
        isNullOrEmpty(fullName, EMPTY_FULL_NAME);
        validateRegex(fullName, REGEX_FULLNAME, FULL_NAME_INVALID);

        isNullOrEmpty(username, EMPTY_USERNAME);
        validateRegex(username, REGEX_USERNAME, USERNAME_INVALID);

        //Only validate password if it's not empty (for updates)
        if (password != null && !password.isEmpty()) {
            validateRegex(password, REGEX_PASSWORD, PASSWORD_INVALID);
        }

        isNullOrEmpty(email, EMPTY_EMAIL);
        validateRegex(email, REGEX_EMAIL, EMAIL_INVALID);

        isNullOrEmpty(phone, EMPTY_PHONE_NUMBER);
        validateRegex(phone, REGEX_PHONE, PHONE_INVALID);

        isNullOrEmpty(address, EMPTY_ADDRESS);

        if (!"male".equalsIgnoreCase(gender) && !"female".equalsIgnoreCase(gender)) {
            throw BusinessException.of(GENDER_INVALID);
        }
    }

    // Service Contact Validation
    public static void validateServiceContact(String fullName, String phoneNumber, String email, String position) {
        isNullOrEmpty(fullName, EMPTY_FULL_NAME);
        isNullOrEmpty(phoneNumber, EMPTY_PHONE_NUMBER);
        validateRegex(phoneNumber, REGEX_PHONE, PHONE_INVALID);
        isNullOrEmpty(email, EMPTY_EMAIL);
        validateRegex(email, REGEX_EMAIL, EMAIL_INVALID);
        isNullOrEmpty(position, EMPTY_POSITION);
    }

    //Blog Validation
    public static void validateBlog(String title, String description, String content) {
        isNullOrEmpty(title, EMPTY_BLOG_TITLE);
        isNullOrEmpty(description, EMPTY_BLOG_DESCRIPTION);
        isNullOrEmpty(content, EMPTY_BLOG_CONTENT);

        // Length validation
        validateLength(title, 5, 100, INVALID_BLOG_TITLE_LENGTH);
        validateLength(description, 10, 300, INVALID_BLOG_DESCRIPTION_LENGTH);
        validateLength(content, 50, 5000, INVALID_BLOG_CONTENT_LENGTH);
    }

    private static void validateLength(String value, int min, int max, String errorMessage) {
        if (value.length() < min || value.length() > max) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }


    public static void validateLocation(LocationRequestDTO locationRequestDTO){
        isNullOrEmpty(locationRequestDTO.getName(), EMPTY_LOCATION_NAME);
        isNullOrEmpty(locationRequestDTO.getDescription(), EMPTY_LOCATION_DESCRIPTION);
        isNullOrEmpty(locationRequestDTO.getImage(), EMPTY_LOCATION_IMAGE);
        isNullOrEmpty(locationRequestDTO.getGeoPosition().toString(), EMPTY_LOCATION_GEO_POSITION);
    }

    public static void validateServiceProvider(ServiceProviderDTO serviceProviderDTO) {
        isNullOrEmpty(serviceProviderDTO.getImageUrl(), EMPTY_IMAGE_URL);

        isNullOrEmpty(serviceProviderDTO.getAbbreviation(), EMPTY_ABBREVIATION);

        isNullOrEmpty(serviceProviderDTO.getWebsite(), EMPTY_WEBSITE);

        isNullOrEmpty(serviceProviderDTO.getEmail(), EMPTY_EMAIL);
        validateRegex(serviceProviderDTO.getEmail(), REGEX_EMAIL, INVALID_EMAIL);

        isNullOrEmpty(serviceProviderDTO.getPhone(), EMPTY_PHONE_NUMBER);
        validateRegex(serviceProviderDTO.getPhone(), REGEX_PHONE, INVALID_PHONE_NUMBER);

        isNullOrEmpty(serviceProviderDTO.getAddress(), EMPTY_ADDRESS);
//        private LocationDTO location;
//
//        private GeoPositionDTO geoPosition;
//
//        private Set<ServiceCategoryDTO> serviceCategorys;

    }

    public static void validateActivity(ActivityDTO activityDTO) {
        isNullOrEmpty(activityDTO.getImageUrl(), EMPTY_IMAGE_URL);

        isNullOrEmpty(activityDTO.getContent(), EMPTY_BLOG_CONTENT);

        isNullOrEmpty(activityDTO.getTitle(), EMPTY_BLOG_TITLE);

        isNullOrEmpty(String.valueOf(activityDTO.getPricePerPerson()), EMPTY_PRICE);
        if(activityDTO.getPricePerPerson() < 0){
            throw BusinessException.of("Price can not be lower than 0");
        }
        isNullOrEmpty(activityDTO.getGeoPosition().toString(), EMPTY_LOCATION_GEO_POSITION);
        isNullOrEmpty(activityDTO.getLocation().toString(), EMPTY_LOCATION);
        isNullOrEmpty(activityDTO.getActivityCategory().toString(), EMPTY_ACTIVITY_CATEGORY);
    }

    public static void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, INVALID_DATE_RANGE);
        }
    }
    public static void validatePrices(Double nettPrice, Double sellingPrice){
        if (nettPrice == null || sellingPrice == null) {
           throw BusinessException.of(HttpStatus.BAD_REQUEST, INVALID_PRICE);
        }
        if (nettPrice < 0 || sellingPrice < 0) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, INVALID_PRICE);
        }

        if (nettPrice > sellingPrice) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, INVALID_PRICE_RANGE);
        }
    }


    public static void validateLog(TourOperationLogRequestDTO logRequestDTO) {
        isNullOrEmpty(logRequestDTO.getAction(), "Empty action");
        isNullOrEmpty(logRequestDTO.getContent(), "Empty content");
    }

    public static void validateRoomDetails(RoomDetailsDTO roomDetails, String categoryName) {
        if (HOTEL.equalsIgnoreCase(categoryName)) {
            if (roomDetails == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, ROOM_DETAILS_REQUIRED);
            }
            if (roomDetails.getCapacity() == null || roomDetails.getCapacity() <= 0) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, INVALID_ROOM_CAPACITY);
            }
            if (roomDetails.getAvailableQuantity() == null || roomDetails.getAvailableQuantity() < 0) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, NEGATIVE_AVAILABLE_QUANTITY);
            }
        } else if (roomDetails != null) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, UNEXPECTED_ROOM_DETAILS);
        }
    }

    public static void validateMealDetails(MealDetailsDTO mealDetails, String categoryName) {
        if (RESTAURANT.equalsIgnoreCase(categoryName)) {
            if (mealDetails == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, MEAL_DETAILS_REQUIRED);
            }
            if (mealDetails.getType() == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, MEAL_TYPE_REQUIRED);
            }
        } else if (mealDetails != null) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, UNEXPECTED_MEAL_DETAILS);
        }
    }

    public static void validateTransportDetails(TransportDetailsDTO transportDetails, String categoryName) {
        if (TRANSPORT.equalsIgnoreCase(categoryName)) {
            if (transportDetails == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TRANSPORT_DETAILS_REQUIRED);
            }
            if (transportDetails.getSeatCapacity() == null || transportDetails.getSeatCapacity() <= 0) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, INVALID_SEAT_CAPACITY);
            }
        } else if (transportDetails != null) {
            throw BusinessException.of(HttpStatus.BAD_REQUEST, UNEXPECTED_TRANSPORT_DETAILS);
        }
    }

    public static void validateServiceDetails(ServiceRequestDTO requestDTO, String categoryName) {
        validateRoomDetails(requestDTO.getRoomDetails(), categoryName);
        validateMealDetails(requestDTO.getMealDetails(), categoryName);
        validateTransportDetails(requestDTO.getTransportDetails(), categoryName);
    }

    public static void validateTourRequest(TourRequestDTO tourRequestDTO) {
            if (tourRequestDTO == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_REQUEST_NULL);
            }

            if (!StringUtils.hasText(tourRequestDTO.getName())) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_NAME_EMPTY);
            }

            if (tourRequestDTO.getNumberDays() == null || tourRequestDTO.getNumberDays() <= 0) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, NUMBER_DAYS_INVALID);
            }

            if (tourRequestDTO.getNumberNights() == null || tourRequestDTO.getNumberNights() < 0) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, NUMBER_NIGHTS_INVALID);
            }

            if (tourRequestDTO.getLocationIds() == null || tourRequestDTO.getLocationIds().isEmpty()) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_MUST_HAVE_LOCATION);
            }

            if (tourRequestDTO.getDepartLocationId() == null) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, DEPART_LOCATION_NOT_FOUND);
            }

            try {
                if (tourRequestDTO.getTourType() != null) {
                    TourType.valueOf(tourRequestDTO.getTourType());
                } else {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_TYPE_REQUIRED);
                }
            } catch (IllegalArgumentException e) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_TYPE_INVALID);
            }

            try {
                if (tourRequestDTO.getTourStatus() != null) {
                    TourStatus.valueOf(tourRequestDTO.getTourStatus());
                } else {
                    throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_STATUS_REQUIRED);
                }
            } catch (IllegalArgumentException e) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, TOUR_STATUS_INVALID);
            }

            if (tourRequestDTO.getMarkUpPercent() == null || tourRequestDTO.getMarkUpPercent() < 0) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, MARKUP_PERCENT_INVALID);
            }
    }
}
