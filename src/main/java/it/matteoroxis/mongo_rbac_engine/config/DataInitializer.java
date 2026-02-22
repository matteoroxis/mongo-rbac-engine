package it.matteoroxis.mongo_rbac_engine.config;

import it.matteoroxis.mongo_rbac_engine.document.UserDocument;
import it.matteoroxis.mongo_rbac_engine.domain.UserRole;
import it.matteoroxis.mongo_rbac_engine.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.findById("admin").isEmpty()) {
                UserDocument admin = new UserDocument();
                admin.setId("admin");
                admin.setRoles(Set.of(UserRole.ADMIN.name()));
                admin.setEmail("admin@admin.com");
                admin.setStatus("ACTIVE");
                userRepository.save(admin);
                log.info("✅ User 'admin' created with id: {}", admin.getId());
            } else {
                log.info("ℹ️  User 'admin' already present, skip.");
            }
        };
    }
}
