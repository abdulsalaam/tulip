package tulip.app.appMessage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class AppMessage implements Serializable {

    /** ObjectMapper used to serialize and deserialize */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** The name of the sender of the AppMessage */
    @JsonProperty("sender")
    private String sender;

    /** The type of the sender of the AppMessage */
    @JsonProperty("senderType")
    private ActorType senderType;

    /** The name of the recipient of the AppMessage */
    @JsonProperty("recipient")
    private String recipient;

    /** The type of the recipient of the AppMessage */
    @JsonProperty("recipientType")
    private ActorType recipientType;

    /** The type of the content of the message */
    @JsonProperty("appMessageContentType")
    private AppMessageContentType appMessageContentType;

    /** The content of the message in the form of a String */
    @JsonProperty("content")
    private String content;

    @JsonCreator
    public AppMessage(@JsonProperty("sender") String sender,
                      @JsonProperty("senderType") ActorType senderType,
                      @JsonProperty("recipient") String recipient,
                      @JsonProperty("recipientType") ActorType recipientType,
                      @JsonProperty("appMessageContentType") AppMessageContentType appMessageContentType,
                      @JsonProperty("content") String content) {
        this.sender = sender;
        this.senderType = senderType;
        this.recipient = recipient;
        this.recipientType = recipientType;
        this.appMessageContentType = appMessageContentType;
        this.content = content;
    }

    /** Returns the JSON representation of the AppMessage */
    public String toJSON() {
        String json = null;

        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    /** Constructs an AppMessage object from a JSON string */
    public static AppMessage fromJSON(String json) {
        AppMessage appMessage = null;

        try {
            appMessage = mapper.readValue(json, AppMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return appMessage;
    }

    public String getSender() {
        return sender;
    }

    public ActorType getSenderType() {
        return senderType;
    }

    public String getRecipient() {
        return recipient;
    }

    public ActorType getRecipientType() {
        return recipientType;
    }

    public AppMessageContentType getAppMessageContentType() {
        return appMessageContentType;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AppMessage that = (AppMessage) o;

        if (!sender.equals(that.sender)) { return false; }
        if (senderType != that.senderType) { return false; }
        if (!recipient.equals(that.recipient)) { return false; }
        if (!recipientType.equals(that.recipientType)) { return false; }
        if (!appMessageContentType.equals(that.appMessageContentType)) { return false; }
        return content.equals(that.content);
    }

    @Override
    public int hashCode() {
        int result = sender.hashCode();
        result = 31 * result + senderType.hashCode();
        result = 31 * result + recipient.hashCode();
        result = 31 * result + recipientType.hashCode();
        result = 31 * result + appMessageContentType.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }
}
