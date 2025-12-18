package vaultWeb.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vaultWeb.dtos.user.UserDto;
import vaultWeb.dtos.user.UserResponseDto;
import vaultWeb.models.User;
import vaultWeb.services.UserService;
import vaultWeb.services.auth.AuthService;
import vaultWeb.services.auth.LoginResult;
import vaultWeb.services.auth.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "User Controller", description = "Handles registration and login of users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final AuthService authService;
  private final RefreshTokenService refreshTokenService;

  @PostMapping("/register")
  @Operation(
      summary = "Register a new user",
      description =
          """
                    Accepts a JSON object containing username and plaintext password.
                    The password is hashed using BCrypt (via Spring Security's PasswordEncoder) before being persisted.
                    The new user is assigned the default role 'User'.""")
  public ResponseEntity<String> register(@Valid @RequestBody UserDto user) {
    userService.registerUser(new User(user));
    return ResponseEntity.ok("User registered successfully");
  }

  @PostMapping("/login")
  @Operation(
      summary = "Authenticate user and return JWT token",
      description =
          """
                    Accepts a username and plaintext password.
                    If credentials are valid, a JWT (JSON Web Token) is returned in the response body.
                    The token includes the username and user role as claims and is signed using HS256 (HMAC with SHA-256).
                    Token validity is 1 hour.

                    Security process:
                    - Uses Spring Security's AuthenticationManager to validate credentials.
                    - On success, the user details are fetched and a JWT is generated via JwtUtil.
                    - The token can be used in the 'Authorization' header for protected endpoints.
                    """)
  public ResponseEntity<?> login(@Valid @RequestBody UserDto user, HttpServletResponse response) {
    LoginResult res = authService.login(user.getUsername(), user.getPassword());
    refreshTokenService.create(res.user(),response);
    return ResponseEntity.ok(Map.of("token", res.accessToken()));
  }


  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(
          @CookieValue(name = "refresh_token", required = false) String refreshToken,
          HttpServletResponse response) {
    if (refreshToken == null) {
      return ResponseEntity.status(401).build();
    }

    return authService.refresh(refreshToken, response);

  }
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {

    ResponseCookie deleteCookie = ResponseCookie
            .from("refresh_token", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/api/auth/refresh")
            .maxAge(0) // 👈 deletes cookie
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

    return ResponseEntity.ok().build();
  }

  @GetMapping("/check-username")
  @Operation(
      summary = "Check if username already exists",
      description = "Returns true if the username is already taken, false otherwise.")
  public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
    boolean exists = userService.usernameExists(username);
    return ResponseEntity.ok(Map.of("exists", exists));
  }

  @GetMapping("/users")
  @Operation(
      summary = "Get all users",
      description =
          "Returns a list of all users with basic info (e.g., usernames) for displaying in the chat list.")
  public ResponseEntity<List<UserResponseDto>> getAllUsers() {
    List<UserResponseDto> users =
        userService.getAllUsers().stream().map(UserResponseDto::new).toList();
    return ResponseEntity.ok(users);
  }
}
