package com.kaminskyi.deskbooking.controller;

import com.kaminskyi.deskbooking.dto.OfficeRequest;
import com.kaminskyi.deskbooking.dto.OfficeResponse;
import com.kaminskyi.deskbooking.service.OfficeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    @GetMapping
    public List<OfficeResponse> getAll() {
        return officeService.getAll();
    }

    @GetMapping("/{id}")
    public OfficeResponse getById(@PathVariable Long id) {
        return officeService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public OfficeResponse create(@Valid @RequestBody OfficeRequest request) {
        return officeService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public OfficeResponse update(@PathVariable Long id, @Valid @RequestBody OfficeRequest request) {
        return officeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        officeService.delete(id);
    }
}
