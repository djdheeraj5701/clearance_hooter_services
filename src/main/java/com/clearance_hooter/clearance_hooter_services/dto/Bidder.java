package com.clearance_hooter.clearance_hooter_services.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bidder { // Bidder is different from User because this will contain a portion of User's data
    private Long id;
    private String name;
    private Long walletAmount;
}
