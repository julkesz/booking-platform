package be.kuleuven.distributedsystems.cloud.model.request;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class PubSubDataRequest implements Serializable {
    private String user;
    private LocalDateTime timeStamp;
    private List<ConfirmQuoteRequest> confirmQuotesRequest;

    public PubSubDataRequest() {
    }
    public PubSubDataRequest(String user, LocalDateTime timeStamp, List<ConfirmQuoteRequest> confirmQuotesRequest) {
        this.user = user;
        this.timeStamp = timeStamp;
        this.confirmQuotesRequest = confirmQuotesRequest;
    }

    public String getUser() {
        return user;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public List<ConfirmQuoteRequest> getConfirmQuotesRequest() {
        return confirmQuotesRequest;
    }

    @Override
    public String toString() {
        return "Request: \n" +
                "\tuser: " + user + "\n" +
                "\ttime: " + timeStamp + "\n" +
                "\ttickets: \n" + confirmQuotesRequest;
    }
}
