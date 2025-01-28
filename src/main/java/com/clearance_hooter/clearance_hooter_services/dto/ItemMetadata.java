package com.clearance_hooter.clearance_hooter_services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ItemMetadata {
    private Long id;
    private String name;
    private String description;
}
