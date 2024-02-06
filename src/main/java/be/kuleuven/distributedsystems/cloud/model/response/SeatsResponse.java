package be.kuleuven.distributedsystems.cloud.model.response;

import be.kuleuven.distributedsystems.cloud.entities.Seat;

import java.io.Serializable;
import java.util.List;

public class SeatsResponse implements Serializable {
    private Embedded _embedded;

    public SeatsResponse() {
    }

    public SeatsResponse(Embedded _embedded) {
        this._embedded = _embedded;
    }

    public Embedded get_embedded() {
        return _embedded;
    }

    public void set_embedded(Embedded _embedded) {
        this._embedded = _embedded;
    }

    public static class Embedded implements Serializable {
        private List<Seat> seats;

        public Embedded() {
        }

        public Embedded(List<Seat> seats) {
            this.seats = seats;
        }

        public List<Seat> getSeats() {
            return seats;
        }

        public void setSeats(List<Seat> seats) {
            this.seats = seats;
        }
    }
}
