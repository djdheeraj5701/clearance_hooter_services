package com.clearance_hooter.clearance_hooter_services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuctionResult {
    private Auction auction;
    private List<Bidding> winBiddings;
    private List<Long> unsoldItemIds;
    private Double totalAmountReceived;
}
