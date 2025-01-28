package com.clearance_hooter.clearance_hooter_services.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Auction {
    private Long id;
    private String name;
    private LocalDateTime started_at;
    private LocalDateTime ended_at;
    private List<ItemMetadata> items;
}
