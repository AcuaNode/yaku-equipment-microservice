package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;

import java.util.Optional;

public interface PondCommandService {
    Optional<Pond> createPond(Long farmId, String name, String species, Double volume, Long ownerId);
    void deletePond(Long pondId, Long ownerId);
    Optional<Pond> updatePond(Long pondId, String name, String species, Double volume, Long ownerId);
}
