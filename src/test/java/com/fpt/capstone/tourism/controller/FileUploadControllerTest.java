package com.fpt.capstone.tourism.controller;

import com.fpt.capstone.tourism.dto.common.GeneralResponse;
import com.fpt.capstone.tourism.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FileUploadControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private FileUploadController fileUploadController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fileUploadController).build();
    }

    @Test
    void testUploadFile() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file", "test-image.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes());

        String uploadedFileUrl = "https://cloudinary.com/fake-url/test-image.jpg";

        when(cloudinaryService.uploadFiles(any(), eq("general"))).thenReturn(uploadedFileUrl);

        mockMvc.perform(multipart("/public/upload-file")
                        .file(mockFile)
                        .param("folder", "general")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Files uploaded successfully"))
                .andExpect(jsonPath("$.data").value(uploadedFileUrl));
    }
}
