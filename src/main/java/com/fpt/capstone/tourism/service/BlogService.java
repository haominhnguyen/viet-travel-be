package com.fpt.capstone.tourism.service;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.BlogDTO;
import com.fpt.capstone.tourism.dto.request.BlogRequestDTO;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicBlogResponseDTO;
import com.fpt.capstone.tourism.model.Blog;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface BlogService {
    GeneralResponse<BlogResponseDTO> createBlog(BlogRequestDTO blogRequestDTO);
    GeneralResponse<BlogResponseDTO> updateBlog(Long id,BlogRequestDTO blogRequestDTO);
    GeneralResponse<BlogResponseDTO> getBlogById(Long id);
    GeneralResponse<BlogResponseDTO> changeBlogDeletedStatus(Long id, Boolean deleted);
    GeneralResponse<PagingDTO<List<BlogResponseDTO>>> getBlogs(int page, int size, String keyword, Boolean isDeleted, String sortField, String sortDirection);
    List<BlogResponseDTO> findNewestBlogs(int numberBlog);
    List<PublicBlogResponseDTO> getBlogsByTagName(String tagName, int numberOfBlogs);
    GeneralResponse<PagingDTO<List<PublicBlogResponseDTO>>> getNewestBlogs(int page, int size);
    GeneralResponse<List<PublicBlogResponseDTO>> getRandomBlogs(int blogNumber);
}
