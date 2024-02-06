package be.kuleuven.distributedsystems.cloud.entities;

import com.google.cloud.firestore.annotation.PropertyName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TrainTime implements Serializable{

    private UUID trainTimeId;
    private String trainCompany;
    private UUID trainId;
    private LocalDateTime time;
    private List<Seat> seats;
    public TrainTime() {
    }
    public TrainTime(UUID trainTimeId, String trainCompany, UUID trainId, LocalDateTime time, List<Seat> seats) {
        this.trainTimeId = trainTimeId;
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.time = time;
        this.seats = seats;
    }

    @PropertyName("trainTimeId")
    public void setTrainTimeIdFromString(String trainTimeId) {
        this.trainTimeId = UUID.fromString(trainTimeId);
    }

    @PropertyName("trainId")
    public void setTrainIdFromString(String trainId) {
        this.trainId = UUID.fromString(trainId);
    }

    @PropertyName("time")
    public void setTimeFromString(String time) {
        this.time = LocalDateTime.parse(time);
    }

    public UUID getTrainTimeId() {
        return trainTimeId;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public String getTrainCompany() {
        return trainCompany;
    }

    public UUID getTrainId() {
        return trainId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TrainTime)) {
            return false;
        }
        var other = (TrainTime) o;
        return this.trainCompany.equals(other.trainCompany)
                && this.trainId.equals(other.trainId)
                && this.time.equals(other.time);
    }

    @Override
    public int hashCode() {
        return this.trainCompany.hashCode() * this.trainId.hashCode() * this.time.hashCode();
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }
}