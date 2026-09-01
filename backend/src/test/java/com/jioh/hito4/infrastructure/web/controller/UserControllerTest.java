package com.jioh.hito4.infrastructure.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jioh.hito4.application.dto.LoginUserRequest;
import com.jioh.hito4.application.dto.RegisterUserRequest;
import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.application.usecase.DeleteUserUseCase;
import com.jioh.hito4.application.usecase.GetUserUseCase;
import com.jioh.hito4.application.usecase.GetUsersUseCase;
import com.jioh.hito4.application.usecase.LoginUseCase;
import com.jioh.hito4.application.usecase.RegisterUserUseCase;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @MockitoBean
    private GetUsersUseCase getUsersUseCase;

    @MockitoBean
    private DeleteUserUseCase deleteUserUseCase;

    @Test
    void register_returns201_onSuccess() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(registerUserUseCase.execute(any())).thenReturn(new UserResponse(1, "jioh", "jioh@example.com"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/users/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("jioh"))
                .andExpect(jsonPath("$.email").value("jioh@example.com"));
    }

    @Test
    void getById_returns400_whenIdIsNotNumeric() throws Exception {
        mockMvc.perform(get("/api/v1/users/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void register_returns400_whenBodyIsMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void register_returns422_whenUseCaseThrowsAlreadyExists() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        doThrow(new UserAlreadyExistsException("User already exists with username: jioh"))
                .when(registerUserUseCase).execute(any());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void login_returns200_withUserResponse() throws Exception {
        LoginUserRequest request = new LoginUserRequest("jioh", "pass123");
        when(loginUseCase.execute(any())).thenReturn(new UserResponse(1, "jioh", "jioh@example.com"));

        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jioh"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        when(getUserUseCase.execute(eq(99))).thenThrow(new UserNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void getAll_returns200_withList() throws Exception {
        when(getUsersUseCase.execute()).thenReturn(List.of(new UserResponse(1, "jioh", "jioh@example.com")));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("jioh"));
    }

    @Test
    void delete_returns204_onSuccess() throws Exception {
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }
}
