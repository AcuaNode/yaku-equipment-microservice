package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;

import java.util.List;

public interface EquipmentQueryService {
    List<Equipment> getAllEquipment();
    List<Equipment> getByPondId(Long pondId);
    List<Equipment> getByFarmId(Long farmId);
}
