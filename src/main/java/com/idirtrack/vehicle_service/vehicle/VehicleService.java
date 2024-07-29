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
import com.idirtrack.vehicle_service.boitier.dto.BoitierDTO;
import com.idirtrack.vehicle_service.client.Client;
import com.idirtrack.vehicle_service.client.ClientDTO;
import com.idirtrack.vehicle_service.client.ClientRepository;
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

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    public BasicResponse createNewVehicle(VehicleRequest request) throws BasicException {
        // Verify if the vehicle does not already exist by matricule
        this.verifyVehicleDoesNotExist(request.getMatricule());
        // Verify if the client exists in the database, if not, check if it exists in
        // the user microservice
        Client client = verifyOrRegisterClient(request);
        // Save the vehicle in database
        Vehicle vehicle = saveVehicleInDatabase(request, client);
        // Atache the vehicle to boities
        List<Boitier> boitiersList = attachBoitierToVehicle(vehicle, request.getBoitiersIds());

        // Transform the List<Boitier> to List<BoitierDTO>
        List<BoitierDTO> boitiersDTOList = Boitier.transformToDTOList(boitiersList);

        // Stock Algorithm in the stock microservice
        // Save the boitier in TracCar microservice

        // VehicleResponse vehicleResponse = VehicleResponse.builder()
        // .vehicle(vehicle.toDTO())
        // .client(client.toDTO())
        // .boitiersList(boitiersDTOList)
        // .build();
        return BasicResponse.builder()
                // .data(vehicleResponse)
                .message("Vehicle created successfully")
                .messageType(MessageType.SUCCESS)
                .status(HttpStatus.OK)
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
    public void verifyVehicleDoesNotExist(String matricule) throws BasicException {
        if (vehicleRepository.existsByMatricule(matricule)) {
            throw new BasicException(BasicResponse.builder()
                    .message("Vehicle already exists")
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
    }

    // Verify if the client exists in the database, if not, check if it exists in
    // the user microservice
    public Client verifyOrRegisterClient(VehicleRequest request) throws BasicException {
        if (!clientRepository.existsByClientMicroserviceId(request.getClientMicroserviceId())) {
            // Boolean result =
            // this.checkIfClientExistsInUserMicroservice(request.getUserMicroserviceId());
            // if (!result) {
            // throw new BasicException(BasicResponse.builder()
            // .message("Client does not exist")
            // .messageType(MessageType.ERROR)
            // .status(HttpStatus.BAD_REQUEST)
            // .build());
            // }
            return this.saveClient(request);
        } else {
            return clientRepository.findByClientMicroserviceId(request.getClientMicroserviceId());
        }
    }

    public Client saveClient(VehicleRequest request) {
        Client client = Client.builder()
                .clientMicroserviceId(request.getClientMicroserviceId())
                .name(StringUtils.capitalize(request.getClientName()))
                .build();
        return clientRepository.save(client);
    }

    private Vehicle saveVehicleInDatabase(VehicleRequest request, Client client) {
        Vehicle vehicle = Vehicle.builder()
                .matricule(request.getMatricule())
                .client(client)
                .type(request.getType())
                .build();
        vehicle = vehicleRepository.save(vehicle);
        return vehicle;
    }

    public Boolean checkIfClientExistsInUserMicroservice(Long userMicroserviceId) {
        return webClientBuilder.build()
                .get()
                .uri("http://user-service/api/client/check-if-exists/" + userMicroserviceId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

}
