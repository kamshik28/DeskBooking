package com.kaminskyi.deskbooking.service;

import com.kaminskyi.deskbooking.dto.OfficeRequest;
import com.kaminskyi.deskbooking.dto.OfficeResponse;
import com.kaminskyi.deskbooking.entity.Office;
import com.kaminskyi.deskbooking.exception.ConflictException;
import com.kaminskyi.deskbooking.exception.NotFoundException;
import com.kaminskyi.deskbooking.repository.DeskRepository;
import com.kaminskyi.deskbooking.repository.OfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfficeService {

    private final OfficeRepository officeRepository;
    private final DeskRepository deskRepository;

    @Transactional(readOnly = true)
    public List<OfficeResponse> getAll() {
        return officeRepository.findAll(Sort.by("name")).stream()
                .map(OfficeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OfficeResponse getById(Long id) {
        return OfficeResponse.from(findOffice(id));
    }

    @Transactional
    public OfficeResponse create(OfficeRequest request) {
        Office office = new Office();
        apply(office, request);
        return OfficeResponse.from(officeRepository.save(office));
    }

    @Transactional
    public OfficeResponse update(Long id, OfficeRequest request) {
        Office office = findOffice(id);
        apply(office, request);
        return OfficeResponse.from(office);
    }

    @Transactional
    public void delete(Long id) {
        Office office = findOffice(id);
        if (deskRepository.existsByOfficeId(id)) {
            throw new ConflictException("Office still has desks, remove them first");
        }
        officeRepository.delete(office);
    }

    Office findOffice(Long id) {
        return officeRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Office", id));
    }

    private void apply(Office office, OfficeRequest request) {
        office.setName(request.name().trim());
        office.setCity(request.city().trim());
        office.setAddress(request.address().trim());
    }
}
