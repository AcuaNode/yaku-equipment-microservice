package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.commands.CreateFarmCommand;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetFarmsByOwnerIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.services.FarmCommandService;
import io.github.rafaviv.yakubackend.equipment.domain.services.FarmQueryService;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources.CreateFarmResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources.FarmResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/farms")
public class FarmController {

    private final FarmCommandService farmCommandService;
    private final FarmQueryService farmQueryService;

    public FarmController(FarmCommandService farmCommandService, FarmQueryService farmQueryService) {
        this.farmCommandService = farmCommandService;
        this.farmQueryService = farmQueryService;
    }

    @PostMapping
    public ResponseEntity<FarmResource> createFarm(@RequestBody CreateFarmResource resource, @RequestHeader("X-User-Id") Long ownerId) {
        CreateFarmCommand command = new CreateFarmCommand(resource.name(), ownerId, resource.address());
        var farm = farmCommandService.handle(command);
        if (farm.isEmpty()) return ResponseEntity.badRequest().build();

        var createdFarm = farm.get();
        var farmResource = new FarmResource(createdFarm.getId(), createdFarm.getName(), createdFarm.getOwnerId(), createdFarm.getAddress(), createdFarm.getFarmToken());

        return new ResponseEntity<>(farmResource, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarm(@PathVariable Long id, @RequestHeader("X-User-Id") Long ownerId) {
        try {
            farmCommandService.deleteFarm(id, ownerId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<FarmResource>> getAllFarmsByOwner(@RequestHeader("X-User-Id") Long ownerId) {
        var query = new GetFarmsByOwnerIdQuery(ownerId);
        var farms = farmQueryService.handle(query);
        var resources = farms.stream()
                .map(farm -> new FarmResource(farm.getId(), farm.getName(), farm.getOwnerId(), farm.getAddress(), farm.getFarmToken()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PatchMapping("/{id}/token")
    public ResponseEntity<FarmResource> regenerateToken(@PathVariable Long id, @RequestHeader("X-User-Id") Long ownerId) {
        try {
            return farmCommandService.regenerateToken(id, ownerId)
                    .map(farm -> ResponseEntity.ok(new FarmResource(farm.getId(), farm.getName(), farm.getOwnerId(), farm.getAddress(), farm.getFarmToken())))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

}