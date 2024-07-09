package com.idirtrack.vehicle_service.vehicle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.idirtrack.vehicle_service.basic.BasicException;
import com.idirtrack.vehicle_service.basic.BasicResponse;
import com.idirtrack.vehicle_service.basic.MessageType;
import com.idirtrack.vehicle_service.client.Client;
import com.idirtrack.vehicle_service.client.ClientDTO;
import com.idirtrack.vehicle_service.client.ClientRepository;
import com.idirtrack.vehicle_service.vehicle.https.VehicleRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /*
     * Creates a new vehicle.
     * 
     * @param request The vehicle request containing vehicle and client details.
     * 
     * @return BasicResponse containing the saved vehicle data.
     * 
     * This method creates a new vehicle record in the database.
     * It first checks if a vehicle with the given matricule already exists; if
     * found, it throws an exception.
     * Then, it verifies if the client associated with the vehicle exists in the
     * database.
     * If the client does not exist in the database, it checks if the client exists
     * in the user microservice.
     * If the client exists in the user microservice, it saves the client to the
     * database.
     * Afterward, it saves the vehicle details to the database.
     * Finally, it returns a response containing the saved vehicle data.
     */

    public BasicResponse createVehicle(VehicleRequest request) throws BasicException {

        // Check if the vehicle does not already exist by matricule
        if (vehicleRepository.existsByMatricule(request.getMatricule())) {
            BasicResponse response = BasicResponse.builder()
                    .message("Vehicle already exists")
                    .messageType(MessageType.ERROR)
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
            throw new BasicException(response);
        }
        // Check if the client exists in the database
        if (!clientRepository.existsByUserMicroserviceId(request.getUserMicroserviceId())) {
            // Check if the client exists in the user microservice
            Boolean result = webClientBuilder.build()
                    .get()
                    .uri("http://user-service/api/client/check-if-exists/" + request.getUserMicroserviceId())
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            if (!result) {
                BasicResponse response = BasicResponse.builder()
                        .message("Client does not exist")
                        .messageType(MessageType.ERROR)
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
                throw new BasicException(response);
            } else {
                // Save the client in the database
                Client client = Client.builder()
                        .userMicroserviceId(request.getUserMicroserviceId())
                        .name(StringUtils.capitalize(request.getClientName()))
                        .build();
                client = clientRepository.save(client);
                // Save the vehicle in the database
                Vehicle vehicle = this.saveVehicleInDatabase(request, client);

                // Return the response
                return BasicResponse.builder()
                        .data(vehicle)
                        .message("Vehicle created successfully")
                        .messageType(MessageType.SUCCESS)
                        .status(HttpStatus.OK)
                        .build();
            }
        } else {
            Client client = clientRepository.findByUserMicroserviceId(request.getUserMicroserviceId());
            // Save the vehicle in the database
            Vehicle vehicle = this.saveVehicleInDatabase(request, client);
            // Return the response
            return BasicResponse.builder()
                    .data(vehicle)
                    .message("Vehicle created successfully")
                    .messageType(MessageType.SUCCESS)
                    .status(HttpStatus.OK)
                    .build();
        }
    }

    /*
     * Saves the vehicle entity in the database.
     * 
     * @param request The vehicle request containing vehicle details.
     * 
     * @param client The client associated with the vehicle.
     * 
     * @return The saved Vehicle entity.
     * 
     * This method constructs a vehicle entity using the provided request data and
     * the associated client.
     * It then persists the vehicle entity in the database.
     */
    
    private Vehicle saveVehicleInDatabase(VehicleRequest request, Client client) {
        Vehicle vehicle = Vehicle.builder()
                .matricule(request.getMatricule())
                .client(client)
                .build();
        vehicle = vehicleRepository.save(vehicle);
        return vehicle;
    }

}
