package com.unipoli.table.dto;

import com.unipoli.table.model.TableStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTableStatusRequest(

        @NotNull
        TableStatus status,

        Long assignedWaiterId
) {
}
