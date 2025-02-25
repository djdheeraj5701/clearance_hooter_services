package com.clearance_hooter.clearance_hooter_services.features.auctions;

import com.clearance_hooter.clearance_hooter_services.dto.Auction;
import com.clearance_hooter.clearance_hooter_services.dto.AuctionMetadata;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

  private final AuctionService auctionService;

  public AuctionController(AuctionService auctionService) {
    this.auctionService = auctionService;
  }

  @PostMapping
  public AuctionMetadata createAuctions(@RequestBody Auction auction) {
    return auctionService.createAuction(auction);
  }

  @GetMapping
  public List<AuctionMetadata> getAuctions(@RequestParam("type") String type) {
    return auctionService.getAuctions(type);
  }

  @GetMapping("/{id}")
  public Auction getAuction(@PathVariable Long id) {
    return auctionService.getAuction(id);
  }

  @GetMapping("/{id}/join")
  public String getAuctionWSUrl(@PathVariable Long id) {
    auctionService.validateAuctionEntry(id);
    return "ws://localhost:8080/ws-auctions?id=" + id;
  }
}