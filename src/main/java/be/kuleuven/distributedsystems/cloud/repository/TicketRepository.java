package be.kuleuven.distributedsystems.cloud.repository;

import be.kuleuven.distributedsystems.cloud.entities.Ticket;
import be.kuleuven.distributedsystems.cloud.exceptions.DuplicateEntity;
import be.kuleuven.distributedsystems.cloud.utils.Utils;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class TicketRepository {
    private final Firestore database;

    @Autowired
    public TicketRepository(Firestore database) {
        this.database = database;
    }

    public Set<Ticket> getAllTickets() {
        try {
            QuerySnapshot querySnapshot = database.collection("Tickets").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream()
                    .map(document -> document.toObject(Ticket.class))
                    .collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    public Set<UUID> getTicketIds() {
        try {
            QuerySnapshot querySnapshot = database.collection("Tickets").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream()
                    .map(document -> document.toObject(Ticket.class))
                    .map(Ticket::getTicketId)
                    .collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    public Set<Ticket> getCustomerTickets(String customer) {
        try {
            QuerySnapshot querySnapshot = database.collection("Tickets").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream()
                    .map(document -> document.toObject(Ticket.class))
                    .filter(ticket -> ticket.getCustomer().equals(customer))
                    .collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    synchronized public void addTicket(Ticket ticket) throws DuplicateEntity {
        if (getAllTickets().contains(ticket))
            throw new DuplicateEntity("This ticket already exists");

        String json = Utils.GSON.toJson(ticket);
        Map<String, Object> data = Utils.GSON.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
        database.collection("Tickets")
                .document(ticket.getTicketId().toString())
                .set(data);
    }
}
