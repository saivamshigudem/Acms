package com.acms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE agents SET is_active = false WHERE id=?")
@FilterDef(
    name = "agentActiveFilter",
    parameters = @ParamDef(name = "isActive", type = Boolean.class)
)
@Filter(
    name = "agentActiveFilter",
    condition = "is_active = :isActive"
)
public class Agent extends AuditEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Agent code is required")
    @Size(max = 20, message = "Agent code must be less than 20 characters")
    @Column(unique = true, nullable = false, length = 20)
    private String agentCode;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    @Column(nullable = false, length = 50)
    private String firstName;

    @Size(max = 50, message = "Middle name must be less than 50 characters")
    @Column(length = 50)
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    @Column(nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number should be valid")
    @Column(nullable = false, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentStatus status = AgentStatus.ACTIVE;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Size(max = 200, message = "Address must be less than 200 characters")
    private String address;

    @Size(max = 100, message = "City must be less than 100 characters")
    private String city;

    @Size(max = 50, message = "State must be less than 50 characters")
    private String state;

    @Size(max = 20, message = "Postal code must be less than 20 characters")
    @Column(name = "postal_code")
    private String postalCode;

    @Size(max = 50, message = "Country must be less than 50 characters")
    private String country;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum AgentStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        TERMINATED
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return id != null && Objects.equals(id, agent.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", agentCode='" + agentCode + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }
}
