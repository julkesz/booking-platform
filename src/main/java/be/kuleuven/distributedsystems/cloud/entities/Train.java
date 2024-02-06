package be.kuleuven.distributedsystems.cloud.entities;

import com.google.cloud.firestore.annotation.PropertyName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Train implements Serializable {
    private String trainCompany;

    private UUID trainId;
    private String name;
    private String location;
    private String image;
    private List<LocalDateTime> trainTimes;

    public Train() {
    }

    public Train(String trainCompany, UUID trainId, String name, String location, String image, List<LocalDateTime> trainTimes) {
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.name = name;
        this.location = location;
        this.image = image;
        this.trainTimes = trainTimes;
    }
    @PropertyName("trainId")
    public void setTrainIdFromString(String trainId) {
        this.trainId = UUID.fromString(trainId);
    }

    @PropertyName("trainTimes")
    public void setTrainTimesFromStringList(List<String> trainTimes) {
        this.trainTimes = trainTimes.stream()
                .map(LocalDateTime::parse)
                .collect(Collectors.toList());
    }

    public String getTrainCompany() {
        return trainCompany;
    }

    public UUID getTrainId() {
        return trainId;
    }

    public String getName() {
        return this.name;
    }

    public String getLocation() {
        return this.location;
    }

    public String getImage() {
        return this.image;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Train)) {
            return false;
        }
        var other = (Train) o;
        return this.trainCompany.equals(other.trainCompany)
                && this.trainId.equals(other.trainId);
    }

    @Override
    public int hashCode() {
        return this.trainCompany.hashCode() * this.trainId.hashCode();
    }

    public List<LocalDateTime> getTrainTimes() {
        return trainTimes;
    }

    public void addTrainTime(LocalDateTime trainTime) {
        this.trainTimes.add(trainTime);
    }
}
