package com.unipoli.table.service;

import com.unipoli.table.dto.TableResponse;
import com.unipoli.table.dto.UpdateTableStatusRequest;
import com.unipoli.table.model.RestaurantTable;
import com.unipoli.table.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TableService {

    private final RestaurantTableRepository repository;

    public TableService(RestaurantTableRepository repository) {
        this.repository = repository;
    }

    public List<TableResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TableResponse updateStatus(
            Long tableId,
            UpdateTableStatusRequest request
    ) {

        RestaurantTable table = repository.findById(tableId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Table not found: " + tableId
                        )
                );

        table.setStatus(request.status());

        RestaurantTable updatedTable = repository.save(table);

        return toResponse(updatedTable);
    }

    private TableResponse toResponse(RestaurantTable table) {
        return new TableResponse(
                table.getId(),
                table.getTableNumber(),
                table.getCapacity(),
                table.getStatus(),
                table.getCurrentOrderId()
        );
    }
}
