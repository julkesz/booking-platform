package be.kuleuven.distributedsystems.cloud.model.response;

import java.util.UUID;

public class TicketResponse{
    public String trainCompany;
    public UUID trainId;
    public UUID seatId;
    public UUID ticketId;
    public String customer;
    public UUID bookingReference;

    public TicketResponse() {
    }

    public TicketResponse(String trainCompany, UUID trainId, UUID seatId, UUID ticketId, String customer, UUID bookingReference) {
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.seatId = seatId;
        this.ticketId = ticketId;
        this.customer = customer;
        this.bookingReference = bookingReference;
    }

    public String getTrainCompany() {
        return trainCompany;
    }

    public UUID getTrainId() { return trainId; }

    public UUID getSeatId() { return seatId; }

    public UUID getTicketId() { return ticketId; }

    public String getCustomer() {
        return customer;
    }

    public UUID getBookingReference() { return bookingReference; }
}
