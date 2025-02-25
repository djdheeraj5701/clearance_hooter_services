package com.clearance_hooter.clearance_hooter_services.features.items;

import com.clearance_hooter.clearance_hooter_services.dto.Item;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends CrudRepository<Item, Long>{

    List<Item> getItemsByAuctionId(long auctionId);
}
