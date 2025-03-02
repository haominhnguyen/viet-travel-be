package com.fpt.capstone.tourism.mapper.custom;

import com.fpt.capstone.tourism.dto.common.*;
import com.fpt.capstone.tourism.model.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ServiceCustomMapper {

    /**
     * Maps a Service entity to a ServiceFullDTO with all related entities
     */
    public ServiceFullDTO mapToServiceFullDTO(Service service) {
        if (service == null) {
            return null;
        }

        try {
            Set<ServiceDetail> details = service.getServiceDetails();
            List<TourDayService> tourDayServices = service.getTourDayServices();

            return ServiceFullDTO.builder()
                    .id(service.getId())
                    .name(service.getName())
                    .nettPrice(service.getNettPrice())
                    .sellingPrice(service.getSellingPrice())
                    .imageUrl(service.getImageUrl())
                    .startDate(service.getStartDate())
                    .endDate(service.getEndDate())
                    .deleted(service.getDeleted())
                    .createdAt(service.getCreatedAt())
                    .updatedAt(service.getUpdatedAt())
                    .serviceCategory(mapToServiceCategoryDTO(service.getServiceCategory()))
                    .serviceProvider(mapToServiceProviderDTO(service.getServiceProvider()))
                    .serviceDetails(details != null ? mapToServiceDetailDTO(details) : Collections.emptySet())
                    .tourDayServices(tourDayServices != null ? mapToTourDayServiceDTOList(tourDayServices) : Collections.emptyList())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error mapping service to DTO", e);
        }
    }

    /**
     * Maps a ServiceCategory entity to a ServiceCategoryDTO
     */
    private ServiceCategoryDTO mapToServiceCategoryDTO(ServiceCategory category) {
        if (category == null) {
            return null;
        }
        ServiceCategoryDTO dto = new ServiceCategoryDTO();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setDeleted(category.getDeleted());
        return dto;
    }

    /**
     * Maps a ServiceProvider entity to a ServiceProviderDTO
     */
    private ServiceProviderDTO mapToServiceProviderDTO(ServiceProvider provider) {
        if (provider == null) {
            return null;
        }

        ServiceProviderDTO dto = new ServiceProviderDTO();
        dto.setId(provider.getId());
        dto.setName(provider.getName());
        dto.setImageUrl(provider.getImageUrl());
        dto.setAbbreviation(provider.getAbbreviation());
        dto.setWebsite(provider.getWebsite());
        dto.setEmail(provider.getEmail());
        dto.setStar(provider.getStar());
        dto.setPhone(provider.getPhone());
        dto.setAddress(provider.getAddress());
        return dto;
    }

    /**
     * Maps a collection of ServiceDetail entities to a set of ServiceDetailDTOs
     */
    private Set<ServiceDetailDTO> mapToServiceDetailDTO(Set<ServiceDetail> details) {
        return details.stream()
                .filter(Objects::nonNull)
                .map(detail -> ServiceDetailDTO.builder()
                        .id(detail.getId())
                        .title(detail.getTitle())
                        .content(detail.getContent())
                        .build())
                .collect(Collectors.toSet());
    }

    /**
     * Maps a ServiceDetail entity to a ServiceDetailDTO
     */
    private ServiceDetailDTO mapToServiceDetailDTO(ServiceDetail detail) {
        if (detail == null) {
            return null;
        }
        return ServiceDetailDTO.builder()
                .id(detail.getId())
                .title(detail.getTitle())
                .content(detail.getContent())
                .build();
    }

    /**
     * Maps a collection of TourDayService entities to a list of TourDayServiceDTOs
     */
    private List<TourDayServiceDTO> mapToTourDayServiceDTOList(List<TourDayService> tourDayServices) {
        if (tourDayServices == null) {
            return null;
        }
        return tourDayServices.stream()
                .map(this::mapToTourDayServiceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps a TourDayService entity to a TourDayServiceDTO
     */
    private TourDayServiceDTO mapToTourDayServiceDTO(TourDayService tourDayService) {
        if (tourDayService == null) {
            return null;
        }
        TourDayServiceDTO dto = new TourDayServiceDTO();
        dto.setId(tourDayService.getId());
        dto.setQuantity(tourDayService.getQuantity());
        dto.setSellingPrice(tourDayService.getSellingPrice());

        if (tourDayService.getTourDay() != null) {
            TourDayDTO tourDayDTO = new TourDayDTO();
            tourDayDTO.setId(tourDayService.getTourDay().getId());
            //dto.setTourDay(tourDayDTO);
        }
        return dto;
    }
}
