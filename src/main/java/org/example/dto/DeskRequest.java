package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeskRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Floor is required")
    private Integer floor;

    @NotNull(message = "Office ID is required")
    private Long officeId;
}
