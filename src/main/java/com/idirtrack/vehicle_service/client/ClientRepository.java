package com.idirtrack.vehicle_service.client;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long>{

    Client findByUserMicroserviceId(Long userMicroserviceId);
    Boolean existsByUserMicroserviceId(Long userMicroserviceId);
}
