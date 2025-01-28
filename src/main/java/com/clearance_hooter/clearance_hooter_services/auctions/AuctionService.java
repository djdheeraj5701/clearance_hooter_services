package com.clearance_hooter.clearance_hooter_services.auctions;

import org.springframework.stereotype.Service;

import com.clearance_hooter.clearance_hooter_services.dto.Auction;

import java.util.List;

@Service
public class AuctionService {
  private final AuctionRepository auctionRepository;
  
  public AuctionService(AuctionRepository auctionRepository) {
    this.auctionRepository = auctionRepository;
  }
  
  public List<Auction> getUpcomingAuctions() {
    return auctionRepository.getAuctions("upcoming");
  }
  
  public List<Auction> getCompletedAuctions() {
    return auctionRepository.getAuctions("completed");
  }
  
  public List<Auction> getLiveAuctions() {
    return auctionRepository.getAuctions("live");
  }
}

