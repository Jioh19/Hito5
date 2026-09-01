# Hito 4 — Identity Microservice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the Hito1 CLI identity system into a productive, persistent, documented Spring Boot microservice that satisfies all three pillars of the official "Hito 4" rubric (semantic REST API + global error handling, Dockerized PostgreSQL persistence via JPA, and profile-gated Swagger/OpenAPI docs).

**Architecture:** Port the existing Hito1 domain and application layers (Clean Architecture / Ports & Adapters) into the `hito4` Spring Boot skeleton unchanged in spirit, replacing the CLI adapter with a REST controller adapter and replacing the in-memory/file repository adapters with a JPA + PostgreSQL adapter behind the same `IIdentityRepository` port. A `@RestControllerAdvice` centralizes exception-to-HTTP-status translation so no endpoint ever leaks a raw stack trace.

**Tech Stack:** Java 21, Spring Boot 4.1.1 (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`), PostgreSQL 16 (Docker), Spring Data JPA, springdoc-openapi (Swagger UI), JUnit 5 + Mockito, Docker Compose.

**Spec:** `/mnt/datakeep/Downloads/🚀 Guía para conseguir el 100% en el _Hito 4_.pdf` (rubric); reference implementation to port: `/mnt/datakeep/Projects/Globant/Hito1`.

## Global Constraints

- Java version: `21` (already pinned in `pom.xml`).
- Spring Boot parent: `4.1.1` (already pinned — do not change the version; if an artifact name from this plan fails to resolve, find the Boot-4-era equivalent name in the local `~/.m2` repo listing rather than downgrading the parent).
- Base package for all new code: `com.jioh.hito4` (matches the Spring Initializr skeleton already in the repo — required for component scanning to pick up beans automatically).
- Database credentials (as given): user `postgres`, password `1160`, database `hito4_db`, port `5432`.
- API base path: `/api/v1/users`, `@GetMapping` for reads (200), `@PostMapping` for creation (201).
- Zero raw stack traces in any HTTP response — every exception, including unexpected ones, is caught by `GlobalExceptionHandler` and translated into an `ErrorResponse` DTO.
- JPA annotations (`@Entity`, `@Table`, `@Id`, …) live only in `infrastructure.persistence.entity` — never in `domain`.
- Spring Data JPA repositories do CRUD with derived query methods only — no hand-written SQL.
- Swagger UI / OpenAPI docs are enabled under the `dev` profile and fully disabled (`enabled: false`) under the `prod` profile.

---

## File Structure

```
hito4/
├── compose.yaml                                  # PostgreSQL service (Task 4)
├── pom.xml                                        # + data-jpa, postgresql, springdoc (Task 3)
├── README.md                                      # rewritten deliverable (Task 10)
└── src
    ├── main/java/com/jioh/hito4
    │   ├── Hito4Application.java                   # already exists
    │   ├── domain                                  # Task 1 — ported verbatim from Hito1
    │   │   ├── entity/User.java
    │   │   ├── exception/{InvalidEmailException,InvalidUsernameException,UserAlreadyExistsException,UserNotFoundException}.java
    │   │   ├── repository/IIdentityRepository.java # Create() now returns User (Task 2)
    │   │   ├── service/AuthenticationPolicy.java
    │   │   └── valueobject/{Email,Username}.java
    │   ├── application                             # Task 2 — ported, RegisterUserUseCase adapted
    │   │   ├── dto/{RegisterUserRequest,LoginUserRequest,UserResponse}.java
    │   │   └── usecase/{RegisterUserUseCase,LoginUseCase,GetUserUseCase,GetUsersUseCase,DeleteUserUseCase}.java
    │   └── infrastructure
    │       ├── persistence                         # Task 5
    │       │   ├── entity/UserEntity.java
    │       │   ├── repository/UserJpaRepository.java
    │       │   └── JpaIdentityRepository.java
    │       └── web                                 # Tasks 6-7
    │           ├── dto/ErrorResponse.java
    │           └── controller/{GlobalExceptionHandler,UserController}.java
    ├── main/resources
    │   ├── application.yml                         # Task 9 (replaces application.properties)
    │   ├── application-dev.yml
    │   └── application-prod.yml
    └── test/java/com/jioh/hito4                    # mirrors main, one test class per production class
```

**Design decisions carried over from Hito1 (stated once, applies to every task below):**
- `InMemoryIdentityRepository`, `FileIdentityRepository`, `FileStorageException`, and the CLI `Main`/`MainTest` are **not** ported. The rubric explicitly requires replacing in-memory/file simulations with real DB persistence, and the CLI adapter is superseded by the REST controller adapter. Application-layer unit tests use Mockito mocks of `IIdentityRepository` (already the pattern in `RegisterUserUseCaseTest`), so no in-memory fake is needed for testing.
- `IIdentityRepository.Create(User user)` changes from `void` to `User` (returns the persisted entity, DB-assigned id included). This lets `RegisterUserUseCase` stop self-generating ids with a static counter — id generation is now the persistence adapter's job (`GenerationType.IDENTITY` in Postgres), which is the correct place for it now that a real DB exists.

---

### Task 1: Port the domain layer

**Files:**
- Create: `src/main/java/com/jioh/hito4/domain/entity/User.java`
- Create: `src/main/java/com/jioh/hito4/domain/valueobject/Email.java`
- Create: `src/main/java/com/jioh/hito4/domain/valueobject/Username.java`
- Create: `src/main/java/com/jioh/hito4/domain/exception/InvalidEmailException.java`
- Create: `src/main/java/com/jioh/hito4/domain/exception/InvalidUsernameException.java`
- Create: `src/main/java/com/jioh/hito4/domain/exception/UserAlreadyExistsException.java`
- Create: `src/main/java/com/jioh/hito4/domain/exception/UserNotFoundException.java`
- Create: `src/main/java/com/jioh/hito4/domain/service/AuthenticationPolicy.java`
- Create: `src/main/java/com/jioh/hito4/domain/repository/IIdentityRepository.java` (interface only — `Create` still returns `void` here; changed in Task 2)
- Test: `src/test/java/com/jioh/hito4/domain/entity/UserTest.java`
- Test: `src/test/java/com/jioh/hito4/domain/valueobject/EmailTest.java`
- Test: `src/test/java/com/jioh/hito4/domain/valueobject/UsernameTest.java`
- Test: `src/test/java/com/jioh/hito4/domain/service/AuthenticationPolicyTest.java`

**Interfaces:**
- Produces: `User(Integer id, Username username, String password, Email email, Instant timestamp)`, `Email(String value)`, `Username(String value)`, `AuthenticationPolicy.authenticate(User, String)`, `IIdentityRepository` port used by every later task.

- [ ] **Step 1: Copy the four production files verbatim, changing only the package declaration**

Copy content from Hito1, changing line 1 of each file from `package com.jioh...` to `package com.jioh.hito4...`:

`src/main/java/com/jioh/hito4/domain/entity/User.java`
```java
package com.jioh.hito4.domain.entity;

import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;

import java.time.Instant;
import java.util.Objects;

public class User {
    private final Integer id;
    private final Username username;
    private final String password;
    private final Email email;
    private final Instant timestamp;

    public User(Integer id, Username username, String password, Email email, Instant timestamp) {
        if (username == null) throw new IllegalArgumentException("Username must not be null");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password must not be empty");
        if (email == null) throw new IllegalArgumentException("Email must not be null");
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.timestamp = timestamp;
    }

    public Integer id() {
        return id;
    }

    public Username username() {
        return username;
    }

    public String password() {
        return password;
    }

    public Email email() {
        return email;
    }

    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
```

`src/main/java/com/jioh/hito4/domain/valueobject/Email.java`
```java
package com.jioh.hito4.domain.valueobject;

import com.jioh.hito4.domain.exception.InvalidEmailException;

public record Email(String value) {
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9]+([._%+-][a-zA-Z0-9]+)*@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public Email {
        String cleanValue = value == null ? null : value.trim().toLowerCase();

        if (cleanValue == null || !cleanValue.matches(EMAIL_REGEX)) {
            throw new InvalidEmailException("Invalid email address: " + value);
        }

        value = cleanValue;
    }
}
```

`src/main/java/com/jioh/hito4/domain/valueobject/Username.java`
```java
package com.jioh.hito4.domain.valueobject;

import com.jioh.hito4.domain.exception.InvalidUsernameException;

public record Username(String value) {
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9]{4,}$";

    public Username {
        String cleanValue = value == null ? null : value.trim().toLowerCase();

        if (cleanValue == null || !cleanValue.matches(USERNAME_REGEX)) {
            throw new InvalidUsernameException("Invalid username: " + value);
        }

        value = cleanValue;
    }
}
```

`src/main/java/com/jioh/hito4/domain/exception/InvalidEmailException.java`
```java
package com.jioh.hito4.domain.exception;

