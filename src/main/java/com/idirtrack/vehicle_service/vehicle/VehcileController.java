package com.idirtrack.vehicle_service.vehicle;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.vehicle_service.basic.BasicException;
import com.idirtrack.vehicle_service.basic.BasicResponse;
import com.idirtrack.vehicle_service.utils.ValidationUtil;
import com.idirtrack.vehicle_service.vehicle.https.VehicleRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vehicles")
public class VehcileController {

    @Autowired
    private VehicleService vehicleService;

    @PostMapping("/")
    public ResponseEntity<BasicResponse> createNewVehicle(@Valid @RequestBody VehicleRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = ValidationUtil.getValidationsErrors(bindingResult);
            BasicResponse response = BasicResponse.builder()
                    .messageObject(errors)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            BasicResponse response = vehicleService.createNewVehicle(request);
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }
    }

    @GetMapping("/")
    public ResponseEntity<BasicResponse> getAllVehicles(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok().body(vehicleService.getAllVehicles(page, size));
        // BasicResponse response = vehicleService.getAllVehicles();
        // return ResponseEntity.status(response.getStatus()).body(response);
    }
}
