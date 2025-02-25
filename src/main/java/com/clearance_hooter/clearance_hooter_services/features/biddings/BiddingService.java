package com.clearance_hooter.clearance_hooter_services.features.biddings;

import com.clearance_hooter.clearance_hooter_services.dto.Bidding;
import com.clearance_hooter.clearance_hooter_services.features.users.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
public class BiddingService {
    private final BiddingRepository biddingRepository;
    private final UserService userService;

    public BiddingService(BiddingRepository biddingRepository, UserService userService) {
        this.biddingRepository = biddingRepository;
        this.userService = userService;
    }

    @Transactional
    public Optional<Bidding> placeBid(long itemId, long bidderId, long amount) {
        synchronized (this) {

            Optional<Bidding> currentHighestBid = biddingRepository.findHighestBidByItemId(itemId);
            if (currentHighestBid.isPresent() && currentHighestBid.get().getAmount() >= amount) {
                return Optional.empty();
            }
            if (!userService.updateWalletAmount(bidderId, amount)) {
                return Optional.empty();
            }

            Bidding newBid = Bidding.builder()
                    .itemId(itemId)
                    .bidderId(bidderId)
                    .amount(amount)
                    .timestamp(new Timestamp(System.currentTimeMillis()))
                    .build();

            biddingRepository.save(newBid);

            return Optional.of(newBid);
        }
    }
}
