package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentCommandService;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.RegisterEquipmentResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    private final EquipmentCommandService equipmentCommandService;
    private final io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentQueryService equipmentQueryService;

    public EquipmentController(EquipmentCommandService equipmentCommandService, io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentQueryService equipmentQueryService) {
        this.equipmentCommandService = equipmentCommandService;
        this.equipmentQueryService = equipmentQueryService;
    }

    @PostMapping
    public ResponseEntity<?> registerEquipment(@RequestBody RegisterEquipmentResource resource) {
        try {
            var equipment = equipmentCommandService.registerEquipment(EquipmentType.valueOf(resource.type().toUpperCase()), resource.name(), resource.physicalCode());
            if (equipment.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(equipment.get());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
        }
    }

    @PostMapping("/{equipmentId}/link/{pondId}")
    public ResponseEntity<?> linkEquipmentToPond(@PathVariable Long equipmentId, @PathVariable Long pondId) {
        var equipment = equipmentCommandService.linkEquipmentToPond(equipmentId, pondId);
        if (equipment.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(equipment.get());
    }

    @GetMapping
    public ResponseEntity<java.util.List<io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment>> getAllEquipment(
            @RequestParam(required = false) Long pondId,
            @RequestParam(required = false) Long farmId) {
        if (pondId != null) {
            return ResponseEntity.ok(equipmentQueryService.getByPondId(pondId));
        }
        if (farmId != null) {
            return ResponseEntity.ok(equipmentQueryService.getByFarmId(farmId));
        }
        return ResponseEntity.ok(equipmentQueryService.getAllEquipment());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            equipmentCommandService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