public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException(String message) {
        super(message);
    }
}
```

`src/main/java/com/jioh/hito4/domain/exception/InvalidUsernameException.java`
```java
package com.jioh.hito4.domain.exception;

public class InvalidUsernameException extends RuntimeException {
    public InvalidUsernameException(String message) {
        super(message);
    }
}
```

`src/main/java/com/jioh/hito4/domain/exception/UserAlreadyExistsException.java`
```java
package com.jioh.hito4.domain.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
```

`src/main/java/com/jioh/hito4/domain/exception/UserNotFoundException.java`
```java
package com.jioh.hito4.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
```

`src/main/java/com/jioh/hito4/domain/service/AuthenticationPolicy.java`
```java
package com.jioh.hito4.domain.service;

import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserNotFoundException;

public class AuthenticationPolicy {

    public void authenticate(User user, String rawPassword) {
        if (user == null) {
            throw new UserNotFoundException("Wrong credentials");
        }
        if (!user.password().equals(rawPassword)) {
            throw new UserNotFoundException("Wrong credentials");
        }
    }
}
```

`src/main/java/com/jioh/hito4/domain/repository/IIdentityRepository.java`
```java
package com.jioh.hito4.domain.repository;

import com.jioh.hito4.domain.entity.User;

import java.util.List;

public interface IIdentityRepository {
    User Get(String username);
    List<User> GetAll();
    User GetById(Integer id);
    boolean ExistsById(Integer id);
    boolean ExistsByEmail(String email);
    boolean ExistsByUsername(String username);
    void Create(User user);
    void Delete(Integer id);
}
```

- [ ] **Step 2: Copy the matching test files, changing only the package declaration**

Copy `UserTest.java`, `EmailTest.java`, `UsernameTest.java`, `AuthenticationPolicyTest.java` from `Hito1/src/test/java/com/jioh/...` into `hito4/src/test/java/com/jioh/hito4/...`, updating only the `package` line and any `import com.jioh....` lines to `import com.jioh.hito4....`.

- [ ] **Step 3: Run the tests**

```bash
./mvnw test -Dtest=UserTest,EmailTest,UsernameTest,AuthenticationPolicyTest
```
Expected: all tests PASS (this is a straight port, no new behavior).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/jioh/hito4/domain src/test/java/com/jioh/hito4/domain
git commit -m "feat: port domain layer from Hito1"
```

---

### Task 2: Port the application layer, with `Create()` returning the persisted `User`

