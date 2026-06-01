package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetAllPondsQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.services.FarmQueryService;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondCommandService;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondQueryService;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources.PondResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.CreatePondResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.PondResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/ponds", produces = MediaType.APPLICATION_JSON_VALUE)
public class PondController {

    private final PondCommandService pondCommandService;
    private final PondQueryService pondQueryService;
    private final FarmQueryService farmQueryService;

    public PondController(PondCommandService pondCommandService, PondQueryService pondQueryService, FarmQueryService farmQueryService) {
        this.pondCommandService = pondCommandService;
        this.pondQueryService = pondQueryService;
        this.farmQueryService = farmQueryService;
    }

    @PostMapping
    public ResponseEntity<PondResource> createPond(@RequestBody CreatePondResource resource, @RequestHeader("X-User-Id") Long ownerId) {
        var pond = pondCommandService.createPond(resource.farmId(), resource.name(), resource.species(), resource.volume(), ownerId);
        if (pond.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var pondResource = PondResourceFromEntityAssembler.toResourceFromEntity(pond.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(pondResource);
    }

    @GetMapping
    public ResponseEntity<List<PondResource>> getAllPonds() {
        var query = new GetAllPondsQuery();
        var ponds = pondQueryService.handle(query);
        var resources = ponds.stream()
                .map(PondResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PondResource> getPondById(@PathVariable Long id) {
        var query = new GetPondByIdQuery(id);
        var pond = pondQueryService.handle(query);
        if (pond.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var pondResource = PondResourceFromEntityAssembler.toResourceFromEntity(pond.get());
        return ResponseEntity.ok(pondResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePond(@PathVariable Long id, @RequestHeader("X-User-Id") Long ownerId) {
        try {
            pondCommandService.deletePond(id, ownerId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<List<PondResource>> getPondsByFarmId(@PathVariable Long farmId, @RequestHeader("X-User-Id") Long ownerId) {
        var farm = farmQueryService.handle(new io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetFarmsByOwnerIdQuery(ownerId))
                .stream().filter(f -> f.getId().equals(farmId)).findFirst();
        if (farm.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var query = new io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByFarmIdQuery(farmId);
        var ponds = pondQueryService.handle(query);
        var resources = ponds.stream()
                .map(PondResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PondResource> updatePond(@PathVariable Long id, @RequestBody CreatePondResource resource, @RequestHeader("X-User-Id") Long ownerId) {
        return pondCommandService.updatePond(id, resource.name(), resource.species(), resource.volume(), ownerId)
                .map(pond -> ResponseEntity.ok(PondResourceFromEntityAssembler.toResourceFromEntity(pond)))
                .orElse(ResponseEntity.notFound().build());
    }
}