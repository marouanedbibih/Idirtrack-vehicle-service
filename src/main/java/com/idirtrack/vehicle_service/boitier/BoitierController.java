package com.idirtrack.vehicle_service.boitier;

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
import com.idirtrack.vehicle_service.boitier.https.BoitierRequest;
import com.idirtrack.vehicle_service.utils.ValidationUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boitier")
@RequiredArgsConstructor
public class BoitierController {

    @Autowired
    private BoitierService boitierService;

    @PostMapping("/")
    public ResponseEntity<BasicResponse> createBoitier(@Valid @RequestBody BoitierRequest request,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = ValidationUtil.getValidationsErrors(bindingResult);
            BasicResponse response = BasicResponse.builder()
                    .messageObject(errors)
                    .build();
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
}
