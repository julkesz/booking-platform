package be.kuleuven.distributedsystems.cloud.model.request;

import java.io.Serializable;

public class ConfirmQuoteRequest implements Serializable {
    private String trainCompany;
    private String trainId;
    private String seatId;

    public ConfirmQuoteRequest() {
    }

    public ConfirmQuoteRequest(String trainCompany, String trainId, String seatId) {
        this.trainCompany = trainCompany;
        this.trainId = trainId;
        this.seatId = seatId;
    }

    public String getTrainCompany() {
        return trainCompany;
    }

    public String getTrainId() {
        return trainId;
    }

    public String getSeatId() {
        return seatId;
    }

    @Override
    public String toString() {
        return "\tTicket: " + "\n" +
                "\t\ttrain company: " + trainCompany + "\n" +
                "\t\ttrain id: " + trainId + "\n" +
                "\t\tseat id: " + seatId + "\n";
    }
}

