package be.kuleuven.distributedsystems.cloud.repository;

import be.kuleuven.distributedsystems.cloud.entities.User;
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
public class UserRepository {
    private final Firestore database;

    @Autowired
    public UserRepository(Firestore database) {
        this.database = database;
    }

    public Set<User> getAllUsers() {
        try {
            QuerySnapshot querySnapshot = database.collection("Users").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream().map(document -> document.toObject(User.class)).collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    public Set<User> getUsersByTicketCount(Integer ticketCount) {
        try {
            QuerySnapshot querySnapshot = database.collection("Users").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream()
                    .map(document -> document.toObject(User.class))
                    .filter(user -> user.getTicketCount().equals(ticketCount))
                    .collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    private Set<String> getUserEmails() {
        try {
            QuerySnapshot querySnapshot = database.collection("Users").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream()
                    .map(document -> document.toObject(User.class))
                    .map(User::getEmail)
                    .collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    public User getUserByEmail(String email) {
        try {
            return database.collection("Users").document(email).get().get().toObject(User.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void addUser(User user) {
        Set<String> bookingIds = getUserEmails();
        if (!bookingIds.contains(user.getEmail())) {
            String json = Utils.GSON.toJson(user);
            Map<String, Object> data = Utils.GSON.fromJson(json, new TypeToken<Map<String, Object>>() {
            }.getType());
            database.collection("Users")
                    .document(user.getEmail())
                    .set(data);
        }
    }

    public void updateUser(User user) {
        String json = Utils.GSON.toJson(user);
        Map<String, Object> data = Utils.GSON.fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
        database.collection("Users")
                .document(user.getEmail())
                .set(data);
    }
}
