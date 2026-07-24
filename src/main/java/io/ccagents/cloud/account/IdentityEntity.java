package io.ccagents.cloud.account;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "identities")
public class IdentityEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdentityProvider provider;

    @Column(name = "provider_subject", nullable = false, length = 320)
    private String providerSubject;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdentityEntity() {
    }

    public static IdentityEntity password(
            UUID id,
            UserEntity user,
            String normalizedEmail,
            String passwordHash,
            Instant now) {
        IdentityEntity identity = new IdentityEntity();
        identity.id = id;
        identity.user = user;
        identity.provider = IdentityProvider.PASSWORD;
        identity.providerSubject = normalizedEmail;
        identity.passwordHash = passwordHash;
        identity.createdAt = now;
        return identity;
    }
}

