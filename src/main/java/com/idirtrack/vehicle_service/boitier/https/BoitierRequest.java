package com.idirtrack.vehicle_service.boitier.https;

import org.springframework.data.annotation.AccessType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoitierRequest {
    // Device Informations
    @NotNull(message = "Stock Microservice ID is required")
    private Long deviceMicroserviceId;

    @NotNull(message = "IMEI is required")
    @Min(value = 100000000, message = "IMEI should be at least 9 digits")
    private Integer imei;

    @NotBlank(message = "Type is required")
    private String deviceType;

    // Card Sim Informations
    @NotNull(message = "Stock Microservice ID is required")
    private Long simMicroserviceId;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d{10}", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotBlank(message = "SIM type is required")
    private String simType;


    // Subscription Informations
    @NotBlank(message = "Start date is required")
    // @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "Start date must be in the format dd/MM/yyyy")
    private String dateStart;

    @NotBlank(message = "End date is required")
    // @Pattern(regexp = "\\d{2}/\\d{2}/\\d{4}", message = "End date must be in the format dd/MM/yyyy")
    private String dateFin;
}
