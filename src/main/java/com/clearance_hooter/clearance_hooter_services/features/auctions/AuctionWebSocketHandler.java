package com.clearance_hooter.clearance_hooter_services.features.auctions;

import com.clearance_hooter.clearance_hooter_services.dto.AuctionWSMessage;
import com.clearance_hooter.clearance_hooter_services.dto.Bidder;
import com.clearance_hooter.clearance_hooter_services.dto.Bidding;
import com.clearance_hooter.clearance_hooter_services.dto.User;
import com.clearance_hooter.clearance_hooter_services.features.biddings.BiddingService;
import com.clearance_hooter.clearance_hooter_services.features.items.ItemService;
import com.clearance_hooter.clearance_hooter_services.features.users.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Configuration
public class AuctionWebSocketHandler implements WebSocketHandler {

    // To avoid error when some other process updates the map
    private final Map<Long, List<WebSocketSession>> auctionIdToSessionsMap = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> auctionIdToItemIdsMap = new ConcurrentHashMap<>();
    private final Map<Long, Timer> timers = new ConcurrentHashMap<>();
    private final Map<Long, Lock> locks = new ConcurrentHashMap<>();

    private final AuctionService auctionService;
    private final ItemService itemService;
    private final BiddingService biddingService;
    private final UserService userService;

    public AuctionWebSocketHandler(AuctionService auctionService,
                                   ItemService itemService,
                                   BiddingService biddingService,
                                   UserService userService) {
        this.auctionService = auctionService;
        this.itemService = itemService;
        this.biddingService = biddingService;
        this.userService = userService;
    }

    // this is executed after 01:01. No one can register thereafter.
    @SneakyThrows
    public void startAuctionScheduler() {
        Set<Map.Entry<Long, List<WebSocketSession>>> entries = auctionIdToSessionsMap.entrySet();
        for (Map.Entry<Long, List<WebSocketSession>> entry : entries) {
            long auctionId = entry.getKey();
            List<WebSocketSession> sessions = entry.getValue();

            if (auctionIdToItemIdsMap.containsKey(auctionId)) continue;

            auctionIdToItemIdsMap.put(auctionId, itemService.getItemIds(auctionId));
            locks.put(auctionId, new ReentrantLock());

            List<Bidder> bidders = getBidders(sessions);
            broadcastBiddersAndStart(sessions, bidders);
        }
    }

    @SneakyThrows
    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        long auctionId = getAuctionId(session);
        session.getAttributes().put("auctionId", auctionId);

        long userId = getUserId(session);
        session.getAttributes().put("userId", userId);

        auctionService.validateAuctionEntry(auctionId);
        modifyATSMap(session, auctionId, "add");

