package com.kaminskyi.deskbooking.service;

import com.kaminskyi.deskbooking.dto.DeskRequest;
import com.kaminskyi.deskbooking.dto.DeskResponse;
import com.kaminskyi.deskbooking.entity.Desk;
import com.kaminskyi.deskbooking.entity.Office;
import com.kaminskyi.deskbooking.exception.ConflictException;
import com.kaminskyi.deskbooking.exception.NotFoundException;
import com.kaminskyi.deskbooking.repository.BookingRepository;
import com.kaminskyi.deskbooking.repository.DeskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeskService {

    private final DeskRepository deskRepository;
    private final BookingRepository bookingRepository;
    private final OfficeService officeService;

    @Transactional(readOnly = true)
    public Page<DeskResponse> search(Long officeId, Integer floor, String name, Pageable pageable) {
        Specification<Desk> spec = Specification.where(null);

        if (officeId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("office").get("id"), officeId));
        }
        if (floor != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("floor"), floor));
        }
        if (name != null && !name.isBlank()) {
            String pattern = "%" + name.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }

        return deskRepository.findAll(spec, pageable).map(DeskResponse::from);
    }

    @Transactional(readOnly = true)
    public DeskResponse getById(Long id) {
        return DeskResponse.from(findDesk(id));
    }

    @Transactional(readOnly = true)
    public List<DeskResponse> findAvailable(Long officeId, LocalDate date) {
        officeService.findOffice(officeId);
        return deskRepository.findAvailable(officeId, date).stream()
                .map(DeskResponse::from)
                .toList();
    }

    @Transactional
    public DeskResponse create(DeskRequest request) {
        Office office = officeService.findOffice(request.officeId());
        checkNameIsFree(office.getId(), request.name());

        Desk desk = new Desk();
        desk.setName(request.name().trim());
        desk.setFloor(request.floor());
        desk.setOffice(office);
        desk.setActive(request.active() == null || request.active());

        return DeskResponse.from(deskRepository.save(desk));
    }

    @Transactional
    public DeskResponse update(Long id, DeskRequest request) {
        Desk desk = findDesk(id);
        Office office = officeService.findOffice(request.officeId());

        boolean nameChanged = !desk.getName().equalsIgnoreCase(request.name().trim())
                || !desk.getOffice().getId().equals(office.getId());
        if (nameChanged) {
            checkNameIsFree(office.getId(), request.name());
        }

        desk.setName(request.name().trim());
        desk.setFloor(request.floor());
        desk.setOffice(office);
        if (request.active() != null) {
            desk.setActive(request.active());
        }
        return DeskResponse.from(desk);
    }

    @Transactional
    public void delete(Long id) {
        Desk desk = findDesk(id);
        // keep history, desk with bookings can only be deactivated
        if (bookingRepository.existsByDeskId(id)) {
            throw new ConflictException("Desk has bookings, deactivate it instead");
        }
        deskRepository.delete(desk);
    }

    Desk findDesk(Long id) {
        return deskRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Desk", id));
    }

    private void checkNameIsFree(Long officeId, String name) {
        if (deskRepository.existsByOfficeIdAndNameIgnoreCase(officeId, name.trim())) {
            throw new ConflictException("Desk '" + name.trim() + "' already exists in this office");
        }
    }
}
