package com.idirtrack.vehicle_service.vehicle.https;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleRequest {
    @NotBlank(message = "Matricule is required")
    private String matricule;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "User Microservice ID is required")
    private Long clientMicroserviceId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String clientName;

    @NotBlank(message = "Company is required")
    @Size(max = 255, message = "Company cannot exceed 255 characters")
    private String clientCompany;

    @NotNull(message = "Boitiers IDs are required")
    private List<Long> boitiersIds;
}
