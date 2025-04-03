package com.fpt.capstone.tourism.dto.request;

import com.fpt.capstone.tourism.dto.common.AuthorDTO;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogRequestDTO {
    private String thumbnailImageUrl;
    private String title;
    private String description;
    private String content;
    private AuthorDTO author;
    private List<TagDTO> tags;
}
