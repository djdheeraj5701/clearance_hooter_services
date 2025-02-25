package com.clearance_hooter.clearance_hooter_services.features.items;

import com.clearance_hooter.clearance_hooter_services.dto.Item;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Long> getItemIds(long auctionId) {
        return getItems(auctionId)
                .stream()
                .map(Item::getId)
                .toList();
    }

    //TODO: cache it. TTL=2min
    public List<Item> getItems(long auctionId) {
        return itemRepository.getItemsByAuctionId(auctionId);
    }
}