**Files:**
- Modify: `src/main/java/com/jioh/hito4/domain/repository/IIdentityRepository.java` — `Create` now returns `User`
- Create: `src/main/java/com/jioh/hito4/application/dto/RegisterUserRequest.java`
- Create: `src/main/java/com/jioh/hito4/application/dto/LoginUserRequest.java`
- Create: `src/main/java/com/jioh/hito4/application/dto/UserResponse.java`
- Create: `src/main/java/com/jioh/hito4/application/usecase/RegisterUserUseCase.java`
- Create: `src/main/java/com/jioh/hito4/application/usecase/LoginUseCase.java`
- Create: `src/main/java/com/jioh/hito4/application/usecase/GetUserUseCase.java`
- Create: `src/main/java/com/jioh/hito4/application/usecase/GetUsersUseCase.java`
- Create: `src/main/java/com/jioh/hito4/application/usecase/DeleteUserUseCase.java`
- Test: `src/test/java/com/jioh/hito4/application/usecase/RegisterUserUseCaseTest.java`
- Test: `src/test/java/com/jioh/hito4/application/usecase/LoginUseCaseTest.java`
- Test: `src/test/java/com/jioh/hito4/application/usecase/GetUserUseCaseTest.java`
- Test: `src/test/java/com/jioh/hito4/application/usecase/GetUsersUseCaseTest.java`
- Test: `src/test/java/com/jioh/hito4/application/usecase/DeleteUserUseCaseTest.java`

**Interfaces:**
- Consumes: `IIdentityRepository`, `User`, `Email`, `Username`, `AuthenticationPolicy` from Task 1.
- Produces: `RegisterUserUseCase.execute(RegisterUserRequest)`, `LoginUseCase.execute(LoginUserRequest): UserResponse`, `GetUserUseCase.execute(Integer): UserResponse`, `GetUsersUseCase.execute(): List<UserResponse>`, `DeleteUserUseCase.execute(Integer)` — all consumed by the controller in Task 7.

- [ ] **Step 1: Widen the port — `Create` returns `User`**

Edit `IIdentityRepository.java`:
```java
package com.jioh.hito4.domain.repository;

import com.jioh.hito4.domain.entity.User;

import java.util.List;

public interface IIdentityRepository {
    User Get(String username);
    List<User> GetAll();
    User GetById(Integer id);
    boolean ExistsById(Integer id);
    boolean ExistsByEmail(String email);
    boolean ExistsByUsername(String username);
    User Create(User user);
    void Delete(Integer id);
}
```

- [ ] **Step 2: Copy the three DTOs verbatim (package rename only)**

`src/main/java/com/jioh/hito4/application/dto/RegisterUserRequest.java`
```java
package com.jioh.hito4.application.dto;

public record RegisterUserRequest(
        String username,
        String password,
        String email) {
}
```

`src/main/java/com/jioh/hito4/application/dto/LoginUserRequest.java`
```java
package com.jioh.hito4.application.dto;

public record LoginUserRequest(
        String username,
        String password
) {
}
```

`src/main/java/com/jioh/hito4/application/dto/UserResponse.java`
```java
package com.jioh.hito4.application.dto;

public record UserResponse(
        Integer id,
        String username,
        String email
) {
}
```

- [ ] **Step 3: Write the failing test for the new `RegisterUserUseCase` behavior**

`src/test/java/com/jioh/hito4/application/usecase/RegisterUserUseCaseTest.java`
```java
package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.RegisterUserRequest;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.InvalidEmailException;
import com.jioh.hito4.domain.exception.InvalidUsernameException;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private IIdentityRepository identityRepository;

    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCase(identityRepository);
    }

    @Test
    void execute_callsRepositoryCreate_whenNoDuplicates() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail("jioh@example.com")).thenReturn(false);
        when(identityRepository.ExistsByUsername("jioh")).thenReturn(false);
        when(identityRepository.Create(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new User(1, u.username(), u.password(), u.email(), u.timestamp());
        });

        registerUserUseCase.execute(dto);

        verify(identityRepository, times(1)).Create(any(User.class));
    }

    @Test
    void execute_passesUnpersistedUserWithNullId_toRepository() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail("jioh@example.com")).thenReturn(false);
        when(identityRepository.ExistsByUsername("jioh")).thenReturn(false);
        when(identityRepository.Create(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new User(1, u.username(), u.password(), u.email(), u.timestamp());
        });

        registerUserUseCase.execute(dto);

        verify(identityRepository).Create(org.mockito.ArgumentMatchers.argThat(user ->
                user.id() == null &&
                user.username().value().equals("jioh") &&
                user.password().equals("pass123") &&
                user.email().value().equals("jioh@example.com")
        ));
    }

    @Test
    void execute_throwsUserAlreadyExistsException_whenEmailIsDuplicated() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail("jioh@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> registerUserUseCase.execute(dto));
        verify(identityRepository, never()).Create(any(User.class));
    }

    @Test
    void execute_throwsUserAlreadyExistsException_whenUsernameIsDuplicated() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail("jioh@example.com")).thenReturn(false);
        when(identityRepository.ExistsByUsername("jioh")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> registerUserUseCase.execute(dto));
        verify(identityRepository, never()).Create(any(User.class));
    }

    @Test
    void execute_throwsInvalidUsernameException_whenUsernameIsBlank() {
        RegisterUserRequest dto = new RegisterUserRequest("", "pass123", "jioh@example.com");
        when(identityRepository.ExistsByEmail(anyString())).thenReturn(false);
        when(identityRepository.ExistsByUsername(anyString())).thenReturn(false);

        assertThrows(InvalidUsernameException.class, () -> registerUserUseCase.execute(dto));
    }

    @Test
    void execute_throwsIllegalArgumentException_whenPasswordIsBlank() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "", "jioh@example.com");
        when(identityRepository.ExistsByEmail(anyString())).thenReturn(false);
        when(identityRepository.ExistsByUsername(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> registerUserUseCase.execute(dto));
    }

    @Test
    void execute_throwsInvalidEmailException_whenEmailIsBlank() {
        RegisterUserRequest dto = new RegisterUserRequest("jioh", "pass123", "");
        when(identityRepository.ExistsByEmail(anyString())).thenReturn(false);
        when(identityRepository.ExistsByUsername(anyString())).thenReturn(false);

        assertThrows(InvalidEmailException.class, () -> registerUserUseCase.execute(dto));
    }
}
```

