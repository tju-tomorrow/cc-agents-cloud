package io.ccagents.cloud.auth;

import io.ccagents.cloud.account.IdentityEntity;
import io.ccagents.cloud.account.IdentityRepository;
import io.ccagents.cloud.account.UserEntity;
import io.ccagents.cloud.account.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository users;
    private final IdentityRepository identities;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Autowired
    public RegistrationService(
            UserRepository users,
            IdentityRepository identities,
            PasswordEncoder passwordEncoder) {
        this(users, identities, passwordEncoder, Clock.systemUTC());
    }

    RegistrationService(
            UserRepository users,
            IdentityRepository identities,
            PasswordEncoder passwordEncoder,
            Clock clock) {
        this.users = users;
        this.identities = identities;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().strip().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        Instant now = clock.instant();
        UserEntity user;
        try {
            user = users.saveAndFlush(new UserEntity(
                    UUID.randomUUID(),
                    email,
                    request.displayName().strip(),
                    now));

            identities.saveAndFlush(IdentityEntity.password(
                    UUID.randomUUID(),
                    user,
                    email,
                    passwordEncoder.encode(request.password()),
                    now));
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyRegisteredException();
        }

        return new RegisterResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
