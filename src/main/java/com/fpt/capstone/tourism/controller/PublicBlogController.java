package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.dto.response.BlogResponseDTO;
import com.fpt.capstone.tourism.dto.response.PagingDTO;
import com.fpt.capstone.tourism.dto.response.PublicBlogResponseDTO;
import com.fpt.capstone.tourism.model.Blog;
import com.fpt.capstone.tourism.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("public/blog")
public class PublicBlogController {
    private final BlogService blogService;

//    @GetMapping("")
//    public ResponseEntity<List<PublicBlogResponseDTO>> getPublic() {
//        List<PublicBlogResponseDTO> publicBlog = blogService.getPublicBlog();
//        return ResponseEntity.ok(publicBlog);
//    }

    @GetMapping("/food-and-drinks")
    public ResponseEntity<List<PublicBlogResponseDTO>> getFoodAndDrinkBlogs(
            @RequestParam(defaultValue = "3") int numberOfBlogs) {
        List<PublicBlogResponseDTO> foodAndDrinkBlogs = blogService.getBlogsByTagName("Ẩm thực", numberOfBlogs);
        return ResponseEntity.ok(foodAndDrinkBlogs);
    }

    @GetMapping("/adventure")
    public ResponseEntity<List<PublicBlogResponseDTO>> getAdventureBlogs(
            @RequestParam(defaultValue = "6") int numberOfBlogs) {
        List<PublicBlogResponseDTO> adventureBlogs = blogService.getBlogsByTagName("Phiêu lưu", numberOfBlogs);
        return ResponseEntity.ok(adventureBlogs);
    }

    @GetMapping("/cultural")
    public ResponseEntity<List<PublicBlogResponseDTO>> getCulturalBlogs(
            @RequestParam(defaultValue = "3") int numberOfBlogs) {
        List<PublicBlogResponseDTO> culturalBlogs = blogService.getBlogsByTagName("Văn hóa", numberOfBlogs);
        return ResponseEntity.ok(culturalBlogs);
    }

    @GetMapping("/newest")
    public ResponseEntity<GeneralResponse<PagingDTO<List<PublicBlogResponseDTO>>>> getNewestBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogService.getNewestBlogs(page, size));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<GeneralResponse<BlogResponseDTO>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogById(id));
    }

    @GetMapping("/random")
    public ResponseEntity<GeneralResponse<List<PublicBlogResponseDTO>>> getRandomBlogs(
            @RequestParam(defaultValue = "3") int blogNumber) {
        return ResponseEntity.ok(blogService.getRandomBlogs(blogNumber));
    }

}
