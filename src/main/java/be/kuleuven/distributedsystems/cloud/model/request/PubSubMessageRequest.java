package be.kuleuven.distributedsystems.cloud.model.request;

import java.io.Serializable;
import java.time.LocalDateTime;


public class PubSubMessageRequest implements Serializable {
    private Message message;
    private String subscription;

    public PubSubMessageRequest() {
    }

    public PubSubMessageRequest(Message message, String subscription) {
        this.message = message;
        this.subscription = subscription;
    }

    public Message getMessage() {
        return message;
    }

    public String getSubscription() {
        return subscription;
    }

    public static class Message implements Serializable {
        private Attributes attributes;
        private String data;
        private String messageId;
        private LocalDateTime publishTime;

        public Message() {
        }

        public Message(Attributes attributes, String data, String messageId, LocalDateTime publishTime) {
            this.attributes = attributes;
            this.data = data;
            this.messageId = messageId;
            this.publishTime = publishTime;
        }

        public Attributes getAttributes() {
            return attributes;
        }

        public String getData() {
            return data;
        }

        public String getMessageId() {
            return messageId;
        }

        public LocalDateTime getPublishTime() {
            return publishTime;
        }

        public class Attributes {
        }
    }
}


