package com.idirtrack.vehicle_service.vehicle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.idirtrack.vehicle_service.basic.BasicException;
import com.idirtrack.vehicle_service.basic.MessageType;
import com.idirtrack.vehicle_service.client.Client;
import com.idirtrack.vehicle_service.client.ClientRepository;
import com.idirtrack.vehicle_service.vehicle.https.VehicleRequest;

import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class VehicleServiceTest {

    @InjectMocks
    private VehicleService vehicleService;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vehicleService = Mockito.spy(new VehicleService());

    }

    // Vehicle Exists Tests
    @Test
    void verifyVehicleDoesNotExist_WhenVehicleDoesNotExist() {
        String matricule = "123ABC";
        when(vehicleRepository.existsByMatricule(matricule)).thenReturn(false);

        assertDoesNotThrow(() -> vehicleService.verifyVehicleDoesNotExist(matricule));
    }

    @Test
    void verifyVehicleDoesNotExist_WhenVehicleExists() {
        String matricule = "123ABC";
        when(vehicleRepository.existsByMatricule(matricule)).thenReturn(true);

        BasicException thrown = assertThrows(BasicException.class,
                () -> vehicleService.verifyVehicleDoesNotExist(matricule));

        assertEquals("Vehicle already exists", thrown.getResponse().getMessage());
        assertEquals(MessageType.ERROR, thrown.getResponse().getMessageType());
        assertEquals(HttpStatus.BAD_REQUEST, thrown.getResponse().getStatus());
    }

    @Test
    public void testSaveClient() {
        // Mock request
        VehicleRequest request = VehicleRequest.builder()
                .matricule("123ABC")
                .userMicroserviceId((long) 12222L)
                .clientName("John Doe")
                .type("Car")
                .build();

        // Mock repository method
        when(clientRepository.save(any())).thenReturn(new Client());

        // Test method
        Client client = vehicleService.saveClient(request);

        // Verify assertions
        assertNotNull(client);
    }

    // @Test
    // void verifyOrRegisterClient_ClientDoesNotExistAnywhere() throws BasicException {
    //     // VehicleRequest request = new VehicleRequest();
    //     // request.setUserMicroserviceId((long) 12222L);

    //     request = VehicleRequest.builder()
    //             .matricule("123ABC")
    //             .userMicroserviceId((long) 12222L)
    //             .clientName("John Doe")
    //             .type("Car")
    //             .build();

    //     when(clientRepository.existsByUserMicroserviceId(anyLong())).thenReturn(false);
    //     doReturn(false).when(vehicleService).checkIfClientExistsInUserMicroservice(anyLong());

    //     BasicException thrown = assertThrows(BasicException.class,
    //             () -> vehicleService.verifyOrRegisterClient(request));

    //     assertEquals("Client does not exist", thrown.getResponse().getMessage());
    //     assertEquals(MessageType.ERROR, thrown.getResponse().getMessageType());
    //     assertEquals(HttpStatus.BAD_REQUEST, thrown.getResponse().getStatus());
    // }

    // @Test
    // public void testVerifyOrRegisterClientThrowsBasicException() {
    //     VehicleService vehicleService = new VehicleService();
    //     VehicleRequest request = new VehicleRequest(); // Assurez-vous que ceci est correctement initialisé

    //     // Teste si la méthode verifyOrRegisterClient lance une BasicException
    //     assertThrows(BasicException.class, () -> vehicleService.verifyOrRegisterClient(request));
    // }

    // @Test
    // void saveVehicleInDatabaseTest() {
    // VehicleRequest request = new VehicleRequest("123ABC");
    // Client client = new Client(1L);
    // Vehicle vehicle = new Vehicle("123ABC", client);
    // when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

    // Vehicle result = vehicleService.saveVehicleInDatabase(request, client);

    // assertEquals("123ABC", result.getMatricule());
    // assertEquals(client, result.getClient());
    // }

    // @Test
    // void checkIfClientExistsInUserMicroserviceTest() {
    // Long userMicroserviceId = 1L;
    // WebClient webClientMock = Mockito.mock(WebClient.class);
    // WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock =
    // Mockito.mock(WebClient.RequestHeadersUriSpec.class);
    // WebClient.RequestHeadersSpec requestHeadersSpecMock =
    // Mockito.mock(WebClient.RequestHeadersSpec.class);
    // WebClient.ResponseSpec responseSpecMock =
    // Mockito.mock(WebClient.ResponseSpec.class);

    // when(webClientBuilder.build()).thenReturn(webClientMock);
    // when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
    // when(requestHeadersUriSpecMock.uri(any(String.class))).thenReturn(requestHeadersSpecMock);
    // when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
    // when(responseSpecMock.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));

    // Boolean exists =
    // vehicleService.checkIfClientExistsInUserMicroservice(userMicroserviceId);

    // assertTrue(exists);
    // }
}