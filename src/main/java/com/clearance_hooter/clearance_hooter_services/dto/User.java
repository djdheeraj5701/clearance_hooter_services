package com.clearance_hooter.clearance_hooter_services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private Double walletAmount;
}
