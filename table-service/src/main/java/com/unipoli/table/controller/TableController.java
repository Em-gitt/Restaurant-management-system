package com.unipoli.table.controller;

import com.unipoli.table.dto.TableResponse;
import com.unipoli.table.dto.UpdateTableStatusRequest;
import com.unipoli.table.service.TableService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
public class TableController {

    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public ResponseEntity<List<TableResponse>> findAll() {
        return ResponseEntity.ok(tableService.findAll());
    }

    @PutMapping("/{tableId}/status")
    public ResponseEntity<TableResponse> updateStatus(
            @PathVariable Long tableId,
            @Valid @RequestBody UpdateTableStatusRequest request
    ) {
        return ResponseEntity.ok(
                tableService.updateStatus(tableId, request)
        );
    }
}
