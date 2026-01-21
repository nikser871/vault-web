package vaultWeb.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * MockMvc tests for UserController that test HTTP responses including status codes.
 *
 * <p>This is different from UserControllerTest which tests controller logic in isolation. These
 * tests verify the full HTTP layer including:
 *
 * <ul>
 *   <li>HTTP status codes (200, 400, 401, 409, etc.)
 *   <li>Response body format
 *   <li>Exception handling via GlobalExceptionHandler
 *   <li>Request/response serialization
 * </ul>
 */
@WebMvcTest(UserController.class)
class UserControllerMockMvcTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;



  // ============================================================================
  // Login Tests - Testing Wrong Credentials
  // ============================================================================

  @Test
  @WithMockUser
  void shouldReturn401_WhenSignInWithWrongPassword() throws Exception {
    // TODO: Implement test for wrong password
    // Expected: HTTP 401 Unauthorized
  }

  @Test
  @WithMockUser
  void shouldReturn401_WhenSignInWithNonExistentUser() throws Exception {
    // TODO: Implement test for non-existent user
    // Expected: HTTP 401 Unauthorized
  }

  // ============================================================================
  // Registration Tests
  // ============================================================================

  // ============================================================================
  // Refresh Token Tests
  // ============================================================================

  @Test
  @WithMockUser
  void shouldReturn401_WhenRefreshTokenInvalid() throws Exception {
    // TODO: Implement test for invalid refresh token
    // Expected: HTTP 401 Unauthorized
  }
}
