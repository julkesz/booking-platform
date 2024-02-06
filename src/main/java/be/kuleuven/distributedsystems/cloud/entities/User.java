package be.kuleuven.distributedsystems.cloud.entities;

import com.google.cloud.firestore.annotation.PropertyName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class User implements Serializable {

    private String email;
    private String[] roles;

    private Integer ticketCount;

    public User() {
    }

    public User(String email, String[] roles) {
        this.email = email;
        this.roles = roles;
        this.ticketCount = 0;
    }

    @PropertyName("roles")
    public void setRolesFromList(List<String> roles) {
        this.roles = roles.toArray(new String[0]);
    }

    public String getEmail() {
        return email;
    }

    public String[] getRoles() {
        return roles;
    }

    public Integer getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(Integer ticketCount) {
        this.ticketCount = ticketCount;
    }

    public boolean isManager() {
        return this.roles != null && Arrays.stream(this.roles).toList().contains("manager");
    }

    public void addTicket() {
        ticketCount++;
    }
}
