package com.acms.controller;

import com.acms.dto.AgentDTO;
import com.acms.dto.ApiResponse;
import com.acms.model.Agent;
import com.acms.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
@Tag(name = "Agent Management", description = "APIs for managing insurance agents")
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    @Operation(summary = "Get all agents", description = "Retrieve a paginated list of all agents")
    public ResponseEntity<ApiResponse<Page<AgentDTO>>> getAllAgents(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<AgentDTO> agents = agentService.getAllAgents(pageable);
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get agent by ID", description = "Retrieve an agent by their unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Agent found",
                    content = @Content(schema = @Schema(implementation = AgentDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Agent not found")
    })
    public ResponseEntity<ApiResponse<AgentDTO>> getAgentById(
            @Parameter(description = "ID of the agent to be retrieved") @PathVariable Long id) {
        AgentDTO agent = agentService.getAgentById(id);
        return ResponseEntity.ok(ApiResponse.success(agent));
    }

    @GetMapping("/code/{agentCode}")
    @Operation(summary = "Get agent by code", description = "Retrieve an agent by their unique agent code")
    public ResponseEntity<ApiResponse<AgentDTO>> getAgentByCode(
            @Parameter(description = "Agent code") @PathVariable String agentCode) {
        AgentDTO agent = agentService.getAgentByCode(agentCode);
        return ResponseEntity.ok(ApiResponse.success(agent));
    }

    @PostMapping
    @Operation(summary = "Create a new agent", description = "Register a new insurance agent")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Agent created successfully",
                    content = @Content(schema = @Schema(implementation = AgentDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input")
    })
    public ResponseEntity<ApiResponse<AgentDTO>> createAgent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Agent details to be created",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AgentDTO.class)))
            @Valid @RequestBody AgentDTO agentDTO) {
        AgentDTO createdAgent = agentService.createAgent(agentDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/agents/" + createdAgent.getId()))
                .body(ApiResponse.withStatus(HttpStatus.CREATED, "Agent created successfully", createdAgent));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an agent", description = "Update an existing agent's information")
    public ResponseEntity<ApiResponse<AgentDTO>> updateAgent(
            @Parameter(description = "ID of the agent to be updated") @PathVariable Long id,
            @Valid @RequestBody AgentDTO agentDTO) {
        AgentDTO updatedAgent = agentService.updateAgent(id, agentDTO);
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Agent updated successfully", updatedAgent));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an agent", description = "Soft delete an agent by their ID")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAgent(
            @Parameter(description = "ID of the agent to be deleted") @PathVariable Long id) {
        agentService.deleteAgent(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update agent status", description = "Update the status of an existing agent")
    public ResponseEntity<ApiResponse<AgentDTO>> updateAgentStatus(
            @Parameter(description = "ID of the agent") @PathVariable Long id,
            @RequestParam Agent.AgentStatus status) {
        AgentDTO updatedAgent = agentService.updateAgentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.withStatus(HttpStatus.OK, "Agent status updated successfully", updatedAgent));
    }

    @GetMapping("/search")
    @Operation(summary = "Search agents", description = "Search agents by name, email, or phone")
    public ResponseEntity<ApiResponse<Page<AgentDTO>>> searchAgents(
            @Parameter(description = "Search query") @RequestParam String query,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<AgentDTO> agents = agentService.searchAgents(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(agents));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get agents by status", description = "Retrieve agents filtered by status")
    public ResponseEntity<ApiResponse<Page<AgentDTO>>> getAgentsByStatus(
            @Parameter(description = "Agent status") @PathVariable Agent.AgentStatus status,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        Page<AgentDTO> agents = agentService.getAgentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(agents));
    }
}
