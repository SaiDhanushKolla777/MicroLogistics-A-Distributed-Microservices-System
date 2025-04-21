package com.micrologistics.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {
    public enum Level { INFO, WARNING, ERROR }

    private String type;
    private String message;
    private Level level;
    private LocalDateTime timestamp;
}
