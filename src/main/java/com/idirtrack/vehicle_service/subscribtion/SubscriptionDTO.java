package com.idirtrack.vehicle_service.subscribtion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDTO {

    private Long id;
    private String dateStart;
    private String dateFin;
    private Long boitierId;
}