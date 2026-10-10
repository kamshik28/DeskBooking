package com.kaminskyi.deskbooking.config;

import com.kaminskyi.deskbooking.entity.Role;
import com.kaminskyi.deskbooking.entity.User;
import com.kaminskyi.deskbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties props;

    @Override
    public void run(ApplicationArguments args) {
        String email = props.admin().email();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        User admin = new User();
        admin.setEmail(email);
        admin.setFullName("Administrator");
        admin.setPasswordHash(passwordEncoder.encode(props.admin().password()));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Created default admin account: {}", email);
    }
}
