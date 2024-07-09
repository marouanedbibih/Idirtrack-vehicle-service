package com.idirtrack.vehicle_service.sim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimDTO {

    private Long id;
    private Long simMicroserviceId;
    private String phoneNumber;
    private String type;

    // Build the entity to dto
    public Sim toEntity() {
        return Sim.builder()
                .id(this.id)
                .simMicroserviceId(this.simMicroserviceId)
                .phoneNumber(this.phoneNumber)
                .type(this.type)
                .build();
    }
}