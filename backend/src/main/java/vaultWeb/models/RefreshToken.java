package vaultWeb.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class RefreshToken {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;
    private String tokenHash;
    private Instant expiresAt;
    private boolean revoked;
}
