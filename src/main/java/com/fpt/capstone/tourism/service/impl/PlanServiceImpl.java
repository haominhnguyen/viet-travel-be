package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.constants.Constants;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.LocationWithoutGeoPositionDTO;
import com.fpt.capstone.tourism.dto.common.PlanDTO;
import com.fpt.capstone.tourism.dto.common.ServiceProviderSimpleDTO;
import com.fpt.capstone.tourism.dto.request.ActivityGenerateDTO;
import com.fpt.capstone.tourism.dto.request.GeneratePlanRequestDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PlanSaleResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.helper.IHelper.PlanHelper;
import com.fpt.capstone.tourism.mapper.BookingMapper;
import com.fpt.capstone.tourism.mapper.LocationMapper;
import com.fpt.capstone.tourism.mapper.PlanMapper;
import com.fpt.capstone.tourism.model.*;
import com.fpt.capstone.tourism.model.enums.PlanStatus;
import com.fpt.capstone.tourism.repository.LocationRepository;
import com.fpt.capstone.tourism.repository.PlanRepository;
import com.fpt.capstone.tourism.repository.ServiceProviderRepository;
import com.fpt.capstone.tourism.repository.ServiceRepository;
import com.fpt.capstone.tourism.service.GeminiApiService;
import com.fpt.capstone.tourism.service.GroqService;
import com.fpt.capstone.tourism.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.fpt.capstone.tourism.constants.Constants.Message.GET_PROVIDER_BY_LOCATION_FAIL;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final LocationRepository locationRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final PlanRepository planRepository;
    private final ServiceRepository serviceRepository;

    private final LocationMapper locationMapper;
    private final PlanMapper planMapper;
    private final BookingMapper bookingMapper;

    private final PlanHelper planHelper;

    private final GeminiApiService geminiApiService;
    private final GroqService groqService;



    @Override
    public GeneralResponse<?> getLocations() {
        try {
            List<Location> locations = locationRepository.findRandomLocation(6);
            List<LocationWithoutGeoPositionDTO> dto = locations.stream().map(locationMapper::toLocationWithoutGeoPositionDTO).toList();
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Không thể lấy địa điểm cho kế hoạch", ex);
        }
    }

    @Override
    public GeneralResponse<?> getLocations(String name) {
        try {
            String normalizedName = removeAccents(name).toLowerCase();
            List<Location> locations = locationRepository.findAll(planHelper.searchLocationByName(normalizedName));
            return GeneralResponse.of(locations.stream().map(locationMapper::toPublicLocationDTO).toList());
        } catch (Exception ex) {
            throw BusinessException.of("Không thể lấy tất cả địa điểm cho kế hoạch", ex);
        }
    }

    @Override
    public String buildServiceProviderContext(Long locationId) {
        try {
            List<ServiceProvider> hotels = serviceProviderRepository.findByLocationIdAndServiceCategoryIdAndDeletedFalse(locationId, 1L);
            List<ServiceProvider> restaurants = serviceProviderRepository.findByLocationIdAndServiceCategoryIdAndDeletedFalse(locationId, 2L);
            List<com.fpt.capstone.tourism.model.Service> activities = serviceRepository.findByServiceCategoryIdAndLocationId(4L, locationId);

            StringBuilder promptBuilder = new StringBuilder("Hãy đề xuất các nhà cung cấp dịch vụ (khách sạn và nhà hàng) phù hợp cho khách hàng dựa trên dữ liệu sau:\n\n");

            // Append hotel data
            promptBuilder.append("🏨 Khách sạn:\n");
            for (ServiceProvider provider : hotels) {
                promptBuilder.append("- ").append(provider.getName())
                        .append(", ID: ").append(provider.getId())
                        .append(", Địa chỉ: ").append(provider.getAddress())
                        .append(", Link Ảnh: ").append(provider.getImageUrl())
                        .append("\n");
            }

            // Append restaurant data
            promptBuilder.append("\n🍽️ Nhà hàng:\n");
            for (ServiceProvider provider : restaurants) {
                promptBuilder.append("- ").append(provider.getName())
                        .append(", ID: ").append(provider.getId())
                        .append(", Địa chỉ: ").append(provider.getAddress())
                        .append(", Link Ảnh: ").append(provider.getImageUrl())
                        .append("\n");
            }

            // Append restaurant data
            promptBuilder.append("\n🍽️ Hoạt động:\n");
            for (com.fpt.capstone.tourism.model.Service service : activities) {
                promptBuilder.append("- ").append(service.getName().replace("Vé", ""))
                        .append(", Giá vé: ").append(service.getSellingPrice())
                        .append(", Link Ảnh: ").append(service.getImageUrl())
                        .append("\n");
            }



            Location location = locationRepository.findById(locationId).orElseThrow();
            promptBuilder.append("🏨 Link Thumbnail Image: ").append(location.getImage()).append("\n");



            return promptBuilder.toString();
        } catch (Exception ex) {
            throw BusinessException.of("Tạo context không thành công", ex);
        }
    }

    @Override
    public String buildCustomerPreferContext(GeneratePlanRequestDTO dto) {
        try {
            StringBuilder contextBuilder = new StringBuilder();

            contextBuilder.append("Khách hàng đã yêu cầu một kế hoạch du lịch với các thông tin sau:\n\n");

            contextBuilder.append("- Địa điểm ID: ").append(dto.getLocationId()).append("\n");

            if (dto.getStartDate() != null && dto.getEndDate() != null) {
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                contextBuilder.append("- Thời gian: Từ ")
                        .append(dateFormat.format(dto.getStartDate()))
                        .append(" đến ")
                        .append(dateFormat.format(dto.getEndDate()))
                        .append("\n");
            } else {
                contextBuilder.append("- Thời gian: Không xác định rõ ràng\n");
            }

            if (dto.getPlanType() != null && !dto.getPlanType().isEmpty()) {
                contextBuilder.append("- Loại kế hoạch mong muốn: ").append(dto.getPlanType()).append("\n");
            } else {
                contextBuilder.append("- Loại kế hoạch mong muốn: Không được cung cấp\n");
            }

            if (dto.getPreferences() != null && !dto.getPreferences().isEmpty()) {
                contextBuilder.append("- Sở thích cá nhân: ").append(dto.getPreferences()).append("\n");
            } else {
                contextBuilder.append("- Sở thích cá nhân: Không được cung cấp\n");
            }

            if (dto.isTravelingWithChildren()) {
                contextBuilder.append("- Có sự tham gia của trẻ em ").append(dto.getPreferences()).append("\n");
            } else {
                contextBuilder.append("- Không có sự tham gia của trẻ em\n");
            }


            contextBuilder.append("\nHãy sử dụng các thông tin trên để tạo ra một kế hoạch du lịch phù hợp nhất với nhu cầu khách hàng.");

            return contextBuilder.toString();
        } catch (Exception ex) {
            throw BusinessException.of("Tạo context không thành công", ex);
        }
    }

    @Override
    public String buildActivityPreferences(ActivityGenerateDTO dto) {
        try {
            StringBuilder contextBuilder = new StringBuilder();

            contextBuilder.append("Khách hàng đã yêu cầu tìm các hoạt động khi đi du lịch với các thông tin sau:\n\n");

            contextBuilder.append("- Địa điểm: ").append(dto.getLocationName()).append("\n");
            contextBuilder.append("- ID của hoạt động bắt đầu từ: ").append(dto.getStartIndex()).append("\n");

            if (dto.getPreferences() != null && !dto.getPreferences().isEmpty()) {
                contextBuilder.append("- Sở thích cá nhân: ").append(dto.getPreferences()).append("\n");
            } else {
                contextBuilder.append("- Sở thích cá nhân: Không được cung cấp\n");
            }
            contextBuilder.append("\nHãy sử dụng các thông tin trên để tìm ra 6 hoạt động phù hợp nhất với nhu cầu khách hàng.");

            return contextBuilder.toString();
        } catch (Exception ex) {
            throw BusinessException.of("Tạo context không thành công", ex);
        }
    }

    @Override
    public GeneralResponse<?> generatePlan(GeneratePlanRequestDTO dto) {
        try {
            String prompt = Constants.AI.PROMPT_START
                    + buildCustomerPreferContext(dto)
                    + buildServiceProviderContext(dto.getLocationId())
                    + Constants.AI.PROMPT_END;

            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> message1 = new HashMap<>();
            message1.put("role", "user");
            message1.put("content", prompt);
            messages.add(message1);

            String model = "deepseek-r1-distill-llama-70b";

            String response = groqService.callGroqAPI(messages, model);


            //response = response.replace("json", "").replace("```", "");


            Plan plan = Plan.builder()
                    .user(User.builder().id(dto.getUserId()).build())
                    .content(response)
                    .deleted(true)
                    .planStatus(PlanStatus.CREATED)
                    .build();

            Plan savedPlan = planRepository.save(plan);


            return GeneralResponse.of(response);
        } catch (Exception ex) {
            throw BusinessException.of("Tạo plan không thành công", ex);
        }
    }

    @Override
    public GeneralResponse<?> getPlanById(Long planId) {
        try {
            Plan plan = planRepository.findById(planId).orElseThrow();
            PlanDTO dto = planMapper.toPlanDto(plan);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> deletePlanById(Long planId) {
        try {
            Plan plan = planRepository.findById(planId).orElseThrow();
            planRepository.deleteById(planId);
            PlanDTO dto = planMapper.toPlanDto(plan);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> getPlansByUserId(Long userId) {
        try {
            List<Plan> plans = planRepository.getByUserId(userId);
            List<PlanDTO> dto = plans.stream().map(planMapper::toPlanDto).toList();
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<PlanDTO>>> getPlans(int page, int size, String sortField, String sortDirection, Long userId) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Plan> spec = planHelper.buildSearchSpecification(userId);

            Page<Plan> tourBookingPage = planRepository.findAll(spec, pageable);

            return planHelper.buildPagedResponse(tourBookingPage);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<PagingDTO<List<PlanSaleResponseDTO>>> getPlans(int page, int size, String sortField, String sortDirection, PlanStatus planStatus, String keyword) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

            // Build search specification
            Specification<Plan> spec = planHelper.buildSearchSpecification(planStatus, keyword);

            Page<Plan> tourBookingPage = planRepository.findAll(spec, pageable);

            return planHelper.buildPagedPlanSaleResponse(tourBookingPage);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> updatePlan(String planJson, Long planId) {
        try {
            Plan plan = planRepository.findById(planId).orElseThrow();
            plan.setContent(planJson);

            planRepository.save(plan);
            PlanDTO dto = planMapper.toPlanDto(plan);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> updateStatus(Long planId) {
        try {
            Plan plan = planRepository.findById(planId).orElseThrow();
            plan.setDeleted(false);
            planRepository.save(plan);
            PlanDTO dto = planMapper.toPlanDto(plan);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> updateStatus(Long planId, PlanStatus planStatus) {
        try {
            Plan plan = planRepository.findById(planId).orElseThrow();
            plan.setPlanStatus(planStatus);
            planRepository.save(plan);
            PlanDTO dto = planMapper.toPlanDto(plan);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại", ex);
        }
    }

    @Override
    public GeneralResponse<?> requestTourCreate(Long planId) {
        try {
            Plan plan = planRepository.findById(planId).orElseThrow();
            plan.setPlanStatus(PlanStatus.PENDING);
            Plan saved = planRepository.save(plan);
            PlanDTO dto = planMapper.toPlanDto(saved);
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of("Thay đổi thành công", ex);
        }
    }

    @Override
    public GeneralResponse<?> getServiceProviders(Long locationId, String categoryName, List<Long> ids) {
        try {
            List<ServiceProvider> providers =serviceProviderRepository.getServiceByLocationIdAndServiceCategoryAndNotIncludeIDs(locationId, categoryName, ids);
            List<ServiceProviderSimpleDTO> dto = providers.stream().map(bookingMapper::toServiceProviderSimpleDTO).toList();
            return GeneralResponse.of(dto);
        } catch (Exception ex) {
            throw BusinessException.of(GET_PROVIDER_BY_LOCATION_FAIL , ex);
        }
    }

    @Override
    public GeneralResponse<?> getActivities(ActivityGenerateDTO dto) {
        try {
            String prompt = Constants.AI.PROMPT_START
                    + buildActivityPreferences(dto)
                    + Constants.AI.ACTIVITIES_PROMPT_END;
            List<Map<String, String>> messages = new ArrayList<>();

            Map<String, String> message1 = new HashMap<>();
            message1.put("role", "user");
            message1.put("content", prompt);
            messages.add(message1);
            String model = "deepseek-r1-distill-llama-70b";
            String response = groqService.callGroqAPI(messages, model);


            //response = response.replace("json", "").replace("```", "");
            return GeneralResponse.of(response);
        } catch (Exception ex) {
            throw BusinessException.of("Lấy dữ liệu thất bại" , ex);
        }
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);//Chuyển chữ có dấu thành ký tự gốc + dấu (ví dụ: Đà → Da + dấu huyền).
        Pattern pattern = Pattern.compile("\\p{M}"); //  Xóa tất cả các dấu khỏi ký tự.
        return pattern.matcher(normalized).replaceAll("");
    }
}
