package com.TenX.Automobile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import com.TenX.Automobile.model.dto.request.ServiceRequest;
import com.TenX.Automobile.model.dto.response.ServiceResponse;
import com.TenX.Automobile.repository.ServiceRepository;

@DataJpaTest
@EntityScan(basePackageClasses = com.TenX.Automobile.model.entity.Service.class)
@EnableJpaRepositories(basePackageClasses = ServiceRepository.class)
@Import(ServiceEntityService.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class ServiceEntityServiceIntegrationTest {

    @Autowired
    private ServiceEntityService serviceEntityService;

    @Autowired
    private ServiceRepository serviceRepository;

    @BeforeEach
    void cleanDatabase() {
        serviceRepository.deleteAll();
    }

    @Test
    void createAndFetchService() {
        ServiceRequest request = ServiceRequest.builder()
                .title("Wheel Alignment")
                .description("Advanced laser-guided wheel alignment.")
                .category("Maintenance")
                .imageUrl("https://example.com/wheel.jpg")
                .estimatedHours(2.5)
                .cost(150.0)
                .build();

        ServiceResponse created = serviceEntityService.create(request);

        assertThat(created.getServiceId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Wheel Alignment");

        ServiceResponse fetched = serviceEntityService.findById(created.getServiceId());
        assertThat(fetched.getTitle()).isEqualTo("Wheel Alignment");
        assertThat(serviceEntityService.findAll()).hasSize(1);
    }

    @Test
    void updateAndDeleteService() {
        ServiceRequest request = ServiceRequest.builder()
                .title("Air Conditioning Service")
                .description("Deep clean and refrigerant refill.")
                .category("Cooling")
                .imageUrl(null)
                .estimatedHours(3.0)
                .cost(180.0)
                .build();

        ServiceResponse created = serviceEntityService.create(request);

        ServiceRequest update = ServiceRequest.builder()
                .title("Air Conditioning Overhaul")
                .description("Full AC system diagnostics and repair.")
                .category("Cooling")
                .imageUrl(null)
                .estimatedHours(4.0)
                .cost(240.0)
                .build();

        ServiceResponse updated = serviceEntityService.update(created.getServiceId(), update);
        assertThat(updated.getTitle()).isEqualTo("Air Conditioning Overhaul");
        assertThat(updated.getEstimatedHours()).isEqualTo(4.0);

        serviceEntityService.delete(created.getServiceId());
        assertThat(serviceEntityService.findAll()).isEmpty();

        assertThatThrownBy(() -> serviceEntityService.findById(created.getServiceId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Service not found");
    }
}

