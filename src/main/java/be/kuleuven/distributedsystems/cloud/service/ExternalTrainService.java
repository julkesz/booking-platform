package be.kuleuven.distributedsystems.cloud.service;

import be.kuleuven.distributedsystems.cloud.entities.*;
import be.kuleuven.distributedsystems.cloud.model.request.ConfirmQuoteRequest;
import be.kuleuven.distributedsystems.cloud.model.response.SeatsResponse;
import be.kuleuven.distributedsystems.cloud.model.response.TicketResponse;
import be.kuleuven.distributedsystems.cloud.model.response.TimesResponse;
import be.kuleuven.distributedsystems.cloud.model.response.TrainsResponse;
import be.kuleuven.distributedsystems.cloud.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class ExternalTrainService implements TrainService {
    private final WebClient.Builder webClientBuilder;
    private static final Logger logger = LoggerFactory.getLogger(ExternalTrainService.class);

    @Autowired
    public ExternalTrainService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public Set<Train> getTrains() {
        Set<Train> allTrains = new HashSet<>();
        AtomicBoolean successful = new AtomicBoolean(false);

        for (int i = 0; i < Utils.RETRY_COUNT && !successful.get(); i++)
            Utils.EXTERNAL_TRAIN_COMPANIES.forEach(trainCompany -> {
                TrainsResponse responseBody = null;
                try {
                    String url = "https://" + trainCompany + "/trains?key=" + Utils.API_KEY;
                    responseBody = webClientBuilder
                            .build()
                            .get()
                            .uri(url)
                            .retrieve()
                            .bodyToMono(TrainsResponse.class)
                            .block();
                } catch (WebClientResponseException e) {
                    successful.set(false);
                    logger.warn("An error occurred while fetching data from " + trainCompany + ": " + e.getMessage());
                }
                if (responseBody != null) {
                    TrainsResponse.Embedded embedded = responseBody.get_embedded();
                    if (embedded != null) {
                        successful.set(true);
                        allTrains.addAll(embedded.getTrains());
                    }
                }
            });

        return allTrains;
    }

    @Override
    public Train getTrain(String trainCompany, String trainId) {
        String url = "https://" + trainCompany + "/trains/" + trainId + "?key=" + Utils.API_KEY;
        Train responseBody = null;
        boolean successful = false;

        for (int i = 0; i < Utils.RETRY_COUNT && !successful; i++)
            try {
                responseBody = webClientBuilder
                        .build()
                        .get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Train.class)
                        .block();
                successful = true;
            } catch (WebClientResponseException e) {
                logger.warn("An error occurred while fetching data from " + trainCompany + ": " + e.getMessage());
            }

        return responseBody;
    }

    @Override
    public List<LocalDateTime> getTrainTimes(String trainCompany, String trainId) {
        String url = "https://" + trainCompany + "/trains/" + trainId + "/times?key=" + Utils.API_KEY;
        List<String> stringList = null;
        boolean successful = false;

        for (int i = 0; i < Utils.RETRY_COUNT && !successful; i++)
            try {
                stringList = webClientBuilder
                        .build()
                        .get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(TimesResponse.class)
                        .block()
                        .get_embedded()
                        .getStringList();
                successful = true;
            } catch (WebClientResponseException e) {
                logger.warn("An error occurred while fetching data from " + trainCompany + ": " + e.getMessage());
            }

        List<LocalDateTime> timeList = stringList
                .stream()
                .map(str -> LocalDateTime.parse(str, Utils.DATE_TIME_FORMATTER1))
                .sorted(LocalDateTime::compareTo)
                .collect(Collectors.toList());

        return timeList;
    }

    @Override
    public List<Seat> getAvailableSeats(String trainCompany, String trainId, String time) {
        String url = "https://" + trainCompany + "/trains/" + trainId + "/seats?time=" + time + "&available=true&key=" + Utils.API_KEY;
        List<Seat> response = null;
        boolean successful = false;

        for (int i = 0; i < Utils.RETRY_COUNT && !successful; i++)
            try {
                response = webClientBuilder
                        .build()
                        .get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(SeatsResponse.class)
                        .block()
                        .get_embedded()
                        .getSeats();
                successful = true;
            } catch (WebClientResponseException e) {
                logger.warn("An error occurred while fetching data from " + trainCompany + ": " + e.getMessage());
            }

        return response;
    }

    @Override
    public Seat getSeat(String trainCompany, String trainId, String seatId) {
        String url = "https://" + trainCompany + "/trains/" + trainId + "/seats/" + seatId + "?key=" + Utils.API_KEY;
        Seat responseBody = null;
        boolean successful = false;

        for (int i = 0; i < Utils.RETRY_COUNT && !successful; i++)
            try {
                responseBody = webClientBuilder
                        .build()
                        .get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(Seat.class)
                        .block();
                successful = true;
            } catch (WebClientResponseException e) {
                logger.warn("An error occurred while fetching data from " + trainCompany + ": " + e.getMessage());
            }

        return responseBody;
    }

    @Override
    public Ticket bookTicket(ConfirmQuoteRequest confirmQuoteRequest, String customer, String bookingReference) {
        AtomicBoolean successfulOneTicket = new AtomicBoolean(false);

        TicketResponse responseBody = null;
        successfulOneTicket.set(false);
        for (int i = 0; i < Utils.RETRY_COUNT && !successfulOneTicket.get(); i++)
            try {
                String url = "https://" + confirmQuoteRequest.getTrainCompany() + "/trains/" + confirmQuoteRequest.getTrainId() + "/seats/" + confirmQuoteRequest.getSeatId() + "/ticket?customer=" + customer + "&bookingReference=" + bookingReference + "&key=" + Utils.API_KEY;

                responseBody = webClientBuilder
                        .build()
                        .put()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(TicketResponse.class)
                        .block();
                successfulOneTicket.set(true);
            } catch (WebClientResponseException e) {
                successfulOneTicket.set(false);
            }
        if (responseBody != null) {
            return new Ticket(responseBody.getTrainCompany(), responseBody.getTrainId(), responseBody.getSeatId(), responseBody.getTicketId(), responseBody.getCustomer(), responseBody.getBookingReference().toString());
        }

        return null;
    }

    @Override
    public void unbookTicket(Ticket ticket) {
        AtomicBoolean successfulDeletion = new AtomicBoolean(false);

        for (int i = 0; i < Utils.RETRY_COUNT && !successfulDeletion.get(); i++) {
            try {
                String url = "https://" + ticket.getTrainCompany() + "/trains/" + ticket.getTrainId() + "/seats/" + ticket.getSeatId() + "/ticket/" + ticket.getTicketId() + "?key=" + Utils.API_KEY;

                webClientBuilder
                        .build()
                        .delete()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(TicketResponse.class)
                        .block();

                successfulDeletion.set(true);
                logger.info("Ticket " + ticket.getTicketId() + " has been successfully deleted.");
            } catch (WebClientResponseException e) {
                successfulDeletion.set(false);
                logger.error("Ticket " + ticket.getTicketId() + " cannot be deleted due to an error: " + e.getMessage());
            }
        }
    }
}
