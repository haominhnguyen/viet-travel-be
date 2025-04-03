package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.request.BlogRequestDTO;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.BlogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

class BlogControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BlogService blogService;

    @InjectMocks
    private BlogController blogController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(blogController).build();
    }

    @Test
    void testCreateBlog() throws Exception {
        BlogRequestDTO blogRequestDTO = new BlogRequestDTO();
        blogRequestDTO.setTitle("New Blog Title");
        blogRequestDTO.setContent("This is a test blog.");

        BlogResponseDTO blogResponseDTO = new BlogResponseDTO();
        blogResponseDTO.setId(1L);
        blogResponseDTO.setTitle("New Blog Title");

        when(blogService.createBlog(any(BlogRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Blog created successfully", blogResponseDTO));

        mockMvc.perform(post("/marketing/blog/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blogRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Blog created successfully"))
                .andExpect(jsonPath("$.data.title").value("New Blog Title"));
    }

    @Test
    void testGetBlogDetails() throws Exception {
        BlogResponseDTO blogResponseDTO = new BlogResponseDTO();
        blogResponseDTO.setId(1L);
        blogResponseDTO.setTitle("Sample Blog");

        when(blogService.getBlogById(1L))
                .thenReturn(new GeneralResponse<>(200, "Success", blogResponseDTO));

        mockMvc.perform(get("/marketing/blog/details/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Sample Blog"));
    }

    @Test
    void testUpdateBlog() throws Exception {
        BlogRequestDTO blogRequestDTO = new BlogRequestDTO();
        blogRequestDTO.setTitle("Updated Blog Title");

        BlogResponseDTO blogResponseDTO = new BlogResponseDTO();
        blogResponseDTO.setId(1L);
        blogResponseDTO.setTitle("Updated Blog Title");

        when(blogService.updateBlog(eq(1L), any(BlogRequestDTO.class)))
                .thenReturn(new GeneralResponse<>(200, "Blog updated successfully", blogResponseDTO));

        mockMvc.perform(put("/marketing/blog/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blogRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Blog updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Blog Title"));
    }

    @Test
    void testChangeBlogStatus() throws Exception {
        BlogResponseDTO blogResponseDTO = new BlogResponseDTO();
        blogResponseDTO.setId(1L);
        blogResponseDTO.setDeleted(true);

        when(blogService.changeBlogDeletedStatus(eq(1L), eq(true)))
                .thenReturn(new GeneralResponse<>(200, "Blog status changed successfully", blogResponseDTO));

        mockMvc.perform(post("/marketing/blog/change-status/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("true")) // JSON Boolean value
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Blog status changed successfully"))
                .andExpect(jsonPath("$.data.deleted").value(true));
    }

    @Test
    void testGetBlogs() throws Exception {
        BlogResponseDTO blogResponseDTO = new BlogResponseDTO();
        blogResponseDTO.setId(1L);
        blogResponseDTO.setTitle("Trending Blog");

        List<BlogResponseDTO> blogList = Collections.singletonList(blogResponseDTO);
        PagingDTO<List<BlogResponseDTO>> pagingDTO = new PagingDTO<>(0, 10, 1, blogList);

        when(blogService.getBlogs(0, 10, null, null, "id", "desc"))
                .thenReturn(new GeneralResponse<>(200, "Success", pagingDTO));

        mockMvc.perform(get("/marketing/blog/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortField", "id")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].title").value("Trending Blog"));
    }
}
