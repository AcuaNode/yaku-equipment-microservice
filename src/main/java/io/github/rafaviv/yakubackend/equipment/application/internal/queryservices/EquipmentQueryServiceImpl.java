package io.github.rafaviv.yakubackend.equipment.application.internal.queryservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentQueryService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipmentQueryServiceImpl implements EquipmentQueryService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentQueryServiceImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    public List<Equipment> getByPondId(Long pondId) {
        return equipmentRepository.findByPondId(pondId);
    }

    @Override
    public List<Equipment> getByFarmId(Long farmId) {
        return equipmentRepository.findByFarmId(farmId);
    }
}
