package com.idirtrack.vehicle_service.boitier;


import java.util.List;
import java.util.stream.Collectors;

import com.idirtrack.vehicle_service.boitier.dto.BoitierDTO;
import com.idirtrack.vehicle_service.device.Device;
import com.idirtrack.vehicle_service.sim.Sim;
import com.idirtrack.vehicle_service.subscription.Subscription;
import com.idirtrack.vehicle_service.vehicle.Vehicle;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "boitier")
public class Boitier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @OneToOne
    @JoinColumn(name = "device_id")
    private Device device;

    @OneToOne
    @JoinColumn(name = "sim_id")
    private Sim sim;

    @OneToMany(mappedBy = "boitier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Subscription> subscribtions;

    public BoitierDTO toDTO() {
        return BoitierDTO.builder()
                .id(this.id)
                .device(this.device.toDTO())
                .sim(this.sim.toDTO())
                .vehicle(this.vehicle.toDTO())
                .subscriptionsList(this.subscribtions.stream().map(Subscription::toDTO).collect(Collectors.toList()))
                .build();
    }

    public static List<BoitierDTO> transformToDTOList(List<Boitier> boitiers) {
        return boitiers.stream()
                .map(Boitier::toDTO)
                .collect(Collectors.toList());
    }
}
