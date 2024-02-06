package be.kuleuven.distributedsystems.cloud.service;

import be.kuleuven.distributedsystems.cloud.entities.*;
import be.kuleuven.distributedsystems.cloud.model.request.ConfirmQuoteRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public interface TrainService {
    Set<Train> getTrains();
    Train getTrain(String trainCompany, String trainId);
    List<LocalDateTime> getTrainTimes(String trainCompany, String trainId);
    List<Seat> getAvailableSeats(String trainCompany, String trainId, String time);
    Seat getSeat(String trainCompany, String trainId, String seatId);

    Ticket bookTicket(ConfirmQuoteRequest confirmQuoteRequest, String customer, String bookingReference);

    void unbookTicket(Ticket ticket);
}
