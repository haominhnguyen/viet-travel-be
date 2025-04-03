//package com.fpt.capstone.tourism.controller;
//
//import com.fpt.capstone.tourism.dto.common.GeneralResponse;
//import com.fpt.capstone.tourism.dto.request.PasswordChangeDTO;
//import com.fpt.capstone.tourism.dto.request.UserProfileRequestDTO;
//import com.fpt.capstone.tourism.dto.response.UserProfileResponseDTO;
//import com.fpt.capstone.tourism.service.UserService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.multipart.MultipartFile;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(MockitoExtension.class)  // ✅ Use Mockito JUnit 5 Extension
//class UserProfileControllerTest {
//
//    private MockMvc mockMvc;
//
//    @Mock  // ✅ Replace @MockBean with @Mock
//    private UserService userService;
//
//    @InjectMocks  // ✅ Inject the mocks into the controller
//    private UserProfileController userProfileController;
//
//    private UserProfileResponseDTO mockUserProfile;
//
//    @BeforeEach
//    void setUp() {
//        mockMvc = MockMvcBuilders.standaloneSetup(userProfileController).build();  // ✅ Create MockMvc manually
//
//        mockUserProfile = UserProfileResponseDTO.builder()
//                .id(1L)
//                .username("testuser")
//                .email("test@example.com")
//                .fullName("Test User")
//                .phone("123456789")
//                .gender(null)
//                .address("123 Test Street")
//                .avatarImg("https://example.com/avatar.jpg")
//                .build();
//    }
//
//    // ✅ Test Get User Profile (Success)
//    @Test
//    void testGetUserProfile_Success() throws Exception {
//        Mockito.when(userService.getUserProfile("testuser"))
//                .thenReturn(new GeneralResponse<>(200, "Success", mockUserProfile));
//
//        mockMvc.perform(get("/user-profile")
//                        .principal(() -> "testuser"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.message").value("Success"))
//                .andExpect(jsonPath("$.data.username").value("testuser"))
//                .andExpect(jsonPath("$.data.email").value("test@example.com"));
//    }
//
//    // ❌ Test Get User Profile (Unauthorized)
//    @Test
//    void testGetUserProfile_Unauthorized() throws Exception {
//        mockMvc.perform(get("/user-profile"))
//                .andDo(print())
//                .andExpect(status().isUnauthorized());
//    }
//
//    // ✅ Test Update User Profile
//    @Test
//    void testUpdateUserProfile_Success() throws Exception {
//        UserProfileRequestDTO requestDTO = new UserProfileRequestDTO("Updated Name", "updated@example.com", null, "987654321", "Updated Address");
//
//        Mockito.when(userService.updateUserProfile(eq(1L), any(UserProfileRequestDTO.class)))
//                .thenReturn(new GeneralResponse<>(200, "Profile updated successfully", mockUserProfile));
//
//        mockMvc.perform(post("/user-profile/update/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{ \"fullName\": \"Updated Name\", \"email\": \"updated@example.com\", \"phone\": \"987654321\", \"address\": \"Updated Address\" }"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.message").value("Profile updated successfully"));
//    }
//
//    // ✅ Test Change Password
//    @Test
//    void testChangePassword_Success() throws Exception {
//        PasswordChangeDTO passwordChangeDTO = new PasswordChangeDTO("oldPassword", "newPassword", "newPassword");
//
//        Mockito.when(userService.changePassword(any(), any(), any(), any()))
//                .thenReturn(new GeneralResponse<>(200, "Password changed successfully", null));
//
//        mockMvc.perform(post("/user-profile/change-password")
//                        .header("Authorization", "Bearer token")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{ \"currentPassword\": \"oldPassword\", \"newPassword\": \"newPassword\", \"newRePassword\": \"newPassword\" }"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.message").value("Password changed successfully"));
//    }
//
//    // ✅ Test Upload Avatar
//    @Test
//    void testUploadAvatar_Success() throws Exception {
//        MultipartFile file = Mockito.mock(MultipartFile.class);
//        Mockito.when(userService.updateAvatar(eq(1L), any(MultipartFile.class)))
//                .thenReturn(new GeneralResponse<>(200, "Avatar uploaded successfully", "avatar_url"));
//
//        mockMvc.perform(multipart("/user-profile/avatar/1")
//                        .file("avatar", "test image content".getBytes()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.message").value("Avatar uploaded successfully"));
//    }
//}
