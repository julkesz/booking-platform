package be.kuleuven.distributedsystems.cloud.repository;

import be.kuleuven.distributedsystems.cloud.entities.Seat;
import be.kuleuven.distributedsystems.cloud.entities.TrainTime;
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
public class TrainTimeRepository {
    private final Firestore database;
    @Autowired
    public TrainTimeRepository(Firestore database) {
        this.database = database;
    }

    public List<Seat> getAvailableSeats(String trainCompany, String trainId, String trainTime) {
        try {
            QuerySnapshot querySnapshot = database.collection("TrainTimes").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            LocalDateTime localDateTime = LocalDateTime.parse(trainTime, Utils.DATE_TIME_FORMATTER1);

            List<Seat> seatsList = documents.stream()
                    .map(document -> document.toObject(TrainTime.class))
                    .filter(time -> time.getTrainCompany().equals(trainCompany))
                    .filter(time -> time.getTrainId().equals(UUID.fromString(trainId)))
                    .filter(time -> time.getTime().equals(localDateTime))
                    .flatMap(time -> time.getSeats().stream())
                    .filter(seat -> seat.getStatus().equals(Seat.Status.AVAILABLE))
                    .collect(Collectors.toList());

            return seatsList;
        } catch (ExecutionException | InterruptedException ignored) {
            return new ArrayList<>();
        }
    }

    public Seat getSeat(String trainCompany, String trainId, String seatId) {
        try {
            QuerySnapshot querySnapshot = database.collection("TrainTimes").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            Seat seatDatabase = documents.stream()
                    .map(document -> document.toObject(TrainTime.class))
                    .filter(time -> time.getTrainCompany().equals(trainCompany))
                    .filter(time -> time.getTrainId().equals(UUID.fromString(trainId)))
                    .flatMap(time -> time.getSeats().stream())
                    .filter(seat -> seat.getSeatId().equals(UUID.fromString(seatId)))
                    .findFirst().get();
            return seatDatabase;
        } catch (Exception ignored) {
            return null;
        }
    }

    public TrainTime getTrainTimeById(UUID trainTimeId) {
        try {
            QuerySnapshot querySnapshot = database.collection("TrainTimes").whereEqualTo("trainTimeId", trainTimeId).get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
            if (documents.isEmpty()) {
                return null;
            }
            return documents.get(0).toObject(TrainTime.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void addTrainTime(TrainTime trainTime) {
        TrainTime trainTimeById = getTrainTimeById(trainTime.getTrainTimeId());
        if (trainTimeById == null) {
            String json = Utils.GSON.toJson(trainTime);
            Map<String, Object> data = Utils.GSON.fromJson(json, new TypeToken<Map<String, Object>>() {
            }.getType());
            database.collection("TrainTimes")
                    .document(trainTime.getTrainTimeId().toString())
                    .set(data);
        }
    }

    private void updateTrainTime(TrainTime trainTime) {
        String json = Utils.GSON.toJson(trainTime);
        Map<String, Object> data = Utils.GSON.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());

        database.collection("TrainTimes")
                .document(trainTime.getTrainTimeId().toString())
                .set(data);
    }


    public void bookSeat(String trainCompany, String trainId, String seatId) {
        try {
            Seat seatDatabase = getSeat(trainCompany, trainId, seatId);
            LocalDateTime localDateTime = seatDatabase.getTime();

            QuerySnapshot querySnapshot = database.collection("TrainTimes").get().get();
            List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();

            documents.stream()
                    .map(document -> document.toObject(TrainTime.class))
                    .filter(time -> time.getTrainCompany().equals(trainCompany))
                    .filter(time -> time.getTrainId().equals(UUID.fromString(trainId)))
                    .filter(time -> time.getTime().equals(localDateTime))
                    .forEach(time -> {
                        List<Seat> updatedSeats = time.getSeats().stream()
                                .map(seat -> {
                                    if (seat.getSeatId().equals(UUID.fromString(seatId))) {
                                        seat.setStatusFromString("BOOKED");
                                    }
                                    return seat;
                                })
                                .collect(Collectors.toList());

                        time.setSeats(updatedSeats);
                        updateTrainTime(time);
                    });
        } catch (Exception ignored) {
        }
    }

}

