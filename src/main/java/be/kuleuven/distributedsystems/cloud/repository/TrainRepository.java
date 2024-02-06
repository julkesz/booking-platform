package be.kuleuven.distributedsystems.cloud.repository;

import be.kuleuven.distributedsystems.cloud.entities.Train;
import be.kuleuven.distributedsystems.cloud.utils.Utils;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Repository
public class TrainRepository {
    private final Firestore database;
    @Autowired
    public TrainRepository(Firestore database) {
        this.database = database;
    }

    public Set<Train> getAllTrains() {
        try {
            QuerySnapshot querySnapshot = database.collection("Trains").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            return documents.stream().map(document -> document.toObject(Train.class)).collect(Collectors.toSet());
        } catch (ExecutionException | InterruptedException ignored) {
            return new HashSet<>();
        }
    }

    public Train getTrainById(String trainCompany, String trainId) {
        try {
            Train train = database.collection("Trains").document(trainId).get().get().toObject(Train.class);
            if (train == null || !train.getTrainCompany().equals(trainCompany))
                return null;
            return train;
        } catch (Exception ignored) {
            return null;
        }
    }

    public Train getTrainByName(String name) {
        try {
            QuerySnapshot querySnapshot = database.collection("Trains").whereEqualTo("name", name).get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            if (documents.isEmpty()) {
                return null;
            }
            return documents.get(0).toObject(Train.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void addTrain(Train train) {
        Train trainByName = getTrainByName(train.getName());
        if (trainByName == null) {
            String json = Utils.GSON.toJson(train);
            Map<String, Object> data = Utils.GSON.fromJson(json, new TypeToken<Map<String, Object>>() {
            }.getType());
            database.collection("Trains")
                    .document(train.getTrainId().toString())
                    .set(data);
        }
    }

    public List<LocalDateTime> getTrainTimes(String trainCompany, String trainId) {

        try {
            QuerySnapshot querySnapshot = database.collection("Trains").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            List<LocalDateTime> timesList = documents.stream()
                    .map(document -> document.toObject(Train.class))
                    .filter(train -> train.getTrainCompany().equals(trainCompany))
                    .filter(train -> train.getTrainId().equals(UUID.fromString(trainId)))
                    .flatMap(train -> train.getTrainTimes().stream())
                    //.map(str -> LocalDateTime.parse(str, Utils.DATE_TIME_FORMATTER1))
                    .sorted(LocalDateTime::compareTo)
                    .collect(Collectors.toList());

            return timesList;
        } catch (ExecutionException | InterruptedException ignored) {
            return new ArrayList<>();
        }
    }
}
