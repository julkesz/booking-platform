package be.kuleuven.distributedsystems.cloud.repository;

import be.kuleuven.distributedsystems.cloud.entities.Booking;
import be.kuleuven.distributedsystems.cloud.entities.User;
import be.kuleuven.distributedsystems.cloud.exceptions.DuplicateEntity;
import be.kuleuven.distributedsystems.cloud.utils.Utils;
import com.google.cloud.firestore.*;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class BookingRepository {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final Firestore database;

    @Autowired
    public BookingRepository(UserRepository userRepository, TicketRepository ticketRepository, Firestore database) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.database = database;
    }

    public Set<Booking> getAllBookings() {
        try {
            QuerySnapshot querySnapshot = database.collection("Bookings").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream().map(document -> document.toObject(Booking.class))
                    .collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    public Set<UUID> getBookingIds() {
        try {
            QuerySnapshot querySnapshot = database.collection("Bookings").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream()
                    .map(document -> document.toObject(Booking.class))
                    .map(Booking::getId)
                    .collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    public Set<Booking> getCustomerBookings(String customer) {
        try {
            QuerySnapshot querySnapshot = database.collection("Bookings").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream()
                    .map(document -> document.toObject(Booking.class))
                    .filter(booking -> booking.getCustomer().equals(customer))
                    .collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    synchronized public void addBooking(Booking booking) throws DuplicateEntity {
        database.runTransaction(transaction -> {
            if (getAllBookings().contains(booking))
                throw new DuplicateEntity("This booking already exists");

            User user = userRepository.getUserByEmail(booking.getCustomer());

            booking.getTickets().forEach(ticket -> {
                user.addTicket();
                try {
                    ticketRepository.addTicket(ticket);
                } catch (DuplicateEntity ignored) {
                }
            });

            userRepository.updateUser(user);
            String json = Utils.GSON.toJson(booking);

            Map<String, Object> data = Utils.GSON.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());
            database.collection("Bookings")
                    .document(booking.getId().toString())
                    .set(data);
            return null;
        });
    }
}