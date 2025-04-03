package com.fpt.capstone.tourism.dto.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PublicServiceDetailDTO {
    ServiceProviderDTO detailServiceProviderDTO;
    List<ServiceProviderDTO> relatedService;
}
