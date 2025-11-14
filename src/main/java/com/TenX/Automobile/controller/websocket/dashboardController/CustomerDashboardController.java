package com.TenX.Automobile.controller.websocket.dashboardController;

import com.TenX.Automobile.model.dto.response.CustomerDashboardResponse;
import com.TenX.Automobile.service.websocket.dashboardService.CustomerDashboardWSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CustomerDashboardController {

    private final CustomerDashboardWSService customerDashboardWSService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/customer/dashboard/request")
    public void requestDashboardUpdate(String username) {
        log.info("WebSocket request received for user: {}", username);

        // Fetch real-time data
        CustomerDashboardResponse update = customerDashboardWSService.getRealtimeOverview(username);

        // Push to subscribed clients
        messagingTemplate.convertAndSend("/topic/customer/dashboard/" + username, update);
    }


}