- [ ] **Step 4: Run the test to confirm it fails to compile (no `RegisterUserUseCase` yet)**

```bash
./mvnw test -Dtest=RegisterUserUseCaseTest
```
Expected: COMPILE ERROR — `RegisterUserUseCase` does not exist.

- [ ] **Step 5: Implement `RegisterUserUseCase` — id generation removed, delegated to the repository**

`src/main/java/com/jioh/hito4/application/usecase/RegisterUserUseCase.java`
```java
package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.RegisterUserRequest;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;

import java.time.Instant;

public class RegisterUserUseCase {
    private final IIdentityRepository identityRepository;

    public RegisterUserUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public void execute(RegisterUserRequest registerUser) {
        if (identityRepository.ExistsByEmail(registerUser.email())) {
            throw new UserAlreadyExistsException("User already exists with email: " + registerUser.email());
        }
        if (identityRepository.ExistsByUsername(registerUser.username())) {
            throw new UserAlreadyExistsException("User already exists with username: " + registerUser.username());
        }
        identityRepository.Create(createUser(registerUser));
    }

    private User createUser(RegisterUserRequest registerUser) {
        return new User(
                null,
                new Username(registerUser.username()),
                registerUser.password(),
                new Email(registerUser.email()),
                Instant.now());
    }
}
```

- [ ] **Step 6: Run the test to confirm it passes**

```bash
./mvnw test -Dtest=RegisterUserUseCaseTest
```
Expected: PASS.

- [ ] **Step 7: Port the remaining use cases verbatim (package rename only) and their tests**

`src/main/java/com/jioh/hito4/application/usecase/LoginUseCase.java`
```java
package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.LoginUserRequest;
import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.service.AuthenticationPolicy;

public class LoginUseCase {
    private final IIdentityRepository identityRepository;
    private final AuthenticationPolicy authenticationPolicy = new AuthenticationPolicy();

    public LoginUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public UserResponse execute(LoginUserRequest loginUserRequest) {
        User user = identityRepository.Get(loginUserRequest.username());
        authenticationPolicy.authenticate(user, loginUserRequest.password());
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.username().value(), user.email().value());
    }
}
```

`src/main/java/com/jioh/hito4/application/usecase/GetUserUseCase.java`
```java
package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.domain.repository.IIdentityRepository;

public class GetUserUseCase {
    private final IIdentityRepository identityRepository;

    public GetUserUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public UserResponse execute(Integer id) {
        User user = identityRepository.GetById(id);
        if (user == null) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.username().value(), user.email().value());
    }
}
```

`src/main/java/com/jioh/hito4/application/usecase/GetUsersUseCase.java`
```java
package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.repository.IIdentityRepository;

import java.util.List;

public class GetUsersUseCase {
    private final IIdentityRepository identityRepository;

    public GetUsersUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public List<UserResponse> execute() {
        List<User> users = identityRepository.GetAll();
        return users.stream().map(this::toResponse).toList();
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.id(), user.username().value(), user.email().value());
    }
}
```

`src/main/java/com/jioh/hito4/application/usecase/DeleteUserUseCase.java`
```java
package com.jioh.hito4.application.usecase;

import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.domain.repository.IIdentityRepository;

public class DeleteUserUseCase {
    private final IIdentityRepository identityRepository;

    public DeleteUserUseCase(IIdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    public void execute(Integer id) {
        if (!identityRepository.ExistsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        identityRepository.Delete(id);
    }
}
```

Copy `LoginUseCaseTest.java`, `GetUserUseCaseTest.java`, `GetUsersUseCaseTest.java`, `DeleteUserUseCaseTest.java` from Hito1 verbatim, updating only `package`/`import` lines from `com.jioh.` to `com.jioh.hito4.` (these tests never call `Create`, so no other changes are needed).

- [ ] **Step 8: Run the full application-layer test suite**

```bash
./mvnw test -Dtest=RegisterUserUseCaseTest,LoginUseCaseTest,GetUserUseCaseTest,GetUsersUseCaseTest,DeleteUserUseCaseTest
```
Expected: all PASS.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/jioh/hito4/application src/main/java/com/jioh/hito4/domain/repository/IIdentityRepository.java src/test/java/com/jioh/hito4/application
git commit -m "feat: port application layer, repository Create() now returns persisted User"
```

---

### Task 3: Add persistence and documentation dependencies

**Files:**
- Modify: `pom.xml`

**Interfaces:**
- Produces: `spring-boot-starter-data-jpa` (JPA/Hibernate + `JpaRepository`), `org.postgresql:postgresql` (JDBC driver), `springdoc-openapi-starter-webmvc-ui` (Swagger UI + OpenAPI annotations) — consumed by Tasks 5 and 8.

- [ ] **Step 1: Add the three dependencies to `<dependencies>`**

Insert into `pom.xml` right after the existing `spring-boot-starter-webmvc` dependency (around line 40):
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

- [ ] **Step 2: Resolve and compile to verify the artifact names are correct for this Boot version**

```bash
./mvnw -q compile
```
Expected: BUILD SUCCESS (no dependency-resolution errors). If `spring-boot-starter-data-jpa` or `postgresql` fails to resolve under Spring Boot 4.1.1's dependency-management BOM, list the actually-available artifacts with `find ~/.m2/repository/org/springframework/boot -maxdepth 1 -name '*jpa*'` and swap in the correct name — the `webmvc`/`restclient` renames already present in this pom show Boot 4 renamed some `-web` starters, so `-data-jpa` may follow the same pattern (e.g. `spring-boot-starter-datajpa`). Adjust and re-run until it resolves; the persistence-layer code in Task 5 is unaffected by the exact artifact name.

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: add JPA, PostgreSQL driver, and springdoc-openapi dependencies"
```

