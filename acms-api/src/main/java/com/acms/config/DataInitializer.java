package com.acms.config;

import com.acms.models.Agent;
import com.acms.models.Role;
import com.acms.models.enums.RoleEnum;
import com.acms.repositories.AgentRepository;
import com.acms.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            AgentRepository agentRepository) {
        return args -> {
            // Create roles if they don't exist
            if (roleRepository.count() == 0) {
                Role userRole = new Role(RoleEnum.ROLE_USER);
                Role managerRole = new Role(RoleEnum.ROLE_MANAGER);
                Role adminRole = new Role(RoleEnum.ROLE_ADMIN);
                
                roleRepository.saveAll(Arrays.asList(userRole, managerRole, adminRole));
            }
            
            // Create test agents if none exist
            if (agentRepository.count() == 0) {
                Agent agent1 = new Agent();
                agent1.setName("John Doe");
                agent1.setEmail("john.doe@example.com");
                agent1.setPhoneNumber("+1234567890");
                agent1.setCommissionTier(new BigDecimal("5.50"));
                agent1.setHireDate(LocalDateTime.now().minusMonths(6));
                agent1.setAddress("123 Main St");
                agent1.setCity("New York");
                agent1.setState("NY");
                agent1.setPostalCode("10001");
                agent1.setCountry("USA");
                
                Agent agent2 = new Agent();
                agent2.setName("Jane Smith");
                agent2.setEmail("jane.smith@example.com");
                agent2.setPhoneNumber("+1987654321");
                agent2.setCommissionTier(new BigDecimal("7.25"));
                agent2.setHireDate(LocalDateTime.now().minusMonths(3));
                agent2.setAddress("456 Oak Ave");
                agent2.setCity("Los Angeles");
                agent2.setState("CA");
                agent2.setPostalCode("90001");
                agent2.setCountry("USA");
                
                agentRepository.saveAll(Arrays.asList(agent1, agent2));
            }
        };
    }
}
