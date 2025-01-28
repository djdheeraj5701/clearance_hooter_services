package com.clearance_hooter.clearance_hooter_services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Bidding {
    private Long id;
    private Long itemId;
    private Long bidderId;
    private String bidderName;
    private Double amountPlaced;
    private Timestamp timestamp;
}