        sendItems(session, auctionId);
    }

    @Override
    public void handleMessage(WebSocketSession userSession, WebSocketMessage<?> message) {
        try {
            AuctionWSMessage userMessage = new ObjectMapper().readValue(message.getPayload().toString(), AuctionWSMessage.class);

            long auctionId = (long) userSession.getAttributes().get("auctionId");
            long userId = (long) userSession.getAttributes().get("userId");
            List<WebSocketSession> sessions = auctionIdToSessionsMap.get(auctionId);

            if (userMessage.getType().equals(AuctionWSMessage.Type.PLACE_BID)) {
                if (!locks.get(auctionId).tryLock()) return;
                Optional<Bidding> bidding = placeAndGetBidding(auctionId, userMessage, userId);
                if (bidding.isPresent()) {
                    sendBidPlacedMessage(bidding.get(), sessions);
                    startTimer(auctionId, 1);
                }
                locks.get(auctionId).unlock();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void startTimer(long auctionId, int hammerCount) {
        Timer existingTimer = timers.getOrDefault(auctionId, null);
        if (existingTimer != null) {
            existingTimer.cancel();
            existingTimer.purge();
        }

        Timer newTimer = new Timer();
        newTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                AuctionWSMessage.Type messageType;
                switch (hammerCount) {
                    case 0:
                        sendItemUnsoldMessage(auctionId);
                        sendNewItemOnDisplayMessage(auctionId);
                        return;
                    case 1:
                        messageType = AuctionWSMessage.Type.HAMMER_ONCE;
                        break;
                    case 2:
                        messageType = AuctionWSMessage.Type.HAMMER_TWICE;
                        break;
                    default:
                        if (locks.get(auctionId).tryLock()) {
                            try {
                                messageType = AuctionWSMessage.Type.HAMMER_THRICE;
                                sendHammerDownMessage(auctionIdToSessionsMap.get(auctionId), messageType);
                                sendItemSoldMessage(auctionId);
                            } finally {
                                sendNewItemOnDisplayMessage(auctionId);
                                locks.get(auctionId).unlock();
                            }
                        }
                        return;
                }

                sendHammerDownMessage(auctionIdToSessionsMap.get(auctionId), messageType);
                startTimer(auctionId, hammerCount + 1);
            }
        }, hammerCount == 0 ? 15000 : 5000); // Schedule the task to run after 15 seconds when item displayed, otherwise 5 seconds

        // Store the new timer in a map
        timers.put(auctionId, newTimer);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, CloseStatus closeStatus) {
        long auctionId = (long) session.getAttributes().get("auctionId");
        modifyATSMap(session, auctionId, "remove");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private Optional<Bidding> placeAndGetBidding(long auctionId, AuctionWSMessage userMessage, long userId) {
        long itemId = auctionIdToItemIdsMap.get(auctionId).get(0);
        long amount = (long) userMessage.getContent();

        return biddingService.placeBid(itemId, userId, amount);
    }

    private void sendItemSoldMessage(long auctionId) {
        try {
            BinaryMessage binaryMessage = AuctionWSMessage.builder()
                    .type(AuctionWSMessage.Type.ITEM_SOLD)
                    .content(auctionIdToItemIdsMap.get(auctionId).remove(0))
                    .build()
                    .toBinaryMessage();
            sendToActiveSessions(auctionIdToSessionsMap.get(auctionId), binaryMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAuctionEndMessage(long auctionId) {
        try {
            BinaryMessage binaryMessage = AuctionWSMessage.builder()
                    .type(AuctionWSMessage.Type.AUCTION_END)
                    .content("Auction ends here.")
                    .build()
                    .toBinaryMessage();
            sendToActiveSessions(auctionIdToSessionsMap.get(auctionId), binaryMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendItemUnsoldMessage(long auctionId) {
        try {
            BinaryMessage binaryMessage = AuctionWSMessage.builder()
                    .type(AuctionWSMessage.Type.ITEM_UNSOLD)
                    .content(auctionIdToItemIdsMap.get(auctionId).remove(0))
                    .build()
                    .toBinaryMessage();
            sendToActiveSessions(auctionIdToSessionsMap.get(auctionId), binaryMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendNewItemOnDisplayMessage(long auctionId) {

        try {
            if (auctionIdToItemIdsMap.get(auctionId).isEmpty()) {
                // send message auction ends here
                sendAuctionEndMessage(auctionId);
                return;
            }
            BinaryMessage binaryMessage = AuctionWSMessage.builder()
                    .type(AuctionWSMessage.Type.DISPLAY_ITEM)
                    .content(auctionIdToItemIdsMap.get(auctionId).get(0))
                    .build()
                    .toBinaryMessage();
            sendToActiveSessions(auctionIdToSessionsMap.get(auctionId), binaryMessage);
            startTimer(auctionId, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendItems(WebSocketSession session, long auctionId) throws Exception {
        BinaryMessage message = AuctionWSMessage.builder()
                .type(AuctionWSMessage.Type.GET_ITEMS)
                .content(itemService.getItems(auctionId))
                .build()
                .toBinaryMessage();
        session.sendMessage(message);
    }

    private void modifyATSMap(WebSocketSession session, long auctionId, String operation) {
        List<WebSocketSession> sessions = auctionIdToSessionsMap.getOrDefault(auctionId, new ArrayList<>());
        if (operation.equals("add")) {
            sessions.add(session);
        } else {
            sessions.remove(session);
        }
        auctionIdToSessionsMap.put(auctionId, sessions);
    }

    private List<Bidder> getBidders(List<WebSocketSession> sessions) {
        List<Bidder> bidders = new ArrayList<>();
        sessions.stream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                            long userId = (long) session.getAttributes().get("userId");
                            Optional<User> user = userService.getUser(userId);
                            user.ifPresent(value -> bidders.add(Bidder.builder()
                                    .id(value.getId())
                                    .name(value.getName())
                                    .walletAmount(value.getWalletAmount())
                                    .build()));
                        }
                );
        return bidders;
    }

    private long getUserId(WebSocketSession session) {
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Long.parseLong(authHeader.substring(7)); // Keeping it simple: user ID is the token itself
        }
        throw new IllegalArgumentException("Invalid Authorization header");
    }

    private Long getAuctionId(WebSocketSession session) {
        return Long.parseLong(
                Arrays.stream(Objects.requireNonNull(session.getUri()).getQuery().split("&"))
                        .map(entry -> entry.split("="))
                        .collect(Collectors.toMap(entry -> entry[0], entry -> entry[1]))
                        .get("id")
        );
    }

    private static void broadcastBiddersAndStart(List<WebSocketSession> sessions, List<Bidder> bidders) {
        try {
            BinaryMessage startMessage = AuctionWSMessage.builder()
                    .type(AuctionWSMessage.Type.START)
                    .build()
                    .toBinaryMessage();

            BinaryMessage biddersMessage = AuctionWSMessage.builder()
                    .type(AuctionWSMessage.Type.GET_BIDDERS)
                    .content(bidders)
                    .build()
                    .toBinaryMessage();
            sessions.stream()
                    .filter(WebSocketSession::isOpen)
                    .forEach(session -> {
                                try {
                                    session.sendMessage(biddersMessage);
                                    session.sendMessage(startMessage);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendBidPlacedMessage(Bidding bidding, List<WebSocketSession> sessions) {
        try {
            BinaryMessage binaryMessage = AuctionWSMessage.builder()
                    .type(AuctionWSMessage.Type.BID_PLACED)
                    .content(bidding)
                    .build()
                    .toBinaryMessage();
            sendToActiveSessions(sessions, binaryMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendHammerDownMessage(List<WebSocketSession> sessions, AuctionWSMessage.Type messageType) {
        try {
            BinaryMessage binaryMessage = AuctionWSMessage.builder()
                    .type(messageType)
                    .build()
                    .toBinaryMessage();
            sendToActiveSessions(sessions, binaryMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendToActiveSessions(List<WebSocketSession> sessions, BinaryMessage binaryMessage) {
        sessions.stream()
                .filter(WebSocketSession::isOpen)
                .forEach(session -> {
                    try {
                        session.sendMessage(binaryMessage);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
