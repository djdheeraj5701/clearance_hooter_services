package com.clearance_hooter.clearance_hooter_services.auctions;

import org.springframework.stereotype.Repository;

import com.clearance_hooter.clearance_hooter_services.dto.Auction;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AuctionRepository {
  
  public List<Auction> getAuctions(String condition) {
    return new ArrayList<>();
  }
}

