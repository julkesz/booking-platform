package be.kuleuven.distributedsystems.cloud.model.response;

import java.io.Serializable;
import java.util.List;

public class TimesResponse implements Serializable {
    private Embedded _embedded;

    public TimesResponse() {
    }

    public TimesResponse(Embedded _embedded) {
        this._embedded = _embedded;
    }

    public Embedded get_embedded() {
        return _embedded;
    }

    public void set_embedded(Embedded _embedded) {
        this._embedded = _embedded;
    }

    public static class Embedded implements Serializable {
        private List<String> stringList;

        public Embedded() {
        }

        public Embedded(List<String> stringList) {
            this.stringList = stringList;
        }

        public List<String> getStringList() {
            return stringList;
        }

        public void setTimes(List<String> stringList) {
            this.stringList = stringList;
        }
    }
}
