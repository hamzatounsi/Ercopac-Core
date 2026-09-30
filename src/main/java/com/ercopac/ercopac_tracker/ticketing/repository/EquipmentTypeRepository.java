package com.ercopac.ercopac_tracker.ticketing.repository;

import com.ercopac.ercopac_tracker.ticketing.domain.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Long> {
    List<EquipmentType> findAllByActiveTrueOrderByNameAsc();
}