---

### Task 4: Dockerized PostgreSQL

**Files:**
- Modify: `compose.yaml`

**Interfaces:**
- Produces: a running PostgreSQL 16 instance on `localhost:5432`, database `hito4_db`, user `postgres`, password `1160`, with a named persistent volume — consumed by every task from here on that needs a live DB (Task 5's integration test, Task 9's `application.yml`, Task 11's manual verification).

- [ ] **Step 1: Replace the empty `compose.yaml`**

```yaml
services:
  db:
    image: postgres:16-alpine
    container_name: hito4-postgres-db
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: hito4_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: "1160"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

- [ ] **Step 2: Start it and verify it comes up healthy**

```bash
docker compose up -d
docker compose ps
```
Expected: `hito4-postgres-db` service state `running`/`healthy`.

```bash
docker exec -it hito4-postgres-db psql -U postgres -d hito4_db -c '\conninfo'
```
Expected: prints connection info with no auth error.

- [ ] **Step 3: Commit**

```bash
git add compose.yaml
git commit -m "feat: add PostgreSQL 16 docker-compose service with persistent volume"
```

---

### Task 5: JPA persistence adapter

**Files:**
- Create: `src/main/java/com/jioh/hito4/infrastructure/persistence/entity/UserEntity.java`
- Create: `src/main/java/com/jioh/hito4/infrastructure/persistence/repository/UserJpaRepository.java`
- Create: `src/main/java/com/jioh/hito4/infrastructure/persistence/JpaIdentityRepository.java`
- Test: `src/test/java/com/jioh/hito4/infrastructure/persistence/JpaIdentityRepositoryTest.java`

**Interfaces:**
- Consumes: `IIdentityRepository` port, `User`, `Email`, `Username` (Tasks 1-2).
- Produces: `JpaIdentityRepository` — the single Spring-managed `IIdentityRepository` bean the use cases (Task 2) and controller (Task 7) will be wired against.

**Prerequisite:** the Task 4 database must be running (`docker compose up -d`) before running this task's test — it hits a real Postgres instance, no mocking.

- [ ] **Step 1: Write the failing integration test**

`src/test/java/com/jioh/hito4/infrastructure/persistence/JpaIdentityRepositoryTest.java`
```java
package com.jioh.hito4.infrastructure.persistence;

import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class JpaIdentityRepositoryTest {

    @Autowired
    private JpaIdentityRepository repository;

    @Test
    void create_assignsAnIdAndPersistsTheUser() {
        User toCreate = new User(null, new Username("jpauser"), "pass123", new Email("jpa@example.com"), Instant.now());

        User created = repository.Create(toCreate);

        assertNotNull(created.id());
        assertEquals("jpauser", created.username().value());
        assertTrue(repository.ExistsById(created.id()));
        assertTrue(repository.ExistsByUsername("jpauser"));
        assertTrue(repository.ExistsByEmail("jpa@example.com"));
    }

    @Test
    void get_returnsNull_whenUsernameDoesNotExist() {
        assertNull(repository.Get("nobody"));
    }

    @Test
    void delete_removesTheUser() {
        User created = repository.Create(new User(null, new Username("todelete"), "pass123", new Email("todelete@example.com"), Instant.now()));

        repository.Delete(created.id());

        assertFalse(repository.ExistsById(created.id()));
    }

    @Test
    void getAll_includesCreatedUsers() {
        repository.Create(new User(null, new Username("listuser"), "pass123", new Email("listuser@example.com"), Instant.now()));

        assertTrue(repository.GetAll().stream().anyMatch(u -> u.username().value().equals("listuser")));
    }
}
```

- [ ] **Step 2: Run it to confirm it fails to compile (no `UserEntity`/`UserJpaRepository`/`JpaIdentityRepository` yet)**

```bash
docker compose up -d
./mvnw test -Dtest=JpaIdentityRepositoryTest
```
Expected: COMPILE ERROR.

- [ ] **Step 3: Implement the JPA entity**

`src/main/java/com/jioh/hito4/infrastructure/persistence/entity/UserEntity.java`
```java
package com.jioh.hito4.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserEntity() {
    }

    public UserEntity(Integer id, String username, String password, String email, Instant createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
```

- [ ] **Step 4: Implement the Spring Data repository**

`src/main/java/com/jioh/hito4/infrastructure/persistence/repository/UserJpaRepository.java`
```java
package com.jioh.hito4.infrastructure.persistence.repository;

import com.jioh.hito4.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Integer> {
    // Spring Data JPA derives every query below from the method name — no manual SQL.
    Optional<UserEntity> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
```

- [ ] **Step 5: Implement the port adapter**

`src/main/java/com/jioh/hito4/infrastructure/persistence/JpaIdentityRepository.java`
```java
package com.jioh.hito4.infrastructure.persistence;

import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import com.jioh.hito4.infrastructure.persistence.entity.UserEntity;
import com.jioh.hito4.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaIdentityRepository implements IIdentityRepository {

    private final UserJpaRepository userJpaRepository;

    public JpaIdentityRepository(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User Get(String username) {
        return userJpaRepository.findByUsername(username).map(this::toDomain).orElse(null);
    }

    @Override
    public List<User> GetAll() {
        return userJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public User GetById(Integer id) {
        return userJpaRepository.findById(id).map(this::toDomain).orElse(null);
    }

    @Override
    public boolean ExistsById(Integer id) {
        return userJpaRepository.existsById(id);
    }

    @Override
    public boolean ExistsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean ExistsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public User Create(User user) {
        UserEntity saved = userJpaRepository.save(toEntity(user));
        return toDomain(saved);
    }

    @Override
    public void Delete(Integer id) {
        userJpaRepository.deleteById(id);
    }

    private UserEntity toEntity(User user) {
        return new UserEntity(user.id(), user.username().value(), user.password(), user.email().value(), user.timestamp());
    }

    private User toDomain(UserEntity entity) {
        return new User(entity.getId(), new Username(entity.getUsername()), entity.getPassword(), new Email(entity.getEmail()), entity.getCreatedAt());
    }
}
```

- [ ] **Step 6: Run the test to confirm it passes**

```bash
./mvnw test -Dtest=JpaIdentityRepositoryTest
```
Expected: PASS (requires the Task 4 database running and Task 9's `dev` datasource config — if Task 9 isn't done yet, temporarily add the four `spring.datasource.*` lines from Task 9 Step 1 to `src/main/resources/application.properties` to unblock this test, then remove them once Task 9 lands).

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/jioh/hito4/infrastructure/persistence src/test/java/com/jioh/hito4/infrastructure/persistence
git commit -m "feat: add JPA-backed IIdentityRepository adapter over PostgreSQL"
```

---

### Task 6: Global exception handler and error DTO

**Files:**
- Create: `src/main/java/com/jioh/hito4/infrastructure/web/dto/ErrorResponse.java`
- Create: `src/main/java/com/jioh/hito4/infrastructure/web/controller/GlobalExceptionHandler.java`
- Test: `src/test/java/com/jioh/hito4/infrastructure/web/controller/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Consumes: `InvalidEmailException`, `InvalidUsernameException`, `UserAlreadyExistsException`, `UserNotFoundException` (Task 1).
- Produces: `ErrorResponse(String message, String code, LocalDateTime timestamp)` — every controller error response body uses this shape.

- [ ] **Step 1: Write the failing unit test (plain JUnit, no Spring context needed)**

`src/test/java/com/jioh/hito4/infrastructure/web/controller/GlobalExceptionHandlerTest.java`
```java
package com.jioh.hito4.infrastructure.web.controller;

import com.jioh.hito4.domain.exception.InvalidUsernameException;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.infrastructure.web.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_returns400_withValidationErrorCode() {
        ResponseEntity<ErrorResponse> response = handler.handleValidation(new InvalidUsernameException("Invalid username: x"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals("Invalid username: x", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleAlreadyExists_returns409_withConflictCode() {
        ResponseEntity<ErrorResponse> response = handler.handleAlreadyExists(new UserAlreadyExistsException("User already exists with username: jioh"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("RESOURCE_CONFLICT", response.getBody().code());
    }

    @Test
    void handleNotFound_returns404_withNotFoundCode() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new UserNotFoundException("User not found with id: 1"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().code());
    }

    @Test
    void handleUnexpected_returns500_withoutLeakingTheOriginalMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("column \"pwd\" does not exist at line 42"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }
}
```

- [ ] **Step 2: Run it to confirm it fails to compile**

```bash
./mvnw test -Dtest=GlobalExceptionHandlerTest
```
Expected: COMPILE ERROR.

- [ ] **Step 3: Implement the DTO and handler**

`src/main/java/com/jioh/hito4/infrastructure/web/dto/ErrorResponse.java`
```java
package com.jioh.hito4.infrastructure.web.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String code,
        LocalDateTime timestamp
) {
}
```

`src/main/java/com/jioh/hito4/infrastructure/web/controller/GlobalExceptionHandler.java`
```java
package com.jioh.hito4.infrastructure.web.controller;

import com.jioh.hito4.domain.exception.InvalidEmailException;
import com.jioh.hito4.domain.exception.InvalidUsernameException;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.infrastructure.web.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidEmailException.class, InvalidUsernameException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidation(RuntimeException ex) {
        return build(ex.getMessage(), "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(UserAlreadyExistsException ex) {
        return build(ex.getMessage(), "RESOURCE_CONFLICT", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return build(ex.getMessage(), "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build("An unexpected error occurred", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> build(String message, String code, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(message, code, LocalDateTime.now()), status);
    }
}
```

- [ ] **Step 4: Run the test to confirm it passes**

```bash
./mvnw test -Dtest=GlobalExceptionHandlerTest
```
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/jioh/hito4/infrastructure/web src/test/java/com/jioh/hito4/infrastructure/web
git commit -m "feat: centralize exception-to-HTTP-status translation with @RestControllerAdvice"
```

---

### Task 7: REST controller

**Files:**
- Modify: `src/main/java/com/jioh/hito4/application/usecase/RegisterUserUseCase.java` — add `@Service`
- Modify: `src/main/java/com/jioh/hito4/application/usecase/LoginUseCase.java` — add `@Service`
- Modify: `src/main/java/com/jioh/hito4/application/usecase/GetUserUseCase.java` — add `@Service`
- Modify: `src/main/java/com/jioh/hito4/application/usecase/GetUsersUseCase.java` — add `@Service`
- Modify: `src/main/java/com/jioh/hito4/application/usecase/DeleteUserUseCase.java` — add `@Service`
- Create: `src/main/java/com/jioh/hito4/infrastructure/web/controller/UserController.java`
- Test: `src/test/java/com/jioh/hito4/infrastructure/web/controller/UserControllerTest.java`

**Interfaces:**
- Consumes: the five use cases (Task 2), `JpaIdentityRepository` as their sole autowired `IIdentityRepository` implementation (Task 5 — Spring autowires by unique type, no qualifier needed), `GlobalExceptionHandler`/`ErrorResponse` (Task 6).
- Produces: `POST /api/v1/users` (201), `POST /api/v1/users/login` (200), `GET /api/v1/users` (200), `GET /api/v1/users/{id}` (200), `DELETE /api/v1/users/{id}` (204).

- [ ] **Step 1: Annotate the five use cases as Spring beans**

Add `import org.springframework.stereotype.Service;` and `@Service` above the class declaration in each of `RegisterUserUseCase`, `LoginUseCase`, `GetUserUseCase`, `GetUsersUseCase`, `DeleteUserUseCase`. Example for `RegisterUserUseCase.java`:
```java
package com.jioh.hito4.application.usecase;

import com.jioh.hito4.application.dto.RegisterUserRequest;
import com.jioh.hito4.domain.entity.User;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.repository.IIdentityRepository;
import com.jioh.hito4.domain.valueobject.Email;
import com.jioh.hito4.domain.valueobject.Username;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RegisterUserUseCase {
    // ... unchanged body from Task 2
}
```
Apply the same `@Service` addition (import + annotation, no other changes) to the other four use case classes.

- [ ] **Step 2: Write the failing controller test**

`src/test/java/com/jioh/hito4/infrastructure/web/controller/UserControllerTest.java`
```java
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void register_returns409_whenUseCaseThrowsAlreadyExists() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("jioh", "pass123", "jioh@example.com");
        doThrow(new UserAlreadyExistsException("User already exists with username: jioh"))
                .when(registerUserUseCase).execute(any());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));
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
```

- [ ] **Step 3: Run it to confirm it fails to compile (no `UserController` yet)**

```bash
./mvnw test -Dtest=UserControllerTest
```
Expected: COMPILE ERROR.

- [ ] **Step 4: Implement the controller**

`src/main/java/com/jioh/hito4/infrastructure/web/controller/UserController.java`
```java
package com.jioh.hito4.infrastructure.web.controller;

import com.jioh.hito4.application.dto.LoginUserRequest;
import com.jioh.hito4.application.dto.RegisterUserRequest;
import com.jioh.hito4.application.dto.UserResponse;
import com.jioh.hito4.application.usecase.DeleteUserUseCase;
import com.jioh.hito4.application.usecase.GetUserUseCase;
import com.jioh.hito4.application.usecase.GetUsersUseCase;
import com.jioh.hito4.application.usecase.LoginUseCase;
import com.jioh.hito4.application.usecase.RegisterUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
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
    public ResponseEntity<Void> register(@RequestBody RegisterUserRequest request) {
        registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginUserRequest request) {
        return ResponseEntity.ok(loginUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(getUsersUseCase.execute());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(getUserUseCase.execute(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 5: Run the test to confirm it passes**

```bash
./mvnw test -Dtest=UserControllerTest
```
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/jioh/hito4/application/usecase src/main/java/com/jioh/hito4/infrastructure/web/controller/UserController.java src/test/java/com/jioh/hito4/infrastructure/web/controller/UserControllerTest.java
git commit -m "feat: expose semantic REST controller over /api/v1/users"
```

---

### Task 8: OpenAPI/Swagger annotations

**Files:**
- Modify: `src/main/java/com/jioh/hito4/infrastructure/web/controller/UserController.java` — add `@Tag`, `@Operation`, `@ApiResponses`
- Modify: `src/main/java/com/jioh/hito4/application/dto/RegisterUserRequest.java` — add `@Schema`
- Modify: `src/main/java/com/jioh/hito4/application/dto/LoginUserRequest.java` — add `@Schema`
- Modify: `src/main/java/com/jioh/hito4/application/dto/UserResponse.java` — add `@Schema`

**Interfaces:**
- Consumes: `springdoc-openapi-starter-webmvc-ui` (Task 3).
- Produces: an OpenAPI 3 contract discoverable at `/api-docs` and a browsable console at `/swagger-ui.html` once Task 9's `dev` profile is active — verified manually in Task 11 (documentation annotations aren't something a unit test meaningfully asserts).

- [ ] **Step 1: Annotate the DTOs**

`src/main/java/com/jioh/hito4/application/dto/RegisterUserRequest.java`
```java
package com.jioh.hito4.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterUserRequest(
        @Schema(description = "Unique username, at least 4 alphanumeric characters", example = "jioh")
        String username,
        @Schema(description = "Account password", example = "pass123")
        String password,
        @Schema(description = "Unique email address", example = "jioh@example.com")
        String email) {
}
```

`src/main/java/com/jioh/hito4/application/dto/LoginUserRequest.java`
```java
package com.jioh.hito4.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginUserRequest(
        @Schema(description = "Username to authenticate", example = "jioh")
        String username,
        @Schema(description = "Account password", example = "pass123")
        String password
) {
}
```

`src/main/java/com/jioh/hito4/application/dto/UserResponse.java`
```java
package com.jioh.hito4.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
        @Schema(description = "Generated user id", example = "1")
        Integer id,
        @Schema(description = "Username", example = "jioh")
        String username,
        @Schema(description = "Email address", example = "jioh@example.com")
        String email
) {
}
```

- [ ] **Step 2: Annotate the controller**

Replace the class declaration and each mapping method's annotations in `UserController.java`:
```java
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
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
            @ApiResponse(responseCode = "409", description = "Username or email already registered")
    })
    public ResponseEntity<Void> register(@RequestBody RegisterUserRequest request) {
        registerUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
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
```

- [ ] **Step 3: Run the existing controller test to confirm the annotations didn't break behavior**

```bash
./mvnw test -Dtest=UserControllerTest
```
Expected: PASS (annotations are metadata only, no behavior change).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/jioh/hito4/infrastructure/web/controller/UserController.java src/main/java/com/jioh/hito4/application/dto
git commit -m "docs: annotate controller and DTOs for OpenAPI contract generation"
```

---

### Task 9: Profile-gated configuration

**Files:**
- Delete: `src/main/resources/application.properties`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-dev.yml`
- Create: `src/main/resources/application-prod.yml`

**Interfaces:**
- Consumes: Task 4's DB credentials, Task 3's springdoc dependency.
- Produces: the datasource config the Task 5 integration test needs, and the `dev`/`prod` Swagger toggle Task 8's contracts are viewed through.

- [ ] **Step 1: Remove the default properties file and add the base YAML**

```bash
rm src/main/resources/application.properties
```

`src/main/resources/application.yml`
```yaml
spring:
  application:
    name: hito4
  profiles:
    active: dev
  datasource:
    url: jdbc:postgresql://localhost:5432/hito4_db
    username: postgres
    password: "1160"
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
```

- [ ] **Step 2: Add the dev profile — Swagger enabled**

`src/main/resources/application-dev.yml`
```yaml
spring:
  jpa:
    show-sql: true

springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
```

- [ ] **Step 3: Add the prod profile — Swagger fully disabled**

`src/main/resources/application-prod.yml`
```yaml
spring:
  jpa:
    show-sql: false

springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

- [ ] **Step 4: Re-run the full test suite to confirm nothing regressed**

```bash
docker compose up -d
./mvnw test
```
Expected: all tests PASS, including `JpaIdentityRepositoryTest` and `UserControllerTest` from earlier tasks.

- [ ] **Step 5: Commit**

```bash
git add src/main/resources
git commit -m "feat: split config into base/dev/prod profiles, disable Swagger outside dev"
```

---

### Task 10: README.md

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Write the deliverable README**

```markdown
# Hito4 — Identity Microservice

A productive, persistent Spring Boot microservice for user identity management: registration, login, lookup, listing, and deletion. Evolves the Hito1 CLI domain/application core (Clean Architecture / Ports & Adapters) by swapping the CLI adapter for a REST controller adapter and the in-memory/file repository adapters for a JPA + PostgreSQL adapter, all behind the same `IIdentityRepository` port.

## Tech Stack

Java 21, Spring Boot 4.1.1 (Web MVC, Spring Data JPA), PostgreSQL 16, Docker Compose, springdoc-openapi (Swagger UI).

## Architecture

- **Domain** (`domain.entity`, `domain.valueobject`, `domain.exception`, `domain.service`, `domain.repository`) — framework-agnostic business rules, unchanged from Hito1.
- **Application** (`application.usecase`, `application.dto`) — one use case per business flow, Spring `@Service` beans injected with the `IIdentityRepository` port.
- **Infrastructure**
  - `infrastructure.persistence` — `UserEntity` (JPA mapping), `UserJpaRepository` (Spring Data JPA), `JpaIdentityRepository` (adapter implementing the domain's `IIdentityRepository` port over PostgreSQL).
  - `infrastructure.web` — `UserController` (REST adapter), `GlobalExceptionHandler` (`@RestControllerAdvice`, translates every domain exception into a semantic HTTP status + `ErrorResponse` body — no endpoint ever returns a raw stack trace).

## Running the Database

```bash
docker compose up -d
```

Starts PostgreSQL 16 on `localhost:5432` (database `hito4_db`), with a named volume so data survives restarts.

## Running the Application (Development Mode)

```bash
./mvnw spring-boot:run
```

Runs under the `dev` profile (default), which points at the local Dockerized database and enables Swagger UI.

## API Documentation and Contract Testing

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

Both are disabled when the `prod` profile is active (`SPRING_PROFILES_ACTIVE=prod`), returning 404 to eliminate the documentation attack surface outside local development.

## API Reference

| Method | Path | Description | Success | Error(s) |
|---|---|---|---|---|
| POST | `/api/v1/users` | Register a user | 201 | 400, 409 |
| POST | `/api/v1/users/login` | Authenticate | 200 | 404 |
| GET | `/api/v1/users` | List users | 200 | — |
| GET | `/api/v1/users/{id}` | Get a user by id | 200 | 404 |
| DELETE | `/api/v1/users/{id}` | Delete a user | 204 | 404 |

Every error response is a JSON `ErrorResponse`: `{ "message": "...", "code": "...", "timestamp": "..." }`.

## Running the Tests

```bash
docker compose up -d
./mvnw test
```

## Test Collection

No Bruno/Postman collection is included (optional per the rubric). Exercise the API manually with `curl`, e.g.:

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"username":"jioh","password":"pass123","email":"jioh@example.com"}'
```
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: rewrite README for the Hito4 microservice deliverable"
```

---

### Task 11: End-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Full clean build and test run**

```bash
docker compose up -d
./mvnw clean verify
```
Expected: BUILD SUCCESS, all unit and integration tests green.

- [ ] **Step 2: Boot the app in dev mode and smoke-test every endpoint**

```bash
./mvnw spring-boot:run &
sleep 5
curl -i -X POST http://localhost:8080/api/v1/users -H "Content-Type: application/json" -d '{"username":"jioh","password":"pass123","email":"jioh@example.com"}'
curl -i -X POST http://localhost:8080/api/v1/users/login -H "Content-Type: application/json" -d '{"username":"jioh","password":"pass123"}'
curl -i http://localhost:8080/api/v1/users
curl -i http://localhost:8080/api/v1/users/1
curl -i -X DELETE http://localhost:8080/api/v1/users/1
curl -i http://localhost:8080/api/v1/users/1
```
Expected respectively: `201`, `200` with `UserResponse` JSON, `200` with a list containing the user, `200` with the user, `204`, `404` with an `ErrorResponse` JSON body (no stack trace).

- [ ] **Step 3: Verify Swagger UI is live under `dev`**

Open http://localhost:8080/swagger-ui.html in a browser (or `curl -i http://localhost:8080/api-docs`) and confirm the `Users` tag, all five operations, and their request/response schemas render, and that "Try it out" successfully calls `GET /api/v1/users`.

- [ ] **Step 4: Verify Swagger UI is blocked under `prod`**

```bash
kill %1   # stop the dev-profile run from Step 2
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run &
sleep 5
curl -i http://localhost:8080/swagger-ui.html
curl -i http://localhost:8080/api-docs
kill %1
```
Expected: both return `404`.

- [ ] **Step 5: Confirm the database persists across a container restart**

```bash
docker compose restart db
sleep 3
docker exec -it hito4-postgres-db psql -U postgres -d hito4_db -c 'SELECT id, username, email FROM users;'
```
Expected: rows created during Step 2 that weren't deleted are still present.
