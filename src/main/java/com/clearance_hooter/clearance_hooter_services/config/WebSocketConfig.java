package com.clearance_hooter.clearance_hooter_services.config;

import com.clearance_hooter.clearance_hooter_services.features.auctions.AuctionWSHandshakeInterceptor;
import com.clearance_hooter.clearance_hooter_services.features.auctions.AuctionWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Configuration
@EnableWebSocket
@EnableScheduling
public class WebSocketConfig implements WebSocketConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);
    private final AuctionWebSocketHandler auctionWebSocketHandler;
    private final AuctionWSHandshakeInterceptor auctionWSHandshakeInterceptor;

    public WebSocketConfig(AuctionWebSocketHandler auctionWebSocketHandler, AuctionWSHandshakeInterceptor auctionWSHandshakeInterceptor) {
        this.auctionWebSocketHandler = auctionWebSocketHandler;
        this.auctionWSHandshakeInterceptor = auctionWSHandshakeInterceptor;
    }

    @EventListener
    public void onDisconnectEvent(SessionDisconnectEvent event) {
        logger.info("disconnected: " + event.getSessionId());
    }

    @Scheduled(cron = "1 1 9-21 * * *")
    void startAuctionScheduler() {
        auctionWebSocketHandler.startAuctionScheduler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(auctionWebSocketHandler, "/ws-auctions")
                .addInterceptors(auctionWSHandshakeInterceptor);
    }
}
