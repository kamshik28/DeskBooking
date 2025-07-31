package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.OfficeRequest;
import org.example.entity.Office;
import org.example.repository.OfficeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeRepository officeRepository;

    @GetMapping
    public List<Office> getAll() {
        return officeRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Office> create(@Valid @RequestBody OfficeRequest request) {
        Office office = new Office();
        office.setName(request.getName());
        office.setCity(request.getCity());
        office.setAddress(request.getAddress());

        return ResponseEntity.ok(officeRepository.save(office));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Office> updateOffice(@PathVariable Long id, @Valid @RequestBody Office updatedOffice) {
        return officeRepository.findById(id)
                .map(existingOffice -> {
                    existingOffice.setName(updatedOffice.getName());
                    existingOffice.setCity(updatedOffice.getCity());
                    existingOffice.setAddress(updatedOffice.getAddress());
                    Office saved = officeRepository.save(existingOffice);
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffice(@PathVariable Long id) {
        if (officeRepository.existsById(id)) {
            officeRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}
