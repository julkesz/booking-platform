package be.kuleuven.distributedsystems.cloud.service;

import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.Ticket;
import be.kuleuven.distributedsystems.cloud.entities.Train;
import be.kuleuven.distributedsystems.cloud.model.request.ConfirmQuoteRequest;
import be.kuleuven.distributedsystems.cloud.repository.TicketRepository;
import be.kuleuven.distributedsystems.cloud.repository.TrainRepository;
import be.kuleuven.distributedsystems.cloud.repository.TrainTimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class InternalTrainService implements TrainService {
    private final TrainRepository trainRepository;

    private final TrainTimeRepository trainTimeRepository;
    private final TicketRepository ticketRepository;
    @Autowired
    public InternalTrainService(TrainRepository trainRepository, TrainTimeRepository trainTimeRepository, TicketRepository ticketRepository) {
        this.trainRepository = trainRepository;
        this.trainTimeRepository = trainTimeRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Set<Train> getTrains() {
        return trainRepository.getAllTrains();
    }

    @Override
    public Train getTrain(String trainCompany, String trainId) {
        return trainRepository.getTrainById(trainCompany, trainId);
    }

    @Override
    public List<LocalDateTime> getTrainTimes(String trainCompany, String trainId) {
        return trainRepository.getTrainTimes(trainCompany, trainId);
    }

    @Override
    public List<Seat> getAvailableSeats(String trainCompany, String trainId, String time) {
        return trainTimeRepository.getAvailableSeats(trainCompany, trainId, time);
    }

    @Override
    public Seat getSeat(String trainCompany, String trainId, String seatId) {
        return trainTimeRepository.getSeat(trainCompany, trainId, seatId);
    }

    @Override
    public Ticket bookTicket(ConfirmQuoteRequest confirmQuoteRequest, String customer, String bookingReference) {
        UUID ticketId = UUID.randomUUID();
        while (ticketRepository.getTicketIds().contains(ticketId))
            ticketId  = UUID.randomUUID();
        UUID finalTicketId = ticketId;

        Ticket newTicket = new Ticket(confirmQuoteRequest.getTrainCompany(), UUID.fromString(confirmQuoteRequest.getTrainId()), UUID.fromString(confirmQuoteRequest.getSeatId()), finalTicketId, customer, bookingReference);
        if (ticketRepository.getAllTickets().contains(newTicket))
            return null;
        return newTicket;
    }

    @Override
    public void unbookTicket(Ticket ticket) {
    }
}
