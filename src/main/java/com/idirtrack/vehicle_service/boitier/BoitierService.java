package com.idirtrack.vehicle_service.boitier;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.idirtrack.vehicle_service.basic.BasicException;
import com.idirtrack.vehicle_service.basic.BasicResponse;
import com.idirtrack.vehicle_service.boitier.https.BoitierRequest;
import com.idirtrack.vehicle_service.device.Device;
import com.idirtrack.vehicle_service.device.DeviceDTO;
import com.idirtrack.vehicle_service.device.DeviceRepository;
import com.idirtrack.vehicle_service.sim.Sim;
import com.idirtrack.vehicle_service.sim.SimDTO;
import com.idirtrack.vehicle_service.sim.SimRepository;
import com.idirtrack.vehicle_service.utils.ValidationUtil;

import jakarta.validation.Valid;

@Service
public class BoitierService {

    @Autowired
    private BoitierRepository boitierRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private SimRepository simRepository;

    public BasicResponse createboitier(BoitierRequest request)
            throws BasicException {
        Map<String, String> errors;
        // Check if device exists in stock microservice
        // Check if card sim exists in stock microservice
        // Check if subscription not past
        // Create the boitier in TrackCar microservice
        // Save the boitier in the database
        
        // Save the device in the database
        DeviceDTO deviceDTO = DeviceDTO.builder()
                .deviceMicroserviceId(request.getDeviceMicroserviceId())
                .imei(request.getImei())
                .type(request.getDeviceType())
                .build();
        Device device = deviceRepository.save(deviceDTO.toEntity());
        // Save the Card sim in the database
        SimDTO simDTO = SimDTO.builder()
                .simMicroserviceId(request.getSimMicroserviceId())
                .phoneNumber(request.getPhoneNumber())
                .type(request.getSimType())
                .build();
        Sim sim = simRepository.save(simDTO.toEntity());
        // Save the subscription in the database

        // Change the device status in stock microservice to pending behind the scenes
        // using thread
        // Change the card sim status in stock microservice to pending behind the scenes
        // using thread
        // Return the response

        return null;
    }
}
