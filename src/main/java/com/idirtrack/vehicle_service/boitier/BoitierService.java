package com.idirtrack.vehicle_service.boitier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.idirtrack.vehicle_service.basic.BasicException;
import com.idirtrack.vehicle_service.basic.BasicResponse;
import com.idirtrack.vehicle_service.basic.MetaData;
import com.idirtrack.vehicle_service.boitier.dto.BoitierDTO;
import com.idirtrack.vehicle_service.boitier.https.BoitierRequest;
import com.idirtrack.vehicle_service.device.Device;
import com.idirtrack.vehicle_service.device.DeviceRepository;
import com.idirtrack.vehicle_service.sim.Sim;
import com.idirtrack.vehicle_service.sim.SimRepository;
import com.idirtrack.vehicle_service.subscription.Subscription;
import com.idirtrack.vehicle_service.subscription.SubscriptionRepository;


@Service
public class BoitierService {

        @Autowired
        private BoitierRepository boitierRepository;
        @Autowired
        private DeviceRepository deviceRepository;
        @Autowired
        private SimRepository simRepository;
        @Autowired
        private SubscriptionRepository subscriptionRepository;
        /*
         * Create new boitier
         */
        public BasicResponse createNewBoitier(BoitierRequest request) throws BasicException {
                // Check if the device already exists in the database
                if (deviceRepository.existsByDeviceMicroserviceId(request.getDeviceMicroserviceId())) {
                        BasicResponse response = BasicResponse.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .message("Device already used in other boitier")
                                        .build();
                        throw new BasicException(response);
                }

                // Check if the sim exists in the database
                if (simRepository.existsBySimMicroserviceId(request.getSimMicroserviceId())) {
                        BasicResponse response = BasicResponse.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .message("Sim already used in other boitier")
                                        .build();
                        throw new BasicException(response);
                }
                // Check if the start date is before the end date
                if (request.getStartDate().after(request.getEndDate())) {
                        BasicResponse response = BasicResponse.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .message("Start date must be before the end date")
                                        .build();
                        throw new BasicException(response);
                }
                // Check if the start date is before the current date
                if (request.getStartDate().before(new java.util.Date())) {
                        BasicResponse response = BasicResponse.builder()
                                        .status(HttpStatus.BAD_REQUEST)
                                        .message("Start date must be after the current date")
                                        .build();
                        throw new BasicException(response);
                }
                // Save the device in the database
                Device device = Device.builder()
                                .deviceMicroserviceId(request.getDeviceMicroserviceId())
                                .imei(request.getImei())
                                .type(request.getDeviceType())
                                .build();
                device = deviceRepository.save(device);

                // Save the sim in the database
                Sim sim = Sim.builder()
                                .simMicroserviceId(request.getSimMicroserviceId())
                                .phoneNumber(request.getPhoneNumber())
                                .type(request.getSimType())
                                .build();
                sim = simRepository.save(sim);

                // Save the boitier in the database
                Boitier boitier = Boitier.builder()
                                .device(device)
                                .sim(sim)
                                .build();
                boitier = boitierRepository.save(boitier);

                // Save the subscription in the database
                Subscription subscription = Subscription.builder()
                                .startDate(request.getStartDate())
                                .endDate(request.getEndDate())
                                .boitier(boitier)
                                .build();
                subscription = subscriptionRepository.save(subscription);

                // Create DTOs for the response
                BoitierDTO boitierDTO = BoitierDTO.builder()
                                .id(boitier.getId())
                                .device(device.toDTO())
                                .sim(sim.toDTO())
                                .subscription(subscription.toDTO())
                                .build();

                // Return the response
                return BasicResponse.builder()
                                .data(boitierDTO)
                                .status(HttpStatus.CREATED)
                                .message("Boitier created successfully")
                                .build();

        }

        /*
         * Service to get all boitiers with pagination
         */
        public BasicResponse getAllBoitiers(int page, int size) {
                // Créer la pagination
                Pageable pageRequest = PageRequest.of(page - 1, size);

                // Récupérer tous les boîtiers de la base de données
                Page<Boitier> boitierPage = boitierRepository.findAll(pageRequest);

                // Créer une liste de DTOs pour les boîtiers
                List<BoitierDTO> boitierDTOs = boitierPage.getContent().stream()
                                .map(boitier -> BoitierDTO.builder()
                                                .id(boitier.getId())
                                                .device(boitier.getDevice().toDTO())
                                                .sim(boitier.getSim().toDTO())
                                                .build())
                                .collect(Collectors.toList());

                MetaData metaData = MetaData.builder()
                                .currentPage(boitierPage.getNumber() + 1)
                                .totalPages(boitierPage.getTotalPages())
                                .size(boitierPage.getSize())
                                .build();

                Map<String, Object> data = new HashMap<>();
                data.put("boitiers", boitierDTOs);
                data.put("metadata", metaData);

                return BasicResponse.builder()
                                .data(data)
                                .status(HttpStatus.OK)
                                .message("Boitiers retrieved successfully")
                                .build();
        }
        // Service: Get boitier by id
        // Service: Update boitier by id
        // Service: Delete boitier by id
        public BasicResponse deleteBoitierById(Long id) throws BasicException {
                // Check if the boitier exists in the database
                if (!boitierRepository.existsById(id)) {
                        BasicResponse response = BasicResponse.builder()
                                        .status(HttpStatus.NOT_FOUND)
                                        .message("Boitier not found")
                                        .build();
                        throw new BasicException(response);
                }

                // Change the status of device in stock microservice to available
                // Change the status of sim in stock microservice to available
                /*
                 * ToDO:
                 * 1. Call the stock microservice to change the status of the device to available
                 * 2. Call the stock microservice to change the status of the sim to available
                 * 3. Check if the device and the sim related to the boitier are deleted also from the database
                 */

                // Delete the boitier from the database
                boitierRepository.deleteById(id);

                // Return the response
                return BasicResponse.builder()
                                .status(HttpStatus.OK)
                                .message("Boitier deleted successfully")
                                .build();
        }
        /*
         * Service for search boitiers
         */

}
