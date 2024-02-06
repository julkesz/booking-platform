package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.auth.SecurityFilter;
import be.kuleuven.distributedsystems.cloud.entities.*;
import be.kuleuven.distributedsystems.cloud.model.request.PubSubDataRequest;
import be.kuleuven.distributedsystems.cloud.pubsub.PubSubConfig;
import be.kuleuven.distributedsystems.cloud.repository.BookingRepository;
import be.kuleuven.distributedsystems.cloud.repository.UserRepository;
import be.kuleuven.distributedsystems.cloud.model.request.ConfirmQuoteRequest;
import be.kuleuven.distributedsystems.cloud.service.ExternalTrainService;
import be.kuleuven.distributedsystems.cloud.service.InternalTrainService;
import be.kuleuven.distributedsystems.cloud.utils.Utils;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class TrainController {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PubSubConfig pubSubConfig;
    private final ExternalTrainService externalTrainService;
    private final InternalTrainService internalTrainService;

    @Autowired
    public TrainController(BookingRepository bookingRepository, UserRepository userRepository, PubSubConfig pubSubConfig, ExternalTrainService externalTrainService, InternalTrainService internalTrainService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.pubSubConfig = pubSubConfig;
        this.externalTrainService = externalTrainService;
        this.internalTrainService = internalTrainService;
    }

    @GetMapping("/api/getTrains")
    public ResponseEntity<Set<Train>> getTrains() {
        Set<Train> allTrains = new HashSet<>();
        allTrains.addAll(externalTrainService.getTrains());
        allTrains.addAll(internalTrainService.getTrains());
        if (allTrains.isEmpty())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().body(allTrains);
    }

    @GetMapping("/api/getTrain")
    public ResponseEntity<Train> getTrain(@RequestParam String trainCompany, @RequestParam String trainId) {
        Train responseBody;
        if (Utils.EXTERNAL_TRAIN_COMPANIES.contains(trainCompany))
            responseBody = externalTrainService.getTrain(trainCompany, trainId);
        else
            responseBody = internalTrainService.getTrain(trainCompany, trainId);

        if (responseBody == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping("/api/getTrainTimes")
    public ResponseEntity<List<LocalDateTime>> getTrainTimes(@RequestParam String trainCompany, @RequestParam String trainId) {
        List<LocalDateTime> timeList;
        if (Utils.EXTERNAL_TRAIN_COMPANIES.contains(trainCompany)) {
            timeList = externalTrainService.getTrainTimes(trainCompany, trainId);
        } else {
            timeList = internalTrainService.getTrainTimes(trainCompany, trainId);
        }
        if (timeList == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().body(timeList);
    }

    @RequestMapping(value = "/api/getAvailableSeats")
    public ResponseEntity<Map<String, Set<Seat>>> getAvailableSeats(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String time) {
        List<Seat> response;
        if (Utils.EXTERNAL_TRAIN_COMPANIES.contains(trainCompany))
            response = externalTrainService.getAvailableSeats(trainCompany, trainId, time);
        else
            response = internalTrainService.getAvailableSeats(trainCompany, trainId, time);

        if (response == null)
            return ResponseEntity.notFound().build();

        Map<String, Set<Seat>> seats = response
                .stream()
                .collect(Collectors.groupingBy(Seat::getType, Collectors.toSet()));

        return ResponseEntity.ok().body(seats);
    }

    @GetMapping("/api/getSeat")
    public ResponseEntity<Seat> getSeat(@RequestParam String trainCompany, @RequestParam String trainId, @RequestParam String seatId) {
        Seat responseBody;
        if (Utils.EXTERNAL_TRAIN_COMPANIES.contains(trainCompany))
            responseBody = externalTrainService.getSeat(trainCompany, trainId, seatId);
        else
            responseBody = internalTrainService.getSeat(trainCompany, trainId, seatId);

        if (responseBody == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok().body(responseBody);
    }

    @PostMapping(value = "/api/confirmQuotes")
    public ResponseEntity<?> confirmQuotes(@RequestBody List<ConfirmQuoteRequest> confirmQuotesRequest) {
        User user = SecurityFilter.getUser();
        PubSubDataRequest pubSubDataRequest = new PubSubDataRequest(user.getEmail(), LocalDateTime.now(), confirmQuotesRequest);
        String messageData = Utils.GSON.toJson(pubSubDataRequest);

        PubsubMessage message = PubsubMessage.newBuilder()
                .setData(ByteString.copyFromUtf8(messageData))
                .build();

        Publisher publisher = pubSubConfig.getPublisher();
        if (publisher != null) {
            publisher.publish(message);
            return ResponseEntity.status(204).build();
        }

        return ResponseEntity.status(500).build();
    }

    @GetMapping(value = "/api/getBookings")
    public ResponseEntity<List<Booking>> getBookings() {
        String user = SecurityFilter.getUser().getEmail();

        for (int i = 0; i < Utils.RETRY_COUNT; i++) {
            List<Booking> bookingList = bookingRepository.getCustomerBookings(user).stream().sorted(Comparator.comparing(Booking::getTime)).toList();
            if (!bookingList.isEmpty())
                return ResponseEntity.ok().body(bookingList);
        }
        return ResponseEntity.ok().body(Collections.emptyList());
    }

    @GetMapping(value = "/api/getAllBookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        User user = SecurityFilter.getUser();
        if (user.isManager()) {
            for (int i = 0; i < Utils.RETRY_COUNT; i++) {
                List<Booking> bookings = bookingRepository.getAllBookings().stream().toList();
                if (!bookings.isEmpty())
                    return ResponseEntity.ok().body(bookings);
            }
        }
        return ResponseEntity.status(403).build();
    }

    @GetMapping(value = "/api/getBestCustomers")
    public ResponseEntity<Set<User>> getBestCustomers() {
        User user = SecurityFilter.getUser();
        if (!user.isManager()){
            return ResponseEntity.status(403).build();
        }

        var orderedCustomers = userRepository.getAllUsers().stream().sorted(Comparator.comparing(User::getTicketCount).reversed()).toList();
        Integer highestTicketCount = orderedCustomers.get(0).getTicketCount();

        return ResponseEntity.ok().body(userRepository.getUsersByTicketCount(highestTicketCount));
    }
}
