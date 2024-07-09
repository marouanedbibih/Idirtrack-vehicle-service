package com.idirtrack.vehicle_service.vehicle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehcileController {
    
    @Autowired
    private VehicleService vehicleService;
    
}
