package com.clearance_hooter.clearance_hooter_services.auctions;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clearance_hooter.clearance_hooter_services.dto.Auction;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionController {
  
  private final AuctionService auctionService;
  
  public AuctionController(AuctionService auctionService) {
    this.auctionService = auctionService;
  }
  
  @GetMapping("/upcoming")
  public List<Auction> getUpcomingAuctions() {
    return auctionService.getUpcomingAuctions();
  }
  
  @GetMapping("/completed")
  public List<Auction> getCompletedAuctions() {
    return auctionService.getCompletedAuctions();
  }
  
  @GetMapping("/live")
  public List<Auction> getLiveAuctions() {
    return auctionService.getLiveAuctions();
  }
}
