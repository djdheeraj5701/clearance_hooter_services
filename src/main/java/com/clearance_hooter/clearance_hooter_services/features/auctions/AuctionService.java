package com.clearance_hooter.clearance_hooter_services.features.auctions;

import com.clearance_hooter.clearance_hooter_services.dto.Auction;
import com.clearance_hooter.clearance_hooter_services.dto.AuctionMetadata;
import com.clearance_hooter.clearance_hooter_services.dto.Item;
import com.clearance_hooter.clearance_hooter_services.exceptions.AuctionJoiningException;
import com.clearance_hooter.clearance_hooter_services.exceptions.AuctionNotFoundException;
import com.clearance_hooter.clearance_hooter_services.features.items.ItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {
  private final AuctionRepository auctionRepository;
  private final ItemRepository itemRepository;

  public AuctionService(AuctionRepository auctionRepository, ItemRepository itemRepository) {
    this.auctionRepository = auctionRepository;
    this.itemRepository = itemRepository;
  }

  public List<AuctionMetadata> getAuctions(String type) {
    return auctionRepository.getAuctions(type);
  }

  public void validateAuctionEntry(Long auctionId) {
    Timestamp now = new Timestamp(System.currentTimeMillis());

    AuctionMetadata auctionMetadata = auctionRepository.findById(auctionId)
            .orElseThrow(() -> new AuctionNotFoundException("Auction not found"));

    if (auctionMetadata.getStartedAt().after(now)) {
      throw new AuctionJoiningException("Auction has not started yet");
    }
    if (new Timestamp(auctionMetadata.getStartedAt().getTime() + 61 * 1000).before(now)) {
      throw new AuctionJoiningException("Auction joining period has ended");
    }
  }

  public Auction getAuction(Long id) {
    Optional<AuctionMetadata> auctionMetadata = auctionRepository.findById(id);
    if (auctionMetadata.isEmpty()) {
      throw new AuctionNotFoundException("No auction found");
    }
    return Auction.builder()
            .id(id)
            .name(auctionMetadata.get().getName())
            .startedAt(auctionMetadata.get().getStartedAt())
            .endedAt(auctionMetadata.get().getEndedAt())
            .items(itemRepository.getItemsByAuctionId(id))
            .build();
  }

  @Transactional
  public AuctionMetadata createAuction(Auction auction) {
    List<Item> items = auction.getItems();
    itemRepository.saveAll(items);

    AuctionMetadata auctionMetadata = AuctionMetadata.builder()
            .name(auction.getName())
            .startedAt(auction.getStartedAt())
            .build();
    return auctionRepository.save(auctionMetadata);
  }
}
