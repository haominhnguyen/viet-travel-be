package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.ServiceDetailRequestDTO;
import com.fpt.capstone.tourism.dto.response.ServiceDetailResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.ServiceDetailFullMapper;
import com.fpt.capstone.tourism.model.Service;
import com.fpt.capstone.tourism.model.ServiceDetail;
import com.fpt.capstone.tourism.repository.ServiceDetailRepository;
import com.fpt.capstone.tourism.repository.ServiceRepository;
import com.fpt.capstone.tourism.service.ServiceDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.fpt.capstone.tourism.constants.Constants.Message.*;

@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor

public class ServiceDetailServiceImpl implements ServiceDetailService{

    private final ServiceDetailRepository serviceDetailRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceDetailFullMapper serviceDetailMapper;

    @Override
    public GeneralResponse<List<ServiceDetailResponseDTO>> getAllServiceDetails(Long serviceId, Long providerId) {
        try {
            // Check if service exists and belongs to provider
            Service service = serviceRepository.findByIdAndServiceProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            // Get all service details
            List<ServiceDetail> serviceDetails = serviceDetailRepository.findAllByServiceId(serviceId);

            return GeneralResponse.of(
                    serviceDetailMapper.toDTOList(serviceDetails),
                    SERVICE_DETAILS_RETRIEVED
            );
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(GET_SERVICE_DETAILS_FAIL, ex);
        }
    }

    @Override
    public GeneralResponse<ServiceDetailResponseDTO> getServiceDetailById(
            Long serviceId, Long detailId, Long providerId) {
        try {
            // Check if service exists and belongs to provider
            serviceRepository.findByIdAndServiceProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            // Get service detail
            ServiceDetail serviceDetail = serviceDetailRepository.findByIdAndServiceId(detailId, serviceId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_DETAIL_NOT_FOUND));

            return GeneralResponse.of(
                    serviceDetailMapper.toDTO(serviceDetail),
                    SERVICE_DETAIL_RETRIEVED
            );
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(GET_SERVICE_DETAIL_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<ServiceDetailResponseDTO> createServiceDetail(
            Long serviceId, Long providerId, ServiceDetailRequestDTO requestDTO) {
        try {
            // Validate input
            if (requestDTO.getTitle() == null || requestDTO.getTitle().trim().isEmpty() ||
                    requestDTO.getContent() == null || requestDTO.getContent().trim().isEmpty()) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Title and content are required");
            }

            // Get service and verify provider ID
            Service service = serviceRepository.findByIdAndServiceProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            // Check if service detail with the same title already exists
            boolean titleExists = serviceDetailRepository
                    .existsByServiceIdAndTitleIgnoreCase(serviceId, requestDTO.getTitle().trim());
            if (titleExists) {
                throw BusinessException.of(HttpStatus.CONFLICT, SERVICE_DETAIL_TITLE_EXISTS);
            }

            // Map and set additional fields
            ServiceDetail serviceDetail = serviceDetailMapper.toEntity(requestDTO);
            serviceDetail.setService(service);
            serviceDetail.setCreatedAt(LocalDateTime.now());

            // Save and return
            ServiceDetail savedDetail = serviceDetailRepository.save(serviceDetail);
            return GeneralResponse.of(
                    serviceDetailMapper.toDTO(savedDetail),
                    SERVICE_DETAIL_CREATED);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            throw BusinessException.of(CREATE_SERVICE_DETAIL_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<ServiceDetailResponseDTO> updateServiceDetail(
            Long serviceId, Long detailId, Long providerId, ServiceDetailRequestDTO requestDTO) {
        try {
            // Validate input
            if (requestDTO.getTitle() == null || requestDTO.getTitle().trim().isEmpty() ||
                    requestDTO.getContent() == null || requestDTO.getContent().trim().isEmpty()) {
                throw BusinessException.of(HttpStatus.BAD_REQUEST, "Title and content are required");
            }

            // Check if service exists and belongs to provider
            serviceRepository.findByIdAndServiceProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            // Get service detail
            ServiceDetail serviceDetail = serviceDetailRepository.findByIdAndServiceId(detailId, serviceId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_DETAIL_NOT_FOUND));

            // Check if another service detail with the same title already exists
            Optional<ServiceDetail> existingDetailWithTitle = serviceDetailRepository
                    .findByServiceIdAndTitleIgnoreCase(serviceId, requestDTO.getTitle().trim());

            if (existingDetailWithTitle.isPresent() && !existingDetailWithTitle.get().getId().equals(detailId)) {
                throw BusinessException.of(HttpStatus.CONFLICT, SERVICE_DETAIL_TITLE_EXISTS);
            }

            // Update fields
            serviceDetail.setTitle(requestDTO.getTitle());
            serviceDetail.setContent(requestDTO.getContent());
            serviceDetail.setUpdatedAt(LocalDateTime.now());

            // Save and return
            ServiceDetail updatedDetail = serviceDetailRepository.save(serviceDetail);
            return GeneralResponse.of(
                    serviceDetailMapper.toDTO(updatedDetail),
                    SERVICE_DETAIL_UPDATED
            );
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("Error updating service detail: ", ex);
            throw BusinessException.of(UPDATE_SERVICE_DETAIL_FAIL, ex);
        }
    }

    @Override
    @Transactional
    public GeneralResponse<ServiceDetailResponseDTO> changeServiceDetailStatus(Long serviceId, Long detailId, Boolean isDeleted, Long providerId) {
        try {
            // Check if service exists and belongs to provider
            serviceRepository.findByIdAndServiceProviderId(serviceId, providerId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_NOT_FOUND));

            // Check if service detail exists
            ServiceDetail serviceDetail = serviceDetailRepository.findByIdAndServiceId(detailId, serviceId)
                    .orElseThrow(() -> BusinessException.of(HttpStatus.NOT_FOUND, SERVICE_DETAIL_NOT_FOUND));

            // Update service detail status
            serviceDetail.setDeleted(isDeleted);
            serviceDetailRepository.save(serviceDetail);

            ServiceDetailResponseDTO responseDTO = new ServiceDetailResponseDTO();
            responseDTO.setId(serviceDetail.getId());
            responseDTO.setTitle(serviceDetail.getTitle());
            responseDTO.setContent(serviceDetail.getContent());
            responseDTO.setServiceId(serviceDetail.getService().getId());
            responseDTO.setCreatedAt(serviceDetail.getCreatedAt());
            responseDTO.setUpdatedAt(serviceDetail.getUpdatedAt());
            responseDTO.setDeleted(serviceDetail.getDeleted());

            String messageCode = isDeleted ? SERVICE_DETAIL_DELETED : SERVICE_DETAIL_RESTORED;
            return GeneralResponse.of(responseDTO, messageCode);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("Error changing service detail status: ", ex);
            throw BusinessException.of(CHANGE_SERVICE_DETAIL_STATUS_FAIL, ex);
        }
    }
}
