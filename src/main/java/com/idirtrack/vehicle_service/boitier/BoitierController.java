package com.idirtrack.vehicle_service.boitier;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.idirtrack.vehicle_service.basic.BasicException;
import com.idirtrack.vehicle_service.basic.BasicResponse;
import com.idirtrack.vehicle_service.boitier.https.BoitierRequest;
import com.idirtrack.vehicle_service.utils.ValidationUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vehicle-api/boitier")
@RequiredArgsConstructor
public class BoitierController {

    @Autowired
    private BoitierService boitierService;

    @PostMapping("/")
    public ResponseEntity<BasicResponse> createBoitier(@Valid @RequestBody BoitierRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {

            Map<String, String> errors = new HashMap<>();

            for (FieldError error : bindingResult.getFieldErrors()) {
                String field = error.getField();

                // Filter errors for device, sim, startDate, and endDate
                if (field.equals("deviceMicroserviceId") || field.equals("imei") || field.equals("deviceType")) {
                    errors.put("device", error.getDefaultMessage());
                } else if (field.equals("simMicroserviceId") || field.equals("phone") || field.equals("ccid")
                        || field.equals("operatorName")) {
                    errors.put("sim", error.getDefaultMessage());
                } else if (field.equals("startDate")) {
                    errors.put("dateStart", error.getDefaultMessage());
                } else if (field.equals("endDate")) {
                    errors.put("dateEnd", error.getDefaultMessage());
                }
            }

            BasicResponse response = BasicResponse.builder().messageObject(errors).build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            BasicResponse response = boitierService.createNewBoitier(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BasicException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getResponse());
        }
    }

    @GetMapping("/")
    public ResponseEntity<BasicResponse> getAllBoitiers(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {
        BasicResponse response = boitierService.getAllBoitiers(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BasicResponse> deleteBoitier(@PathVariable Long id) {
        try {
            BasicResponse response = boitierService.deleteBoitierById(id);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (BasicException e) {
            return ResponseEntity.status(e.getResponse().getStatus()).body(e.getResponse());
        }

    }
}
