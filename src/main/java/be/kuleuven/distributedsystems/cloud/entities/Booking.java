package be.kuleuven.distributedsystems.cloud.entities;

import com.google.cloud.firestore.annotation.PropertyName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Booking implements Serializable {
    private UUID id;
    private LocalDateTime time;
    private List<Ticket> tickets;
    private String customer;

    public Booking() {
    }

    public Booking(UUID id, LocalDateTime time, List<Ticket> tickets, String customer) {
        this.id = id;
        this.time = time;
        this.tickets = tickets;
        this.customer = customer;
    }

    @PropertyName("id")
    public void setIdFromString(String id) {
        this.id = UUID.fromString(id);
    }

    @PropertyName("time")
    public void setTimeFromString(String time) {
        this.time = LocalDateTime.parse(time);
    }

    public UUID getId() {
        return this.id;
    }

    public LocalDateTime getTime() {
        return this.time;
    }

    public List<Ticket> getTickets() {
        return this.tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
    }

    public String getCustomer() {
        return this.customer;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Booking)) {
            return false;
        }
        var other = (Booking) o;
        return this.tickets.equals(other.tickets)
                && this.customer.equals(other.customer);
    }

    @Override
    public int hashCode() {
        return this.tickets.hashCode() * this.customer.hashCode();
    }
}
