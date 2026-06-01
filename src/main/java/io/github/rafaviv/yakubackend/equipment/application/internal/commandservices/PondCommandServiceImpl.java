package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PondCommandServiceImpl implements PondCommandService {

    private final PondRepository pondRepository;
    private final FarmRepository farmRepository;

    public PondCommandServiceImpl(PondRepository pondRepository, FarmRepository farmRepository) {
        this.pondRepository = pondRepository;
        this.farmRepository = farmRepository;
    }

    @Override
    public Optional<Pond> createPond(Long farmId, String name, String species, Double volume, Long ownerId) {
        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new IllegalArgumentException("Farm not found with id: " + farmId));
        if (!farm.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Farm not found with id: " + farmId);
        }
        Pond pond = new Pond(farmId, name, species, volume);
        return Optional.of(pondRepository.save(pond));
    }

    @Override
    public void deletePond(Long pondId, Long ownerId) {
        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() -> new IllegalArgumentException("Pond not found with id: " + pondId));
        Farm farm = farmRepository.findById(pond.getFarmId())
                .orElseThrow(() -> new IllegalArgumentException("Farm not found with id: " + pond.getFarmId()));
        if (!farm.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Pond not found with id: " + pondId);
        }
        pondRepository.deleteById(pondId);
    }

    @Override
    public Optional<Pond> updatePond(Long pondId, String name, String species, Double volume, Long ownerId) {
        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() -> new IllegalArgumentException("Pond not found with id: " + pondId));
        Farm farm = farmRepository.findById(pond.getFarmId())
                .orElseThrow(() -> new IllegalArgumentException("Farm not found with id: " + pond.getFarmId()));
        if (!farm.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Pond not found with id: " + pondId);
        }
        pond.update(name, species, volume);
        return Optional.of(pondRepository.save(pond));
    }
}
