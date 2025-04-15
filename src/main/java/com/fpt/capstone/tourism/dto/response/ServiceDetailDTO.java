package com.fpt.capstone.tourism.dto.response;

import com.fpt.capstone.tourism.dto.common.PaxPriceDTO;
import com.fpt.capstone.tourism.dto.common.ServiceAttributeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDetailDTO {
    private Long id;
    private String name;
    private String description;
    private Integer dayNumber;
    private String status; // ACTIVE, EXPIRED, UPCOMING
    private Double nettPrice;
    private Double sellingPrice;
    private Long locationId;
    private String locationName;
    private Long serviceProviderId;
    private String serviceProviderName;
    private String categoryName;
    private Date startDate;
    private Date endDate;
    private Map<Long, PaxPriceDTO> paxPrices; // Key: paxId, Value: PaxPriceDTO
    private List<ServiceAttributeDTO> attributes; // Additional attributes like room details, meal details, etc.


}
