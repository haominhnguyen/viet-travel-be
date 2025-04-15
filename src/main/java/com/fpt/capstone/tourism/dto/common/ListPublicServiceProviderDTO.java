package com.fpt.capstone.tourism.dto.common;

import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicLocationDTO;
import com.fpt.capstone.tourism.dto.response.PublicServiceProviderDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ListPublicServiceProviderDTO {
    List<PublicLocationSimpleDTO> locationDTOS;
    GeneralResponse<PagingDTO<List<PublicServiceProviderDTO>>> publicServiceProviderDTOS;
}
