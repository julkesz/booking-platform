package be.kuleuven.distributedsystems.cloud.utils;

import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class Beans {
    @Bean(name = "isProduction")
    public boolean isProduction() {
        return Objects.equals(System.getenv("GAE_ENV"), "standard");
    }

    @Bean(name = "projectId")
    public String projectId() {
        if (isProduction())
            return Utils.CLOUD_PROJECT_ID;
        return Utils.LOCAL_PROJECT_ID;
    }

    @Bean
    public Firestore database() {
        if (isProduction())
            return Utils.CLOUD_DATABASE;
        return Utils.LOCAL_DATABASE;
    }

    @Bean(name = "pubSubEmulatorHost")
    public String pubSubEmulatorHost() {
        if (isProduction())
            return Utils.CLOUD_PUBSUB_EMULATOR_HOST;
        return Utils.LOCAL_PUBSUB_EMULATOR_HOST;
    }

    @Bean(name = "pubSubSubscriberEndpoint")
    public String pubSubSubscriberEndpoint() {
        if (isProduction())
            return Utils.CLOUD_PUBSUB_SUBSCRIBER_ENDPOINT;
        return Utils.LOCAL_PUBSUB_SUBSCRIBER_ENDPOINT;
    }
}
