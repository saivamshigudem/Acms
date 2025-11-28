package com.acms.config;

import com.acms.dto.AgentDTO;
import com.acms.models.Agent;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {
    
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
        
        modelMapper.typeMap(Agent.class, AgentDTO.class).addMappings(mapper -> {
            mapper.map(Agent::getId, AgentDTO::setId);
        });
        
        return modelMapper;
    }
}
