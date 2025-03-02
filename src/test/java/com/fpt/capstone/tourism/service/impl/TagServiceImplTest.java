package com.fpt.capstone.tourism.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.given;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.TagMapper;
import com.fpt.capstone.tourism.model.Tag;
import com.fpt.capstone.tourism.repository.TagRepository;
import com.fpt.capstone.tourism.service.impl.TagServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag1, tag2;
    private TagDTO tagDTO1, tagDTO2;

    @BeforeEach
    void setUp() {
        // Mock Tags
        tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("Adventure");

        tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("Relax");

        // Mock TagDTOs
        tagDTO1 = new TagDTO();
        tagDTO1.setId(1L);
        tagDTO1.setName("Adventure");

        tagDTO2 = new TagDTO();
        tagDTO2.setId(2L);
        tagDTO2.setName("Relax");
    }

    @Test
    void findAllById_Success() {
        List<Long> tagIds = Arrays.asList(1L, 2L);
        List<Tag> tags = Arrays.asList(tag1, tag2);

        when(tagRepository.findAllById(tagIds)).thenReturn(tags);

        List<Tag> result = tagService.findAllById(tagIds);

        assertEquals(2, result.size());
        assertEquals("Adventure", result.get(0).getName());
        assertEquals("Relax", result.get(1).getName());
    }

    @Test
    void findAll_Success() {
        List<Tag> tags = Arrays.asList(tag1, tag2);
        List<TagDTO> tagDTOs = Arrays.asList(tagDTO1, tagDTO2);

        when(tagRepository.findAll()).thenReturn(tags);
        when(tagMapper.toDTO(tag1)).thenReturn(tagDTO1);
        when(tagMapper.toDTO(tag2)).thenReturn(tagDTO2);

        GeneralResponse<List<TagDTO>> response = tagService.findAll();

        assertEquals(200, response.getStatus());
        assertEquals(2, response.getData().size());
        assertEquals("Adventure", response.getData().get(0).getName());
    }
}

