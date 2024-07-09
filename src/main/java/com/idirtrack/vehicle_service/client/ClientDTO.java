package com.idirtrack.vehicle_service.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private Long id;
    private Long userMicroserviceId;
    private String name;

    // Build dto to entity
    public Client toEntity() {
        return Client.builder()
                .id(this.id)
                .userMicroserviceId(this.userMicroserviceId)
                .name(this.name)
                .build();
    }
}
