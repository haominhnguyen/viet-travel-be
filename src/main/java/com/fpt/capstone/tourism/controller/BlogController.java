package com.fpt.capstone.tourism.controller;


import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.common.BlogDTO;
import com.fpt.capstone.tourism.dto.request.BlogRequestDTO;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("marketing/blog")
public class BlogController {

    private final BlogService blogService;

    @PostMapping("/create")
    public ResponseEntity<GeneralResponse<BlogResponseDTO>> create(@RequestBody BlogRequestDTO blogRequestDTO) {
        return ResponseEntity.ok(blogService.createBlog(blogRequestDTO));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<GeneralResponse<BlogResponseDTO>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<GeneralResponse<BlogResponseDTO>> update(@PathVariable Long id,@RequestBody BlogRequestDTO blogRequestDTO) {
        return ResponseEntity.ok(blogService.updateBlog(id,blogRequestDTO));
    }

    @PostMapping("/change-status/{id}")
    public ResponseEntity<GeneralResponse<BlogResponseDTO>> delete(@PathVariable Long id, @RequestBody Boolean deleted) {
        return ResponseEntity.ok(blogService.changeBlogDeletedStatus(id, deleted));
    }

    @GetMapping("/list")
    public ResponseEntity<GeneralResponse<PagingDTO<List<BlogResponseDTO>>>> getBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ok(blogService.getBlogs(page, size, keyword, isDeleted, sortField, sortDirection));
    }
}
