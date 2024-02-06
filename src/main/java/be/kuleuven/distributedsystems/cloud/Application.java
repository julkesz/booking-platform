package be.kuleuven.distributedsystems.cloud;

import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import be.kuleuven.distributedsystems.cloud.entities.TrainTime;
import be.kuleuven.distributedsystems.cloud.model.response.InternalTrainResponse;

import be.kuleuven.distributedsystems.cloud.repository.TrainRepository;
import be.kuleuven.distributedsystems.cloud.repository.TrainTimeRepository;
import be.kuleuven.distributedsystems.cloud.utils.Utils;
import com.google.gson.stream.JsonReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebClientConfigurer;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
public class Application {
    private final TrainRepository trainRepository;

    private final TrainTimeRepository trainTimeRepository;

    @Autowired
    public Application(TrainRepository trainRepository, TrainTimeRepository trainTimeRepository) {
        this.trainRepository = trainRepository;
        this.trainTimeRepository = trainTimeRepository;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws IOException {
        System.setProperty("server.port", System.getenv().getOrDefault("PORT", "8080"));

        ApplicationContext context = SpringApplication.run(Application.class, args);

    }

    /*
     * You can use this builder to create a Spring WebClient instance which can be used to make REST-calls.
     */
    @Bean
    WebClient.Builder webClientBuilder(HypermediaWebClientConfigurer configurer) {
        return configurer.registerHypermediaTypes(WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024)));
    }

    @Bean
    HttpFirewall httpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    @PostConstruct
    private void loadDataFile() {
        try {
            JsonReader jsonReader = new JsonReader(new InputStreamReader(Utils.DATA_FILE_STREAM));
            InternalTrainResponse response = Utils.GSON.fromJson(jsonReader, InternalTrainResponse.class);

            response.getTrains().forEach(internalTrain -> {
                String name = internalTrain.getName();
                String location = internalTrain.getLocation();
                String image = internalTrain.getImage();
                UUID id = UUID.randomUUID();

                Train trainDatabase = trainRepository.getTrainByName(name);
                if (trainDatabase == null) {
                    Train newTrain = new Train(name, id, name, location, image, new ArrayList<>());
                    List<Seat> seats = internalTrain.getSeats();

                    Map<LocalDateTime, List<Seat>> groupedSeats = seats.stream()
                            .collect(Collectors.groupingBy(Seat::getTime));

                    groupedSeats.forEach((trainTime, seatList) -> {
                        List<Seat> newSeats = new ArrayList<>(seatList.size());
                        seatList.forEach(seat -> {
                            Seat newSeat = new Seat(name, id, UUID.randomUUID(), seat.getTime(), seat.getType(), seat.getName(), seat.getPrice(), Seat.Status.AVAILABLE);
                            newSeats.add(newSeat);
                        });
                        TrainTime newTrainTime = new TrainTime(UUID.randomUUID(), newTrain.getName(), newTrain.getTrainId(), trainTime, newSeats);
                        trainTimeRepository.addTrainTime(newTrainTime);
                        newTrain.addTrainTime(newTrainTime.getTime());
                    });
                    trainRepository.addTrain(newTrain);
                }
            });

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
