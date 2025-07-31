package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.DeskRequest;
import org.example.entity.Desk;
import org.example.entity.Office;
import org.example.repository.DeskRepository;
import org.example.repository.OfficeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/desks")
@RequiredArgsConstructor
public class DeskController {

    private final DeskRepository deskRepository;
    private final OfficeRepository officeRepository;

    @GetMapping
    public Page<Desk> getAllDesks(
            @RequestParam Optional<Long> officeId,
            @RequestParam Optional<Integer> floor,
            @RequestParam Optional<String> name,
            Pageable pageable
    ) {
        Specification<Desk> spec = Specification.where(null);

        if (officeId.isPresent()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("office").get("id"), officeId.get()));
        }

        if (floor.isPresent()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("floor"), floor.get()));
        }

        if (name.isPresent() && !name.get().isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")),
                            "%" + name.get().toLowerCase() + "%"));
        }

        return deskRepository.findAll(spec, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Desk> getDeskById(@PathVariable Long id) {
        return deskRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Desk> createDesk(@Valid @RequestBody DeskRequest request) {
        // Шукаємо офіс за переданим ID
        Office office = officeRepository.findById(request.getOfficeId())
                .orElse(null);
        if (office == null) {
            return ResponseEntity.notFound().build();
        }

        Desk desk = new Desk();
        desk.setName(request.getName());
        desk.setFloor(request.getFloor());
        desk.setOffice(office);

        Desk saved = deskRepository.save(desk);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Desk> updateDesk(@PathVariable Long id,
                                           @Valid @RequestBody DeskRequest request) {
        return deskRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    existing.setFloor(request.getFloor());
                    Office office = officeRepository.findById(request.getOfficeId())
                            .orElse(null);
                    if (office == null) {
                        return null;
                    }
                    existing.setOffice(office);
                    return ResponseEntity.ok(deskRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesk(@PathVariable Long id) {
        if (!deskRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        deskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
