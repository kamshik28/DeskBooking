package com.kaminskyi.deskbooking.dto;

import com.kaminskyi.deskbooking.entity.Desk;

public record DeskResponse(Long id, String name, Integer floor, boolean active, Long officeId, String officeName) {

    public static DeskResponse from(Desk desk) {
        return new DeskResponse(
                desk.getId(),
                desk.getName(),
                desk.getFloor(),
                desk.isActive(),
                desk.getOffice().getId(),
                desk.getOffice().getName()
        );
    }
}
