package io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByPondId(Long pondId);
    List<Equipment> findByStatus(EquipmentStatus status);

    @Query("SELECT e FROM Equipment e WHERE e.pondId IN (SELECT p.id FROM Pond p WHERE p.farmId = :farmId)")
    List<Equipment> findByFarmId(@Param("farmId") Long farmId);
}
