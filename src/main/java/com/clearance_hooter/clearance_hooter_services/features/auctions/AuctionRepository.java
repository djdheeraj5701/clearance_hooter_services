package com.clearance_hooter.clearance_hooter_services.features.auctions;

import com.clearance_hooter.clearance_hooter_services.dto.AuctionMetadata;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends CrudRepository<AuctionMetadata, Long> {

  @Query("SELECT a FROM AuctionMetadata a WHERE a.startedAt > CURRENT_TIMESTAMP")
  List<AuctionMetadata> findUpcomingAuctions();

  @Query("SELECT a FROM AuctionMetadata a WHERE a.startedAt <= CURRENT_TIMESTAMP AND a.endedAt IS NULL")
  List<AuctionMetadata> findLiveAuctions();

  @Query("SELECT a FROM AuctionMetadata a WHERE a.endedAt < CURRENT_TIMESTAMP")
  List<AuctionMetadata> findCompletedAuctions();

  @NotNull
  Optional<AuctionMetadata> findById(@Param("id") @NotNull Long id);

  default List<AuctionMetadata> getAuctions(String condition) {
    return switch (condition) {
      case "upcoming" -> findUpcomingAuctions();
      case "live" -> findLiveAuctions();
      case "completed" -> findCompletedAuctions();
      default -> List.of();
    };
  }
}
