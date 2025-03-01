package com.clearance_hooter.clearance_hooter_services.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private Long walletAmount;
}
