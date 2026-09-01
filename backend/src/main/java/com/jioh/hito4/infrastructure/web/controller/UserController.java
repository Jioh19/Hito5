package com.jioh.hito4.infrastructure.web.controller;

import com.jioh.hito4.application.dto.LoginUserRequest;
import com.jioh.hito4.application.dto.RegisterUserRequest;
import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.application.usecase.DeleteUserUseCase;
import com.jioh.hito4.application.usecase.GetUserUseCase;
import com.jioh.hito4.application.usecase.GetUsersUseCase;
import com.jioh.hito4.application.usecase.LoginUseCase;
import com.jioh.hito4.application.usecase.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@Tag(name = "Users", description = "User identity registration, authentication and lookup")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final GetUserUseCase getUserUseCase;
    private final GetUsersUseCase getUsersUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UserController(RegisterUserUseCase registerUserUseCase,
                           LoginUseCase loginUseCase,
                           GetUserUseCase getUserUseCase,
                           GetUsersUseCase getUsersUseCase,
                           DeleteUserUseCase deleteUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.getUserUseCase = getUserUseCase;
        this.getUsersUseCase = getUsersUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @PostMapping
    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Invalid username, password or email"),
            @ApiResponse(responseCode = "422", description = "Username or email already registered")
    })
    public ResponseEntity<UserResponse> register(@RequestBody RegisterUserRequest request) {
        UserResponse created = registerUserUseCase.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/users/" + created.id())).body(created);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "404", description = "Wrong credentials")
    })
    public ResponseEntity<UserResponse> login(@RequestBody LoginUserRequest request) {
        return ResponseEntity.ok(loginUseCase.execute(request));
    }

    @GetMapping
    @Operation(summary = "List all users")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Users listed")})
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(getUsersUseCase.execute());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(getUserUseCase.execute(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
