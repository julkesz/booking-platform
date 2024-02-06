package be.kuleuven.distributedsystems.cloud.entities;

import com.google.cloud.firestore.annotation.PropertyName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Seat implements Serializable {
    private String trainCompany;
    private UUID trainId;
    private UUID seatId;
    private LocalDateTime time;
    private String type;
    private String name;
    private double price;

    private Status status;

    public enum Status {
        AVAILABLE,
        BOOKED
    }

    public Seat() {
    }

    public Seat(String trainCompany, UUID trainId, UUID seatId, LocalDateTime time, String type, String name, double price, Status status) {
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.seatId = seatId;
        this.time = time;
        this.type = type;
        this.name = name;
        this.price = price;
        this.status = status;
    }

    @PropertyName("trainId")
    public void setTrainIdFromString(String trainId) {
        this.trainId = UUID.fromString(trainId);
    }

    @PropertyName("seatId")
    public void setSeatIdFromString(String seatId) {
        this.seatId = UUID.fromString(seatId);
    }

    @PropertyName("time")
    public void setTimeFromString(String time) {
        this.time = LocalDateTime.parse(time);
    }

    @PropertyName("status")
    public void setStatusFromString(String status) {
        this.status = Status.valueOf(status);
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

    public LocalDateTime getTime() {
        return this.time;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public double getPrice() {
        return this.price;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Seat)) {
            return false;
        }
        var other = (Seat) o;
        return this.trainCompany.equals(other.trainCompany)
                && this.trainId.equals(other.trainId)
                && this.seatId.equals(other.seatId);
    }

    @Override
    public int hashCode() {
        return this.trainCompany.hashCode() * this.trainId.hashCode() * this.seatId.hashCode();
    }
}
