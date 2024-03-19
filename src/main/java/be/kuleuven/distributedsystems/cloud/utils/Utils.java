package be.kuleuven.distributedsystems.cloud.utils;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static final String API_KEY  = "JViZPgNadspVcHsMbDFrdGg0XXxyiE";
    public static final String LOCAL_PROJECT_ID = "booking-platform";
    public static final String CLOUD_PROJECT_ID = "train-companies-ds";

    public static final List<String> EXTERNAL_TRAIN_COMPANIES = Arrays.asList(
            "reliabletrains.com",
            "unreliabletrains.com"
    );

    public static final Integer RETRY_COUNT = 5;

    private static final Credentials localCredentials = new FirestoreOptions.EmulatorCredentials();
    private static final FirestoreOptions localFirestoreOptions = FirestoreOptions.getDefaultInstance()
            .toBuilder()
            .setProjectId(Utils.LOCAL_PROJECT_ID)
            .setCredentials(localCredentials)
            .setEmulatorHost("localhost:8084")
            .build();
    public static final Firestore LOCAL_DATABASE = localFirestoreOptions.getService();

    private static Credentials cloudCredentials = null;

    static {
        try {
            cloudCredentials = GoogleCredentials.getApplicationDefault();
        } catch (IOException ignore) {
        }
    }

    private static final FirestoreOptions cloudFirestoreOptions = FirestoreOptions.getDefaultInstance()
            .toBuilder()
            .setCredentials(cloudCredentials)
            .setProjectId(Utils.CLOUD_PROJECT_ID)
            .build();
    public static final Firestore CLOUD_DATABASE = cloudFirestoreOptions.getService();

    public static final DateTimeFormatter DATE_TIME_FORMATTER1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter DATE_TIME_FORMATTER2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateTimeFormatter DATE_TIME_FORMATTER3 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
    public static final DateTimeFormatter DATE_TIME_FORMATTER4 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    private static final GsonBuilder gsonBuilder = new GsonBuilder();
    static {
        gsonBuilder.registerTypeAdapter(LocalDateTime .class, new LocalDateTimeTypeAdapter());
    }
    public static final Gson GSON = gsonBuilder.setPrettyPrinting().create();

    public static final String LOCAL_PUBSUB_EMULATOR_HOST = "localhost:8083";
    public static final String CLOUD_PUBSUB_EMULATOR_HOST = "train-companies-ds.ew.r.appspot.com:8083";
    public static final String PUBSUB_TOPIC_ID = "confirm-quotes";
    public static final String PUBSUB_SUBSCRIPTION_ID = "subscription-1";
    public static final String LOCAL_PUBSUB_SUBSCRIBER_ENDPOINT ="http://localhost:8080/subscription";
    public static final String CLOUD_PUBSUB_SUBSCRIBER_ENDPOINT ="https://train-companies-ds.ew.r.appspot.com:8080/subscription";
    public static final InputStream DATA_FILE_STREAM = Utils.class.getClassLoader().getResourceAsStream("data.json");
    public static final String EMAIL_ADDRESS = "trainservice11@gmail.com";
}
