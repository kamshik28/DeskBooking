package com.kaminskyi.deskbooking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeskRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(0) Integer floor,
        @NotNull Long officeId,
        Boolean active
) {
}
