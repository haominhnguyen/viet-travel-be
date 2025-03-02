package com.fpt.capstone.tourism.service.impl;

import com.fpt.capstone.tourism.dto.common.BlogDTO;
import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.TagDTO;
import com.fpt.capstone.tourism.dto.request.BlogRequestDTO;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicBlogResponseDTO;
import com.fpt.capstone.tourism.exception.common.BusinessException;
import com.fpt.capstone.tourism.mapper.BlogMapper;
import com.fpt.capstone.tourism.mapper.PublicBlogMapper;
import com.fpt.capstone.tourism.model.Blog;
import com.fpt.capstone.tourism.model.Tag;
import com.fpt.capstone.tourism.model.User;
import com.fpt.capstone.tourism.repository.BlogRepository;
import com.fpt.capstone.tourism.service.TagService;
import com.fpt.capstone.tourism.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogServiceImplTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserService userService;

    @Mock
    private TagService tagService;

    @Mock
    private BlogMapper blogMapper;

    @Mock
    private PublicBlogMapper publicBlogMapper;

    @InjectMocks
    private BlogServiceImpl blogService;

    private Blog blog;
    private BlogRequestDTO blogRequestDTO;
    private BlogResponseDTO blogResponseDTO;
    private User author;
    private List<Tag> tags;
    private List<TagDTO> tagDTOs;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(1L);

        tags = new ArrayList<>();
        tagDTOs = new ArrayList<>();

        blog = new Blog();
        blog.setId(1L);
        blog.setTitle("Test Blog");
        blog.setDescription("Test Description");
        blog.setContent("Test Content");
        blog.setThumbnailImageUrl("test.jpg");
        blog.setCreatedAt(LocalDateTime.now());
        blog.setAuthor(author);
        blog.setBlogTags(tags);
        blog.setDeleted(false);

        blogRequestDTO = new BlogRequestDTO();
        blogRequestDTO.setTitle("Test Blog");
        blogRequestDTO.setDescription("Test Description");
        blogRequestDTO.setContent("Test Content");
        blogRequestDTO.setThumbnailImageUrl("test.jpg");
        blogRequestDTO.setAuthor(new com.fpt.capstone.tourism.dto.common.AuthorDTO(1L, "Author Name","test.jpg","testemail@gmail.com"));
        blogRequestDTO.setTags(tagDTOs);

        blogResponseDTO = new BlogResponseDTO();
        blogResponseDTO.setId(1L);
        blogResponseDTO.setTitle("Test Blog");
        blogResponseDTO.setDescription("Test Description");
        blogResponseDTO.setContent("Test Content");
        blogResponseDTO.setThumbnailImageUrl("test.jpg");
        blogResponseDTO.setDeleted(false);
    }

    @Test
    void createBlog_Success() {
        when(blogMapper.toEntity(any(BlogRequestDTO.class))).thenReturn(blog);
        when(blogRepository.save(any())).thenReturn(blog);
        when(blogMapper.toDTO(any())).thenReturn(blogResponseDTO);

        GeneralResponse<BlogResponseDTO> response = blogService.createBlog(blogRequestDTO);

        assertNotNull(response);
        assertEquals(201, response.getStatus());
        assertEquals("Blog created successfully", response.getMessage());
        verify(blogRepository, times(1)).save(any());
    }


    @Test
    void updateBlog_Success() {
        when(blogRepository.findById(anyLong())).thenReturn(Optional.of(blog));
        when(userService.findById(anyLong())).thenReturn(author);
        when(tagService.findAllById(any())).thenReturn(tags);
        when(blogMapper.toDTO(any())).thenReturn(blogResponseDTO);

        GeneralResponse<BlogResponseDTO> response = blogService.updateBlog(1L, blogRequestDTO);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        verify(blogRepository, times(1)).save(any());
    }

    @Test
    void getBlogById_Success() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));
        when(blogMapper.toDTO(blog)).thenReturn(blogResponseDTO);

        GeneralResponse<BlogResponseDTO> response = blogService.getBlogById(1L);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        verify(blogRepository, times(1)).findById(1L);
    }

    @Test
    void changeBlogDeletedStatus_Success() {
        when(blogRepository.findById(1L)).thenReturn(Optional.of(blog));
        when(blogRepository.save(any())).thenReturn(blog);
        when(blogMapper.toDTO(any())).thenReturn(blogResponseDTO);

        GeneralResponse<BlogResponseDTO> response = blogService.changeBlogDeletedStatus(1L, false);

        assertNotNull(response);
        assertFalse(response.getData().getDeleted());
        verify(blogRepository, times(1)).save(any());
    }

    @Test
    void getRandomBlogs_Success() {
        when(blogRepository.findAll()).thenReturn(Collections.singletonList(blog));
        when(publicBlogMapper.blogToPublicBlogResponseDTO(any())).thenReturn(new PublicBlogResponseDTO());

        GeneralResponse<List<PublicBlogResponseDTO>> response = blogService.getRandomBlogs(1);

        assertNotNull(response);
        assertEquals(200, response.getStatus());
        verify(blogRepository, times(1)).findAll();
    }
}

