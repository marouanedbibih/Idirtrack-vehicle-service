package com.idirtrack.vehicle_service.sim;

import com.idirtrack.vehicle_service.boitier.Boitier;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sim")
public class Sim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long simMicroserviceId;
    private String phoneNumber;
    private String type;

    @OneToOne(mappedBy = "sim")
    private Boitier boitier;

    
}
