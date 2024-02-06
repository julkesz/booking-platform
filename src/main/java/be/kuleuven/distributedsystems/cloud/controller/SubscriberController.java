package be.kuleuven.distributedsystems.cloud.controller;

import be.kuleuven.distributedsystems.cloud.entities.*;
import be.kuleuven.distributedsystems.cloud.exceptions.DuplicateEntity;
import be.kuleuven.distributedsystems.cloud.model.request.ConfirmQuoteRequest;
import be.kuleuven.distributedsystems.cloud.model.request.PubSubDataRequest;
import be.kuleuven.distributedsystems.cloud.model.request.PubSubMessageRequest;
import be.kuleuven.distributedsystems.cloud.repository.BookingRepository;
import be.kuleuven.distributedsystems.cloud.repository.TrainTimeRepository;
import be.kuleuven.distributedsystems.cloud.repository.UserRepository;
import be.kuleuven.distributedsystems.cloud.service.EmailService;
import be.kuleuven.distributedsystems.cloud.service.ExternalTrainService;
import be.kuleuven.distributedsystems.cloud.service.InternalTrainService;
import be.kuleuven.distributedsystems.cloud.utils.Utils;
import com.google.gson.reflect.TypeToken;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class SubscriberController {
    private final TrainTimeRepository trainTimeRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ExternalTrainService externalTrainService;
    private final InternalTrainService internalTrainService;
    private final EmailService emailService;


    @Autowired
    public SubscriberController(BookingRepository bookingRepository, UserRepository userRepository, ExternalTrainService externalTrainService, InternalTrainService internalTrainService, TrainTimeRepository trainTimeRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.trainTimeRepository = trainTimeRepository;
        this.externalTrainService = externalTrainService;
        this.internalTrainService = internalTrainService;
        this.emailService = emailService;
    }

    @PostMapping("/subscription")
    public ResponseEntity<?> subscription(@RequestBody String message) {
        PubSubMessageRequest pubSubMessageRequest = Utils.GSON.fromJson(message, new TypeToken<PubSubMessageRequest>() {}.getType());
        byte[] decodedBytes = Base64.getDecoder().decode(pubSubMessageRequest.getMessage().getData());
        String decodedString = new String(decodedBytes);

        PubSubDataRequest pubSubDataRequest = Utils.GSON.fromJson(decodedString, new TypeToken<PubSubDataRequest>() {}.getType());
        User user = userRepository.getUserByEmail(pubSubDataRequest.getUser());

        if (user != null) {

            UUID bookingId = UUID.randomUUID();
            while (bookingRepository.getBookingIds().contains(bookingId))
                bookingId = UUID.randomUUID();
            UUID finalBookingId = bookingId;

            List<Ticket> tickets = new ArrayList<>();
            AtomicBoolean successfulBooking = new AtomicBoolean(true);
            Booking booking = new Booking(finalBookingId, pubSubDataRequest.getTimeStamp(), tickets, user.getEmail());

            for (ConfirmQuoteRequest confirmQuoteRequest : pubSubDataRequest.getConfirmQuotesRequest()) {
                Ticket ticket = bookTicket(confirmQuoteRequest, booking.getCustomer(), booking.getId().toString());
                if (ticket != null)
                    booking.addTicket(ticket);
                else {
                    unBookTickets(booking.getTickets());
                    successfulBooking.set(false);
                    emailService.sendBookingConfirmationEmail(booking.getCustomer(), "Failure of a new booking request", "Confirmation of the booking " + booking.getId() + " failed. At least one of the seats could not be booked. Here are the details of the request: " + "\n" + getBookingInfo(pubSubDataRequest));
                    System.out.println("Confirmation of the booking " + booking.getId() + " failed. At least one of the seats could not be booked. Here are the details of the request: " + "\n" + getBookingInfo(pubSubDataRequest));
                    break;
                }
            }

            if (successfulBooking.get())
                try {
                    booking.getTickets().forEach(ticket -> {
                        if (!Utils.EXTERNAL_TRAIN_COMPANIES.contains(ticket.getTrainCompany()))
                            trainTimeRepository.bookSeat(ticket.getTrainCompany(), ticket.getTrainId().toString(), ticket.getSeatId().toString());
                    });
                    bookingRepository.addBooking(booking);
                    emailService.sendBookingConfirmationEmail(booking.getCustomer(), "Successful completion of a new booking request", "We are happy to inform you that your booking " + booking.getId() + " is confirmed. Here are the details of your booking: "+ "\n" + getBookingInfo(pubSubDataRequest));
                    System.out.println("We are happy to inform you that your booking " + booking.getId() + " is confirmed. Here are the details of your booking: "+ "\n" + getBookingInfo(pubSubDataRequest));
                } catch (DuplicateEntity duplicateEntity) {
                    System.out.println("Booking " + booking.getId() + " cannot be added. It already exists in the database.");
                    emailService.sendBookingConfirmationEmail(booking.getCustomer(), "Failure of a new booking request", "Confirmation of the booking " + booking.getId() + " failed. The booking already exists. Here are the details of the request: "+ "\n" + getBookingInfo(pubSubDataRequest));
                    System.out.println("Confirmation of the booking " + booking.getId() + " failed. The booking already exists. Here are the details of the request: "+ "\n" + getBookingInfo(pubSubDataRequest));
                }
        }
        return ResponseEntity.status(200).build();
    }

    public Ticket bookTicket(ConfirmQuoteRequest confirmQuoteRequest, String customer, String bookingReference) {
        if (Utils.EXTERNAL_TRAIN_COMPANIES.contains(confirmQuoteRequest.getTrainCompany())) {
            return externalTrainService.bookTicket(confirmQuoteRequest, customer, bookingReference);
        } else {
            return internalTrainService.bookTicket(confirmQuoteRequest, customer, bookingReference);
        }
    }

    public void unBookTickets(List<Ticket> tickets) {
        tickets.forEach(ticket -> {
            if (Utils.EXTERNAL_TRAIN_COMPANIES.contains(ticket.getTrainCompany())){
                externalTrainService.unbookTicket(ticket);
            }
        });
    }

    public String getBookingInfo(PubSubDataRequest pubSubDataRequest){
        StringBuilder info = new StringBuilder("Request: \n" +
                "\tuser: " + pubSubDataRequest.getUser() + "\n" +
                "\ttime: " + pubSubDataRequest.getTimeStamp() + "\n" +
                "\ttickets: \n");
        for (ConfirmQuoteRequest confirmQuoteRequest : pubSubDataRequest.getConfirmQuotesRequest()) {
            Seat seatDatabase;
            Train trainDatabase;
            if (Utils.EXTERNAL_TRAIN_COMPANIES.contains(confirmQuoteRequest.getTrainCompany())){
                seatDatabase = externalTrainService.getSeat(confirmQuoteRequest.getTrainCompany(), confirmQuoteRequest.getTrainId(), confirmQuoteRequest.getSeatId());
                trainDatabase = externalTrainService.getTrain(confirmQuoteRequest.getTrainCompany(), confirmQuoteRequest.getTrainId());
            } else {
                seatDatabase = internalTrainService.getSeat(confirmQuoteRequest.getTrainCompany(), confirmQuoteRequest.getTrainId(), confirmQuoteRequest.getSeatId());
                trainDatabase = internalTrainService.getTrain(confirmQuoteRequest.getTrainCompany(), confirmQuoteRequest.getTrainId());
            }
            if (seatDatabase == null || trainDatabase == null){
                info.append(confirmQuoteRequest);
            } else {
                info.append("\tTicket: \n").append("\t\ttrain company: ").append(seatDatabase.getTrainCompany()).append("\n").append("\t\tlocation: ").append(trainDatabase.getLocation()).append("\n").append("\t\ttrain time: ").append(seatDatabase.getTime()).append("\n").append("\t\tseat: ").append(seatDatabase.getName()).append("\n");
            }
        }
        return info.toString();
    }
}