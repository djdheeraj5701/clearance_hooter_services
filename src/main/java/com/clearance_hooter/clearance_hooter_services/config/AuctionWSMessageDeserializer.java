package com.clearance_hooter.clearance_hooter_services.config;

import com.clearance_hooter.clearance_hooter_services.dto.AuctionWSMessage;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class AuctionWSMessageDeserializer extends JsonDeserializer<AuctionWSMessage> {

    @Override
    public AuctionWSMessage deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);

        AuctionWSMessage.Type type = AuctionWSMessage.Type.valueOf(node.path("type").asText());
        Object content = null;

        if (type == AuctionWSMessage.Type.PLACE_BID) {
            content = node.get("content").asLong();
        }
        // handle other types if necessary

        return AuctionWSMessage.builder()
                .type(type)
                .content(content)
                .build();
    }
}