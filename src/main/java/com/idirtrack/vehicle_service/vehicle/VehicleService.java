package com.idirtrack.vehicle_service.vehicle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.idirtrack.vehicle_service.basic.BasicException;
import com.idirtrack.vehicle_service.basic.BasicResponse;
import com.idirtrack.vehicle_service.basic.MessageType;
import com.idirtrack.vehicle_service.basic.MetaData;
import com.idirtrack.vehicle_service.boitier.Boitier;
import com.idirtrack.vehicle_service.boitier.BoitierRepository;
import com.idirtrack.vehicle_service.boitier.BoitierService;
import com.idirtrack.vehicle_service.boitier.dto.BoitierDTO;
import com.idirtrack.vehicle_service.client.Client;
import com.idirtrack.vehicle_service.client.ClientDTO;
import com.idirtrack.vehicle_service.client.ClientRepository;
import com.idirtrack.vehicle_service.client.ClientService;
import com.idirtrack.vehicle_service.traccar.TracCarService;
import com.idirtrack.vehicle_service.vehicle.https.VehicleRequest;
import com.idirtrack.vehicle_service.vehicle.https.VehicleResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice.This;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private BoitierRepository boitierRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private BoitierService boitierService;

    @Autowired
    private TracCarService tracCarService;

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    public BasicResponse createNewVehicle(VehicleRequest request) throws BasicException {
        // Verify if the vehicle does not already exist by matricule
        if (vehicleRepository.existsByMatricule(request.getMatricule())) {
            throw new BasicException(BasicResponse.builder()
                    .message("Vehicle already exists")
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.CONFLICT)
                    .build());

        }

        // Check if the client exists in User Microservice if not, throw an exception
        // if the user exist in the user microservice, check if the client exists in the
        // database
        // if the client does not exist in the database, save the client
        // if the client exists in the database, get the client
        if (!clientService.isExistInUserMicroservice(
                request.getClientMicroserviceId(),
                request.getClientName(),
                request.getClientCompany())) {
            throw new BasicException(BasicResponse.builder()
                    .message("Client does not exist in the Client Microservice")
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        } else {
            if (!clientRepository.existsByClientMicroserviceId(request.getClientMicroserviceId())) {
                try {
                    clientService.saveClient(request);
                    logger.info("Client saved successfully");
                } catch (Exception e) {
                    throw new BasicException(BasicResponse.builder()
                            .message(e.getMessage())
                            .messageType(MessageType.ERROR)
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
                }
            }
        }

        /*
         * Check if the boitiers exist in the database and are not already attached to
         * another vehicle
         * 
         * If one boitier not exist in the database, throw an exception
         * If one boitier is already attached to another vehicle, throw an exception
         */
        List<Boitier> boitiers = new ArrayList<>();
        for (Long boitierId : request.getBoitiersIds()) {

            // Check if the boitier exists in the database
            Boitier boitier = boitierRepository.findById(boitierId)
                    .orElseThrow(() -> new BasicException(BasicResponse.builder()
                            .message("Boitier not found")
                            .messageType(MessageType.ERROR)
                            .status(HttpStatus.NOT_FOUND)
                            .build()));
            // Check if the boitier is already attached to a vehicle
            if (boitier.getVehicle() != null) {
                String message = "Boitier with the phone " + boitier.getSim().getPhone() + " and device IMEI "
                        + boitier.getDevice().getImei() + " already attached to a vehicle";
                throw new BasicException(BasicResponse.builder()
                        .message(message)
                        .messageType(MessageType.WARNING)
                        .status(HttpStatus.CONFLICT)
                        .build());
            }

            // Add the boitier to the list of boitiers
            boitiers.add(boitier);
        }

        // Save the Boities in TracCar Microservice
        for (Boitier boitier : boitiers) {
            boolean isSaved = tracCarService.createDevice(
                    request.getClientName(),
                    boitier.getDevice().getImei(),
                    request.getClientCompany(),
                    request.getMatricule());
            if (!isSaved) {
                throw new BasicException(BasicResponse.builder()
                        .message("Error while saving the boitier in TracCar Microservice")
                        .messageType(MessageType.WARNING)
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build());
            }
        }

        // Attach Boitiers to the vehicle and save the vehicle in the database
        Vehicle vehicle = Vehicle.builder()
                .matricule(request.getMatricule())
                .client(clientRepository.findByClientMicroserviceId(request.getClientMicroserviceId()))
                .type(request.getType())
                .boitiers(boitiers)
                .build();

        vehicle = vehicleRepository.save(vehicle);

        // Attach vehicle to boitiers
        for (Boitier boitier : boitiers) {
            boitier.setVehicle(vehicle);
            boitierRepository.save(boitier);
        }

        return BasicResponse.builder()
                .message("Vehicle created successfully")
                .messageType(MessageType.INFO)
                .status(HttpStatus.CREATED)
                .build();
    }

    /**
     * Retrieves a paginated list of vehicles.
     * 
     * This method performs the following steps:
     * 1. Creates a pagination request using the provided page number and page size.
     * 2. Fetches a page of vehicles from the repository.
     * 3. Throws a {@link BasicException} if no vehicles are found for the specified
     * page.
     * 4. Transforms the vehicles into DTOs for the response.
     * 5. Constructs metadata for pagination, including current page, total pages,
     * and page size.
     * 6. Builds and returns a {@link BasicResponse} object containing the vehicle
     * list, metadata, and status.
     * 
     * @param page the page number (1-based index) to retrieve
     * @param size the number of vehicles per page
     * @return a {@link BasicResponse} with the list of vehicles, pagination
     *         metadata, and status
     * @throws BasicException if no vehicles are found for the specified page
     */
    public BasicResponse getAllVehicles(int page, int size) throws BasicException {

        // Create a pagination request
        Pageable pageable = PageRequest.of(page - 1, size);

        // Get Page of vehicles
        Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);

        // Throw exception if the vehicles list is empty
        if (vehicles.getContent().isEmpty()) {
            throw new BasicException(BasicResponse.builder()
                    .message("No vehicles found")
                    .messageType(MessageType.INFO)
                    .status(HttpStatus.NOT_FOUND)
                    .build());
        }

        // Build the DTO of vehicle resppnse
        List<VehicleResponse> vehiclesResponse = vehicles.getContent().stream()
                .map(vehicle -> {
                    ClientDTO clientDTO = ClientDTO.builder()
                            .id(vehicle.getClient().getId())
                            .name(vehicle.getClient().getName())
                            .company(vehicle.getClient().getCompany())
                            .build();
                    return VehicleResponse.builder()
                            .vehicle(vehicle.toDTO())
                            .client(clientDTO)
                            .build();
                })
                .collect(Collectors.toList());

        // Build the metadata object
        MetaData metaData = MetaData.builder()
                .currentPage(vehicles.getNumber() + 1)
                .totalPages(vehicles.getTotalPages())
                .size(vehicles.getSize())
                .build();

        // Build the response object
        return BasicResponse.builder()
                .content(vehiclesResponse)
                .metadata(metaData)
                .status(HttpStatus.OK)
                .build();
    }

    // Service: Get the vehicle by id and her boitiers
    public List<Boitier> attachBoitierToVehicle(Vehicle vehicle, List<Long> boitierIds) throws BasicException {
        // Boucle in boitiers for get the boitier and attach to vehicle
        List<Boitier> boitiersList = new ArrayList<>();
        for (Long boitierId : boitierIds) {
            Boitier boitier = boitierRepository.findById(boitierId)
                    .orElseThrow(() -> new BasicException(BasicResponse.builder()
                            .message("Boitier not found")
                            .messageType(MessageType.ERROR)
                            .status(HttpStatus.NOT_FOUND)
                            .build()));
            boitier.setVehicle(vehicle);
            boitier = boitierRepository.save(boitier);
            boitiersList.add(boitier);
        }
        vehicle.setBoitiers(boitiersList);

        return boitiersList;
    }

    // Verify if the vehicle does not already exist by matricule
    // public void verifyVehicleDoesNotExist(String matricule) throws BasicException
    // {
    // if (vehicleRepository.existsByMatricule(matricule)) {
    // throw new BasicException(BasicResponse.builder()
    // .message("Vehicle already exists")
    // .messageType(MessageType.ERROR)
    // .status(HttpStatus.BAD_REQUEST)
    // .build());
    // }
    // }

    // Verify if the client exists in the database, if not, check if it exists in
    // the user microservice
    // public Client verifyOrRegisterClient(VehicleRequest request) throws
    // BasicException {
    // if
    // (!clientRepository.existsByClientMicroserviceId(request.getClientMicroserviceId()))
    // {
    // // Boolean result =
    // //
    // this.checkIfClientExistsInUserMicroservice(request.getUserMicroserviceId());
    // // if (!result) {
    // // throw new BasicException(BasicResponse.builder()
    // // .message("Client does not exist")
    // // .messageType(MessageType.ERROR)
    // // .status(HttpStatus.BAD_REQUEST)
    // // .build());
    // // }
    // return this.saveClient(request);
    // } else {
    // return
    // clientRepository.findByClientMicroserviceId(request.getClientMicroserviceId());
    // }
    // }

    // private Vehicle saveVehicleInDatabase(VehicleRequest request, Client client)
    // {
    // Vehicle vehicle = Vehicle.builder()
    // .matricule(request.getMatricule())
    // .client(client)
    // .type(request.getType())
    // .build();
    // vehicle = vehicleRepository.save(vehicle);
    // return vehicle;
    // }

}
