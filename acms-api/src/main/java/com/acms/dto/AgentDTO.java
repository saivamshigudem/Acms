package com.acms.dto;

import com.acms.models.Agent.AgentStatus;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AgentDTO {
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    private String email;
    
    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phoneNumber;
    
    @NotNull(message = "Commission tier is required")
    @DecimalMin(value = "0.0", message = "Commission tier must be greater than or equal to 0")
    @DecimalMax(value = "100.0", message = "Commission tier must be less than or equal to 100")
    private BigDecimal commissionTier;
    
    private AgentStatus status = AgentStatus.ACTIVE;
    private LocalDateTime hireDate;
    private LocalDateTime terminationDate;
    private String address;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
