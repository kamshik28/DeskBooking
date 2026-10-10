package com.kaminskyi.deskbooking.controller;

import com.kaminskyi.deskbooking.dto.DeskRequest;
import com.kaminskyi.deskbooking.dto.DeskResponse;
import com.kaminskyi.deskbooking.service.DeskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/desks")
@RequiredArgsConstructor
public class DeskController {

    private final DeskService deskService;

    @GetMapping
    public Page<DeskResponse> search(@RequestParam(required = false) Long officeId,
                                     @RequestParam(required = false) Integer floor,
                                     @RequestParam(required = false) String name,
                                     @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
                                     Pageable pageable) {
        return deskService.search(officeId, floor, name, pageable);
    }

    @GetMapping("/available")
    public List<DeskResponse> available(@RequestParam Long officeId,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return deskService.findAvailable(officeId, date);
    }

    @GetMapping("/{id}")
    public DeskResponse getById(@PathVariable Long id) {
        return deskService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public DeskResponse create(@Valid @RequestBody DeskRequest request) {
        return deskService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DeskResponse update(@PathVariable Long id, @Valid @RequestBody DeskRequest request) {
        return deskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        deskService.delete(id);
    }
}
