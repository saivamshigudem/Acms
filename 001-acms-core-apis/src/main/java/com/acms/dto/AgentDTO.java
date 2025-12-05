package com.acms.dto;

import com.acms.model.Agent.AgentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Agent data transfer object")
public class AgentDTO {

    @Schema(description = "Unique identifier of the agent", example = "1")
    private Long id;

    @NotBlank(message = "Agent code is required")
    @Size(max = 20, message = "Agent code must be less than 20 characters")
    @Schema(description = "Unique agent code", example = "AG001", required = true)
    private String agentCode;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    @Schema(description = "Agent's first name", example = "John", required = true)
    private String firstName;

    @Size(max = 50, message = "Middle name must be less than 50 characters")
    @Schema(description = "Agent's middle name", example = "William", required = false)
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    @Schema(description = "Agent's last name", example = "Doe", required = true)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Schema(description = "Agent's email address", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number should be valid")
    @Schema(description = "Agent's phone number", example = "+1234567890", required = true)
    private String phone;

    @Schema(description = "Agent's status", example = "ACTIVE", required = true)
    private AgentStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Agent's date of birth (YYYY-MM-DD)", example = "1985-05-15", type = "string", format = "date")
    private LocalDate dateOfBirth;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Agent's hire date (YYYY-MM-DD)", example = "2020-01-15", type = "string", format = "date")
    private LocalDate hireDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Agent's termination date (YYYY-MM-DD)", example = "2023-12-31", type = "string", format = "date")
    private LocalDate terminationDate;

    @Size(max = 200, message = "Address must be less than 200 characters")
    @Schema(description = "Agent's address", example = "123 Main St", required = false)
    private String address;

    @Size(max = 100, message = "City must be less than 100 characters")
    @Schema(description = "Agent's city", example = "New York", required = false)
    private String city;

    @Size(max = 50, message = "State must be less than 50 characters")
    @Schema(description = "Agent's state/province", example = "NY", required = false)
    private String state;

    @Size(max = 20, message = "Postal code must be less than 20 characters")
    @Schema(description = "Agent's postal/zip code", example = "10001", required = false)
    private String postalCode;

    @Size(max = 50, message = "Country must be less than 50 characters")
    @Schema(description = "Agent's country", example = "USA", required = false)
    private String country;

    @Schema(description = "Additional notes about the agent", example = "Top performing agent for Q1 2023", required = false)
    private String notes;

    @Schema(description = "Indicates if the agent is active", example = "true", required = true)
    private boolean isActive;

    @Schema(description = "Version for optimistic locking", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long version;
}
