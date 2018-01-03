package tulip.sockets.messages;

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

    /** The name of the sender of the message */
    @JsonProperty("sender")
    private String sender;

    /** The name of the recipient of the message */
    @JsonProperty("recipient")
    private String recipient;

    /** The type of the content of the message */
    @JsonProperty("contentType")
    private ContentType contentType;

    /** The content of the message in the form of a String */
    @JsonProperty("content")
    private String content;

    @JsonCreator
    public Message(@JsonProperty("sender") String sender, @JsonProperty("recipient") String recipient,
                   @JsonProperty("contentType") ContentType contentType, @JsonProperty("content") String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.contentType = contentType;
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getContent() {
        return content;
    }

    /**
     * Translates the message into JSON
     * @return the JSON representation of the message
     */
    public String toJSON() {
        String json = null;

        try {
            json = mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    /** Constructs a Message object from a JSON string
     * @return the Message object constructed
     */
    public static Message fromJSON(String json) {
        Message message = null;

        try {
            message = mapper.readValue(json, Message.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }
}
