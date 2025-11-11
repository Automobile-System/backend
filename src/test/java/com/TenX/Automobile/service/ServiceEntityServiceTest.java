package com.TenX.Automobile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.TenX.Automobile.model.dto.request.ServiceRequest;
import com.TenX.Automobile.model.dto.response.ServiceResponse;
import com.TenX.Automobile.model.entity.Service;
import com.TenX.Automobile.repository.ServiceRepository;

@ExtendWith(MockitoExtension.class)
class ServiceEntityServiceTest {

    @Mock
    private ServiceRepository serviceRepository;

    @InjectMocks
    private ServiceEntityService serviceEntityService;

    private Service serviceEntity;
    private ServiceRequest request;

    @BeforeEach
    void setUp() {
        serviceEntity = Service.builder()
                .serviceId(1L)
                .title("Engine Tune Up")
                .description("Full engine tune up")
                .category("Engine")
                .imageUrl("https://example.com/engine.jpg")
                .estimatedHours(5.0)
                .cost(250.0)
                .build();

        request = ServiceRequest.builder()
                .title("Engine Tune Up")
                .description("Full engine tune up")
                .category("Engine")
                .imageUrl("https://example.com/engine.jpg")
                .estimatedHours(5.0)
                .cost(250.0)
                .build();
    }

    @Test
    @DisplayName("findAll should convert entities to responses")
    void findAllReturnsAllServices() {
        when(serviceRepository.findAll()).thenReturn(List.of(serviceEntity));

        List<ServiceResponse> responses = serviceEntityService.findAll();

        assertThat(responses).hasSize(1);
        ServiceResponse response = responses.get(0);
        assertThat(response.getServiceId()).isEqualTo(serviceEntity.getServiceId());
        assertThat(response.getTitle()).isEqualTo(serviceEntity.getTitle());
        verify(serviceRepository).findAll();
    }

    @Test
    @DisplayName("findById should return response when service exists")
    void findByIdReturnsService() {
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(serviceEntity));

        ServiceResponse response = serviceEntityService.findById(1L);

        assertThat(response.getTitle()).isEqualTo("Engine Tune Up");
        verify(serviceRepository).findById(1L);
    }

    @Test
    @DisplayName("findById should throw when service does not exist")
    void findByIdThrowsWhenMissing() {
        when(serviceRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceEntityService.findById(42L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Service not found with id: 42");
    }

    @Test
    @DisplayName("create should persist and return response")
    void createPersistsService() {
        when(serviceRepository.save(any(Service.class))).thenReturn(serviceEntity);

        ServiceResponse response = serviceEntityService.create(request);

        assertThat(response.getServiceId()).isEqualTo(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("update should modify existing service")
    void updateModifiesService() {
        Service updatedEntity = Service.builder()
                .serviceId(serviceEntity.getServiceId())
                .title("Updated Title")
                .description(serviceEntity.getDescription())
                .category(serviceEntity.getCategory())
                .imageUrl(serviceEntity.getImageUrl())
                .estimatedHours(serviceEntity.getEstimatedHours())
                .cost(serviceEntity.getCost())
                .build();

        when(serviceRepository.findById(1L)).thenReturn(Optional.of(serviceEntity));
        when(serviceRepository.save(any(Service.class))).thenReturn(updatedEntity);

        ServiceRequest updateRequest = ServiceRequest.builder()
                .title("Updated Title")
                .description(request.getDescription())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .estimatedHours(request.getEstimatedHours())
                .cost(request.getCost())
                .build();

        ServiceResponse response = serviceEntityService.update(1L, updateRequest);

        assertThat(response.getTitle()).isEqualTo("Updated Title");
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("delete should remove service when it exists")
    void deleteRemovesService() {
        when(serviceRepository.existsById(1L)).thenReturn(true);

        serviceEntityService.delete(1L);

        verify(serviceRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete should throw when service is missing")
    void deleteThrowsWhenMissing() {
        when(serviceRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> serviceEntityService.delete(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Service not found with id: 1");

        verify(serviceRepository, never()).deleteById(anyLong());
    }
}

