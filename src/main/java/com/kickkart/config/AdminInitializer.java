package com.kickkart.config;

import com.kickkart.entity.User;
import com.kickkart.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Value("${admin.email:admin@kickkart.com}")
    private String adminEmail;

    @Value("${admin.password:07@KicKart}")
    private String adminPassword;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (adminEmail == null || adminEmail.trim().isEmpty() ||
            adminPassword == null || adminPassword.trim().isEmpty()) {
            logger.warn("Admin email or password is missing. Skipping Admin initialization.");
            return;
        }

        String email = adminEmail.trim();
        String password = adminPassword.trim();

        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            User user = existingUserOpt.get();
            boolean roleNeedsUpdate = !"ADMIN".equalsIgnoreCase(user.getRole());
            boolean passwordNeedsUpdate = !passwordEncoder.matches(password, user.getPassword());

            if (roleNeedsUpdate || passwordNeedsUpdate) {
                if (roleNeedsUpdate) {
                    user.setRole("ADMIN");
                }
                if (passwordNeedsUpdate) {
                    user.setPassword(passwordEncoder.encode(password));
                }
                userRepository.save(user);
                logger.info("Existing Admin account updated successfully for email: {}", email);
            } else {
                logger.info("Admin account already exists and is properly configured for email: {}", email);
            }
        } else {
            // Determine username, ensuring uniqueness
            String username = email.contains("@") ? email.split("@")[0] : "admin";
            if (userRepository.existsByUsername(username)) {
                username = username + "_" + System.currentTimeMillis();
            }

            User newAdmin = new User();
            newAdmin.setEmail(email);
            newAdmin.setUsername(username);
            newAdmin.setFullName("Administrator");
            newAdmin.setPassword(passwordEncoder.encode(password));
            newAdmin.setRole("ADMIN");

            userRepository.save(newAdmin);
            logger.info("New Admin account created successfully for email: {}", email);
        }
    }
}
