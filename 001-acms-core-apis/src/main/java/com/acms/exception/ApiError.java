package com.acms.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    private HttpStatus status;
    private String message;
    private String debugMessage;
    private List<ApiSubError> subErrors;
    private String path;

    public ApiError(LocalDateTime timestamp, HttpStatus status, String message, String debugMessage, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.debugMessage = debugMessage;
        this.path = path;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiSubError {
        private String object;
        private String field;
        private Object rejectedValue;
        private String message;
    }
}
