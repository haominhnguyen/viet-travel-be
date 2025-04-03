package com.fpt.capstone.tourism.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.fpt.capstone.tourism.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryServiceImpl cloudinaryService;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void uploadFile_Success() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        MultipartFile[] files = {file};
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "http://cloudinary.com/test.jpg");

        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        var result = cloudinaryService.uploadFile(files);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("http://cloudinary.com/test.jpg", result.get(0));
    }

    @Test
    void uploadAvatar_Success() throws Exception {
        MultipartFile file = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", "avatar data".getBytes());
        Long userId = 123L;
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "http://cloudinary.com/avatar_123.jpg");

        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        var result = cloudinaryService.uploadAvatar(file, userId);
        assertNotNull(result);
        assertEquals("http://cloudinary.com/avatar_123.jpg", result);
    }

    @Test
    void uploadFiles_Success() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        String folder = "test-folder";
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("url", "http://cloudinary.com/test-folder/test.jpg");

        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        var result = cloudinaryService.uploadFiles(file, folder);
        assertNotNull(result);
        assertEquals("http://cloudinary.com/test-folder/test.jpg", result);
    }

    @Test
    void uploadFile_Failure() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        MultipartFile[] files = {file};

        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("Upload failed"));

        assertThrows(RuntimeException.class, () -> cloudinaryService.uploadFile(files));
    }
}
