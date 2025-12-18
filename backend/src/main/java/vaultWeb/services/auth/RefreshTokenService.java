package vaultWeb.services.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vaultWeb.models.RefreshToken;
import vaultWeb.models.User;
import vaultWeb.repositories.RefreshTokenRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public void create(User user, HttpServletResponse response) {

        // 1. Revoke old tokens
        refreshTokenRepository.revokeAllByUser(user.getId());

        // 2. Generate token
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String rawToken =
                Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // 3. Hash token
        String hash = passwordEncoder.encode(rawToken);

        // 4. Store in DB
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash);
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        // 5. Send cookie
        ResponseCookie cookie =
                ResponseCookie.from("refresh_token", rawToken)
                        .httpOnly(true)
                        .secure(true)
                        .sameSite("None")
                        .path("/api/auth/refresh")
                        .maxAge(Duration.ofDays(7))
                        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
