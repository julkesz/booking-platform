package be.kuleuven.distributedsystems.cloud.pubsub;

import be.kuleuven.distributedsystems.cloud.utils.Utils;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.NotFoundException;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.*;
import com.google.pubsub.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class PubSubConfig {
    private TransportChannelProvider channelProvider;
    private CredentialsProvider credentialsProvider;
    private Topic topic;
    private final String pubSubEmulatorHost;
    private final String pubSubSubscriberEndpoint;
    private final String projectId;
    private final Publisher publisher;

    @Autowired
    public PubSubConfig(@Qualifier("pubSubEmulatorHost") String pubSubEmulatorHost, @Qualifier("pubSubSubscriberEndpoint") String pubSubSubscriberEndpoint, @Qualifier("projectId") String projectId, @Qualifier("isProduction") boolean isProduction) throws IOException {
        this.pubSubEmulatorHost = pubSubEmulatorHost;
        this.pubSubSubscriberEndpoint = pubSubSubscriberEndpoint;
        this.projectId = projectId;

        if (isProduction)
            publisher = getCloudPublisher();
        else {
            this.channelProvider = createChannelProvider();
            this.credentialsProvider = createCredentialsProvider();
            this.topic = createTopic();
            publisher = getLocalPublisher();
            createSubscription(topic.getName());
        }
    }

    private TransportChannelProvider createChannelProvider() {
        ManagedChannel managedChannel = ManagedChannelBuilder.forTarget(pubSubEmulatorHost).usePlaintext().build();
        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(managedChannel));
    }

    private CredentialsProvider createCredentialsProvider() {
        return NoCredentialsProvider.create();
    }

    private Topic createTopic() throws IOException {
        TopicAdminClient topicAdminClient = TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(credentialsProvider)
                        .build());

        TopicName topicName = TopicName.of(projectId, Utils.PUBSUB_TOPIC_ID);
        try {
            return topicAdminClient.getTopic(topicName);
        } catch (NotFoundException e) {
            return topicAdminClient.createTopic(topicName);
        }
    }

    private void createSubscription(String topicName) throws IOException {
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(
                SubscriptionAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(credentialsProvider)
                        .build());

        SubscriptionName subscriptionName = SubscriptionName.of(projectId, Utils.PUBSUB_SUBSCRIPTION_ID);
        try {
            Subscription subscription = subscriptionAdminClient.getSubscription(subscriptionName);
            System.out.println("Subscription already exists: " + subscription.getAllFields());
        } catch (NotFoundException e) {
            PushConfig pushConfig = PushConfig.newBuilder()
                    .setPushEndpoint(pubSubSubscriberEndpoint)
                    .build();
            subscriptionAdminClient.createSubscription(
                    Subscription.newBuilder()
                            .setName(subscriptionName.toString())
                            .setTopic(topicName)
                            .setPushConfig(pushConfig)
                            .setAckDeadlineSeconds(60)
                            .build());
            System.out.println("Created a subscription: " + subscriptionName);
        }
    }

    public TransportChannelProvider getChannelProvider() {
        return channelProvider;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public Topic getTopic() {
        return topic;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    private Publisher getCloudPublisher() {
        try {
            return Publisher.newBuilder(TopicName.of(projectId, Utils.PUBSUB_TOPIC_ID)).build();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Publisher getLocalPublisher() {
        try {
            return Publisher.newBuilder(getTopic().getName())
                    .setChannelProvider(getChannelProvider())
                    .setCredentialsProvider(getCredentialsProvider())
                    .build();
        } catch (Exception ignored) {
            return null;
        }
    }
}