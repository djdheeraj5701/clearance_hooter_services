package com.clearance_hooter.clearance_hooter_services.features.biddings;

import com.clearance_hooter.clearance_hooter_services.dto.Bidding;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BiddingRepository extends CrudRepository<Bidding, Long> {

    @Query("SELECT b FROM Bidding b WHERE b.itemId = :itemId ORDER BY b.amount DESC LIMIT 1")
    Optional<Bidding> findHighestBidByItemId(@Param("itemId") long itemId);
}