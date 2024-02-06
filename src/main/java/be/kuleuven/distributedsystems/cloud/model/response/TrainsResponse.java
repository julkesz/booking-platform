package be.kuleuven.distributedsystems.cloud.model.response;

import be.kuleuven.distributedsystems.cloud.entities.Train;

import java.io.Serializable;
import java.util.List;

public class TrainsResponse implements Serializable {
    private Embedded _embedded;

    public TrainsResponse() {
    }

    public TrainsResponse(Embedded _embedded) {
        this._embedded = _embedded;
    }

    public Embedded get_embedded() {
        return _embedded;
    }

    public void set_embedded(Embedded _embedded) {
        this._embedded = _embedded;
    }

    public static class Embedded implements Serializable {
        private List<Train> trains;

        public Embedded() {
        }

        public Embedded(List<Train> trains) {
            this.trains = trains;
        }

        public List<Train> getTrains() {
            return trains;
        }

        public void setTrains(List<Train> trains) {
            this.trains = trains;
        }
    }
}

