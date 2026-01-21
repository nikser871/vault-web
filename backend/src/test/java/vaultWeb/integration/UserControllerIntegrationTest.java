package vaultWeb.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import vaultWeb.dtos.user.UserDto;
import vaultWeb.models.User;
import vaultWeb.repositories.UserRepository;
import vaultWeb.security.JwtUtil;

class UserControllerIntegrationTest extends IntegrationTestBase {

  // ============================================================================
  // Test Utility Methods
  // ============================================================================
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserRepository userRepository;
  @Autowired private JwtUtil jwtUtil;

  private UserDto createUserDto(String username, String password) {
    UserDto dto = new UserDto();
    dto.setUsername(username);
    dto.setPassword(password);
    return dto;
  }

  private Cookie extractCookie(MvcResult result, String cookieName) {
    return result.getResponse().getCookie(cookieName);
  }

  private String extractTokenFromResponse(MvcResult result) throws Exception {
    String json = result.getResponse().getContentAsString();
    JsonNode node = objectMapper.readTree(json);
    return node.get("token").asText();
  }

  private String authHeader(String token) {
    return "Bearer " + token;
  }

  // ============================================================================
  // Stage 1: Foundation Setup
  // ============================================================================

  @Test
  void shouldLoadSpringContext() {
    assertNotNull(mockMvc);
    assertNotNull(userRepository);
  }

  // ============================================================================
  // Stage 2: Basic Authentication Flow (3 tests)
  // ============================================================================

  @Test
  void shouldRegisterNewUser() throws Exception {
    UserDto testUser = createUserDto("testuser", "TestPassword1");

    // Perform registration request and verify response
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
        .andExpect(status().isOk())
        .andExpect(content().string("User registered successfully"));

    // Verify user is saved in database
    assertTrue(userRepository.findByUsername(testUser.getUsername()).isPresent());

    // Verify password is properly BCrypt hashed
    User savedUser = userRepository.findByUsername(testUser.getUsername()).get();
    assertTrue(savedUser.getPassword().startsWith("$2a$"));
  }

  @Test
  void shouldFailRegistration_WhenDuplicateUsername() throws Exception {
    UserDto testUser = createUserDto("testuser", "TestPassword1");
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
        .andExpect(status().isOk())
        .andExpect(content().string("User registered successfully"));
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Registration error: Username 'testuser' is already taken"));

    assertTrue(userRepository.findByUsername(testUser.getUsername()).isPresent());
  }

  @Test
  void shouldLogin_WithValidCredentials() throws Exception {
    // Register a user first
    UserDto testUser = createUserDto("testuser", "TestPassword1");
    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
        .andExpect(status().isOk())
        .andExpect(content().string("User registered successfully"));

    // Login with the registered user and capture result
    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testUser)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andReturn();

    // Verify refresh_token cookie is set
    Cookie refreshTokenCookie = extractCookie(result, "refresh_token");
    assertNotNull(refreshTokenCookie, "refresh_token cookie should be set");
    assertNotNull(refreshTokenCookie.getValue(), "refresh_token should have a value");
    assertTrue(refreshTokenCookie.isHttpOnly(), "refresh_token should be HttpOnly");
    assertTrue(refreshTokenCookie.getSecure(), "refresh_token should be Secure");
    assertEquals(
        "/api/auth/refresh",
        refreshTokenCookie.getPath(),
        "refresh_token path should be /api/auth/refresh");
    assertEquals(
        30 * 24 * 60 * 60,
        refreshTokenCookie.getMaxAge(),
        "refresh_token should expire in 30 days");
  }

  // ============================================================================
  // Stage 3: JWT Token Integration (4 tests)
  // ============================================================================

  @Test
  void shouldGenerateValidJwtToken_OnLogin() throws Exception {
    // TODO: Register and login
    // TODO: Extract access token from response
    // TODO: Parse token using jwtUtil
    // TODO: Verify token contains username claim
    // TODO: Verify token is not expired
  }

  @Test
  void shouldAccessProtectedEndpoint_WithValidToken() throws Exception {
    // TODO: Register and login to get token
    // TODO: Call GET /api/auth/users with Authorization: Bearer {token}
    // TODO: Verify 200 OK response
    // TODO: Verify response contains user list
  }

  @Test
  void shouldReject_WithInvalidToken() throws Exception {
    // TODO: Call GET /api/auth/users with invalid/malformed token
    // TODO: Verify 401 Unauthorized response
  }

  @Test
  void shouldReject_WithMissingToken() throws Exception {
    // TODO: Call GET /api/auth/users without Authorization header
    // TODO: Verify 401 Unauthorized response
  }
}
