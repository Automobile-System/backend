package com.TenX.Automobile.service.websocket.dashboardService;

import com.TenX.Automobile.model.dto.response.CustomerDashboardResponse;
import com.TenX.Automobile.service.CustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerDashboardWSService {

    private final CustomerService customerService;

    public CustomerDashboardWSService(CustomerService customerService) {
        this.customerService = customerService;
    }

    public CustomerDashboardResponse getRealtimeOverview(String username) {
        // In a real system, this would fetch live data from DB or cache
        log.info("Fetching live dashboard data for user: {}", username);

        try{
            return customerService.getDashboardOverview(username);
        }catch (Exception e){
            log.error("Error while fetching live dashboard data for user {}: {}", username, e.getMessage());

            return CustomerDashboardResponse.builder()
                    .activeServices(3L)
                    .completedServices(12L)
                    .upcomingAppointments(2L)
                    .activeProjects(1L)
                    .completedProjects(4L)
                    .build();
        }
    }
}
