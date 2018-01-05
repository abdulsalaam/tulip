package tulip.service.producerConsumer.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

/**
 * This class is used to communicate through sockets.
 * Messages are sent over a socket connection by converting a Message in a JSON String at one end of the connection
 * and doing the opposite operation at the other end of the connection.
 * */
public class Message implements Serializable {

    /** ObjectMapper used to serialize and deserialize */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** The target of the message */
    @JsonProperty("target")
    private Target target;

    /** The type of the content of the message */
    @JsonProperty("contentType")
    private ContentType contentType;

    /** The content of the message in the form of a String */
    @JsonProperty("content")
    private String content;

    @JsonCreator
    public Message(@JsonProperty("target") Target target,
                   @JsonProperty("contentType") ContentType contentType,
                   @JsonProperty("content") String content) {
        this.target = target;
        this.contentType = contentType;
        this.content = content;
    }

    public Target getTarget() {
        return target;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getContent() {
        return content;
    }

    /** Returns the JSON representation of the message */
    public String toJSON() {
        String json = null;

        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    /** Constructs a Message object from a JSON string */
    public static Message fromJSON(String json) {
        Message message = null;

        try {
            message = mapper.readValue(json, Message.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Message message = (Message) o;

        if (target != message.target) { return false; }
        if (contentType != message.contentType) { return false; }
        return content.equals(message.content);
    }

    @Override
    public int hashCode() {
        int result = target.hashCode();
        result = 31 * result + contentType.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }
}
