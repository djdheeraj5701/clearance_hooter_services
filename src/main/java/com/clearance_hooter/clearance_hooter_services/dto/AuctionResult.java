package com.clearance_hooter.clearance_hooter_services.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionResult {
    private Auction auction;
    private List<Bidding> winBiddings;
    private List<Long> unsoldItemIds;
    private Double totalAmountReceived;
}