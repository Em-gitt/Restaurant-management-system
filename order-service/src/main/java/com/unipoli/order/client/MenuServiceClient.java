package com.unipoli.order.client;

import com.unipoli.order.dto.DishInfo;
import com.unipoli.order.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MenuServiceClient {

    private final RestClient restClient;

    public MenuServiceClient(@Value("${services.menu.url:http://localhost:8083}") String menuServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(menuServiceUrl)
                .build();
    }

    public Map<Long, DishInfo> getDishesByIds(List<Long> dishIds, String authHeader) {
        try {
            var requestSpec = restClient.get()
                    .uri("/api/v1/menu/dishes");

            if (authHeader != null && !authHeader.isBlank()) {
                requestSpec.header("Authorization", authHeader);
            }

            List<DishInfo> dishes = requestSpec.retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (dishes == null) {
                return Map.of();
            }

            return dishes.stream()
                    .filter(d -> dishIds.contains(d.dishId()))
                    .collect(Collectors.toMap(DishInfo::dishId, d -> d));

        } catch (Exception e) {
            throw new ExternalServiceException("Error communicating with Menu Service: " + e.getMessage());
        }
    }
}
