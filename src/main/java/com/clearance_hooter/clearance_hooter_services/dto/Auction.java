package com.clearance_hooter.clearance_hooter_services.dto;

import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Auction {
    private Long id;
    private String name;
    private Timestamp startedAt;
    private Timestamp endedAt;
    private List<Item> items;
}
