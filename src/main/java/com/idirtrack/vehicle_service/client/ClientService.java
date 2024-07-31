package com.idirtrack.vehicle_service.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Basic;
import com.idirtrack.vehicle_service.basic.BasicResponse;
import com.idirtrack.vehicle_service.vehicle.https.VehicleRequest;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Check if a client exists in the user microservice
     * @param id
     * @param clientName
     * @param companyName
     * @return Boolean
     */
    public Boolean isExistInUserMicroservice(Long id, String clientName, String companyName) {
        // Construct the URI with query parameters
        String uri = String.format("/exist-for-create-vehicle/?clientId=%d&clientName=%s&companyName=%s",
                id, clientName, companyName);

        // Send request to user microservice to check if the client exists
        BasicResponse response = webClientBuilder.build()
                .get()
                .uri("http://user-service/user-api/clients" + uri)
                .retrieve()
                .bodyToMono(BasicResponse.class)
                .block();

        Boolean exist = (Boolean) response.getContent();

        return exist;
    }

    /**
     * Save client in database from Vehicle Request
     * 
     * @param request
     * @return Client
     */
    public Client saveClient(VehicleRequest request) throws Exception {
        try {
            Client client = Client.builder()
                    .clientMicroserviceId(request.getClientMicroserviceId())
                    .name(StringUtils.capitalize(request.getClientName()))
                    .company(request.getClientCompany())
                    .build();
            return clientRepository.save(client);
        } catch (Exception e) {
            throw new Exception("Error while saving client");
        }

    }

}
