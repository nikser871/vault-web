package vaultWeb.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vaultWeb.models.RefreshToken;

import java.util.List;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    @Modifying
    @Transactional
    @Query("""
    update RefreshToken rt
    set rt.revoked = true
    where rt.user.id = :userId
  """)
    void revokeAllByUser(@Param("userId") Long userId);

    @Query("""
        select rt from RefreshToken rt
        where rt.revoked = false
          and rt.expiresAt > CURRENT_TIMESTAMP
    """)
    List<RefreshToken> findAllValidTokens();

}
