package com.kaminskyi.deskbooking.dto;

import com.kaminskyi.deskbooking.entity.Office;

public record OfficeResponse(Long id, String name, String city, String address) {

    public static OfficeResponse from(Office office) {
        return new OfficeResponse(office.getId(), office.getName(), office.getCity(), office.getAddress());
    }
}
