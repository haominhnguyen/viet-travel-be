package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicBlogResponseDTO;
import com.fpt.capstone.tourism.service.BlogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

class PublicBlogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BlogService blogService;

    @InjectMocks
    private PublicBlogController publicBlogController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(publicBlogController).build();
    }

    @Test
    void testGetFoodAndDrinkBlogs() throws Exception {
        PublicBlogResponseDTO blogResponseDTO = new PublicBlogResponseDTO();
        blogResponseDTO.setId(1L);
        blogResponseDTO.setTitle("Food Blog");

        List<PublicBlogResponseDTO> blogList = Collections.singletonList(blogResponseDTO);

        when(blogService.getBlogsByTagName(eq("Food & Drinks"), eq(3)))
                .thenReturn(blogList);

        mockMvc.perform(get("/public/blog/food-and-drinks")
                        .param("numberOfBlogs", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Food Blog"));
    }

    @Test
    void testGetAdventureBlogs() throws Exception {
        PublicBlogResponseDTO blogResponseDTO = new PublicBlogResponseDTO();
        blogResponseDTO.setId(2L);
        blogResponseDTO.setTitle("Adventure Blog");

        List<PublicBlogResponseDTO> blogList = Collections.singletonList(blogResponseDTO);

        when(blogService.getBlogsByTagName(eq("Adventure"), eq(6)))
                .thenReturn(blogList);

        mockMvc.perform(get("/public/blog/adventure")
                        .param("numberOfBlogs", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Adventure Blog"));
    }

    @Test
    void testGetCulturalBlogs() throws Exception {
        PublicBlogResponseDTO blogResponseDTO = new PublicBlogResponseDTO();
        blogResponseDTO.setId(3L);
        blogResponseDTO.setTitle("Cultural Blog");

        List<PublicBlogResponseDTO> blogList = Collections.singletonList(blogResponseDTO);

        when(blogService.getBlogsByTagName(eq("Cultural"), eq(3)))
                .thenReturn(blogList);

        mockMvc.perform(get("/public/blog/cultural")
                        .param("numberOfBlogs", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].title").value("Cultural Blog"));
    }

    @Test
    void testGetNewestBlogs() throws Exception {
        PublicBlogResponseDTO blogResponseDTO = new PublicBlogResponseDTO();
        blogResponseDTO.setId(4L);
        blogResponseDTO.setTitle("Newest Blog");

        List<PublicBlogResponseDTO> blogList = Collections.singletonList(blogResponseDTO);
        PagingDTO<List<PublicBlogResponseDTO>> pagingDTO = new PagingDTO<>(0, 10, 1, blogList);

        when(blogService.getNewestBlogs(0, 10))
                .thenReturn(new GeneralResponse<>(200, "Success", pagingDTO));

        mockMvc.perform(get("/public/blog/newest")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(4))
                .andExpect(jsonPath("$.data.items[0].title").value("Newest Blog"));
    }

    @Test
    void testGetBlogDetails() throws Exception {
        BlogResponseDTO blogResponseDTO = new BlogResponseDTO();
        blogResponseDTO.setId(5L);
        blogResponseDTO.setTitle("Blog Details");

        when(blogService.getBlogById(5L))
                .thenReturn(new GeneralResponse<>(200, "Success", blogResponseDTO));

        mockMvc.perform(get("/public/blog/details/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.title").value("Blog Details"));
    }

    @Test
    void testGetRandomBlogs() throws Exception {
        PublicBlogResponseDTO blogResponseDTO1 = new PublicBlogResponseDTO();
        blogResponseDTO1.setId(6L);
        blogResponseDTO1.setTitle("Random Blog 1");

        PublicBlogResponseDTO blogResponseDTO2 = new PublicBlogResponseDTO();
        blogResponseDTO2.setId(7L);
        blogResponseDTO2.setTitle("Random Blog 2");

        List<PublicBlogResponseDTO> blogList = Arrays.asList(blogResponseDTO1, blogResponseDTO2);

        when(blogService.getRandomBlogs(3))
                .thenReturn(new GeneralResponse<>(200, "Success", blogList));

        mockMvc.perform(get("/public/blog/random")
                        .param("blogNumber", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(6))
                .andExpect(jsonPath("$.data[0].title").value("Random Blog 1"))
                .andExpect(jsonPath("$.data[1].id").value(7))
                .andExpect(jsonPath("$.data[1].title").value("Random Blog 2"));
    }
}
