package com.clearance_hooter.clearance_hooter_services.dto;

import com.clearance_hooter.clearance_hooter_services.config.AuctionWSMessageDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.BinaryMessage;

@Getter
@Setter
@Builder
@JsonDeserialize(using = AuctionWSMessageDeserializer.class)
public class AuctionWSMessage {
    private Type type;
    private Object content;

    public enum Type {
        GET_ITEMS,
        GET_BIDDERS,
        START,
        DISPLAY_ITEM,
        PLACE_BID,
        BID_PLACED,
        HAMMER_ONCE,
        HAMMER_TWICE,
        HAMMER_THRICE,
        ITEM_UNSOLD,
        ITEM_SOLD,
        AUCTION_END
    }

    public BinaryMessage toBinaryMessage() throws Exception {
        return new BinaryMessage(new ObjectMapper().writeValueAsBytes(this));
    }
}
