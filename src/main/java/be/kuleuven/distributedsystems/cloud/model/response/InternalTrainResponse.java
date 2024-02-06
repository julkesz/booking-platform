package be.kuleuven.distributedsystems.cloud.model.response;

import be.kuleuven.distributedsystems.cloud.entities.Seat;

import java.io.Serializable;
import java.util.List;

public class InternalTrainResponse implements Serializable {
    private List<InternalTrain> trains;

    public InternalTrainResponse() {
    }

    public InternalTrainResponse(List<InternalTrain> trains) {
        this.trains = trains;
    }

    public List<InternalTrain> getTrains() {
        return trains;
    }

    public void setTrains(List<InternalTrain> trains) {
        this.trains = trains;
    }

    public static class InternalTrain implements Serializable {
        private String name;
        private String location;
        private String image;
        private List<Seat> seats;

        public InternalTrain() {
        }

        public InternalTrain(String name, String location, String image, List<Seat> seats) {
            this.name = name;
            this.location = location;
            this.image = image;
            this.seats = seats;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public List<Seat> getSeats() {
            return seats;
        }

        public void setSeats(List<Seat> seats) {
            this.seats = seats;
        }
    }
}
