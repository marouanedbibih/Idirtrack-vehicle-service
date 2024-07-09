package com.idirtrack.vehicle_service.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long>{

    boolean existsByMatricule(String matricule);
    
}
