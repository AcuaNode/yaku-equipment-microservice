package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.events.EquipmentRegistrationRequested;
import io.github.rafaviv.yakubackend.equipment.domain.model.events.SensorLinkedToPondEvent;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.events.kafka.KafkaDomainEventPublisher;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EquipmentCommandServiceImpl implements EquipmentCommandService {

    private static final String EQUIPMENT_EVENTS_TOPIC = "equipment-events";
    private static final String POND_EVENTS_TOPIC = "pond-events";

    private final EquipmentRepository equipmentRepository;
    private final PondRepository pondRepository;
    private final KafkaDomainEventPublisher eventPublisher;

    public EquipmentCommandServiceImpl(EquipmentRepository equipmentRepository, PondRepository pondRepository, KafkaDomainEventPublisher eventPublisher) {
        this.equipmentRepository = equipmentRepository;
        this.pondRepository = pondRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Equipment> registerEquipment(EquipmentType type, String name, String physicalCode) {
        Equipment equipment = new Equipment(type, name, physicalCode);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        try {
            eventPublisher.publish(EQUIPMENT_EVENTS_TOPIC, new EquipmentRegistrationRequested(savedEquipment.getId()));
        } catch (Exception e) {
            throw new RuntimeException("Error publishing equipment registration event", e);
        }

        return Optional.of(savedEquipment);
    }

    @Override
    public Optional<Equipment> linkEquipmentToPond(Long equipmentId, Long pondId) {
        var equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));
        var pond = pondRepository.findById(pondId)
                .orElseThrow(() -> new IllegalArgumentException("Pond not found"));

        equipment.linkToPond(pond.getId());
        Equipment savedEquipment = equipmentRepository.save(equipment);

        if (savedEquipment.getType() == EquipmentType.SENSOR) {
            eventPublisher.publish(POND_EVENTS_TOPIC, new SensorLinkedToPondEvent(savedEquipment.getId(), pond.getId()));
        }

        return Optional.of(savedEquipment);
    }

    @Override
    public void deleteEquipment(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new IllegalArgumentException("Equipment not found with id: " + equipmentId);
        }
        equipmentRepository.deleteById(equipmentId);
    }
}