package be.kuleuven.distributedsystems.cloud.entities;

import com.google.cloud.firestore.annotation.PropertyName;

import java.io.Serializable;
import java.util.UUID;

public class Ticket implements Serializable {
    private String trainCompany;
    private UUID trainId;
    private UUID seatId;
    private UUID ticketId;
    private String customer;
    private String bookingReference;

    public Ticket() {
    }

    public Ticket(String trainCompany, UUID trainId, UUID seatId, UUID ticketId, String customer, String bookingReference) {
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.seatId = seatId;
        this.ticketId = ticketId;
        this.customer = customer;
        this.bookingReference = bookingReference;
    }

    @PropertyName("trainId")
    public void setTrainIdFromString(String trainId) {
        this.trainId = UUID.fromString(trainId);
    }

    @PropertyName("seatId")
    public void setSeatIdFromString(String seatId) {
        this.seatId = UUID.fromString(seatId);
    }

    @PropertyName("ticketId")
    public void setTicketIdFromString(String ticketId) {
        this.ticketId = UUID.fromString(ticketId);
    }

    public String getTrainCompany() {
        return trainCompany;
    }

    public UUID getTrainId() {
        return trainId;
    }

    public UUID getSeatId() {
        return this.seatId;
    }

    public UUID getTicketId() {
        return this.ticketId;
    }

    public String getCustomer() {
        return this.customer;
    }

    public String getBookingReference() {
        return this.bookingReference;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Ticket)) {
            return false;
        }
        var other = (Ticket) o;
        return this.trainCompany.equals(other.trainCompany)
                && this.trainId.equals(other.trainId)
                && this.seatId.equals(other.seatId);
    }

    @Override
    public int hashCode() {
        return this.trainCompany.hashCode() * this.trainId.hashCode() * this.seatId.hashCode();
    }
}
