package pl.jz.bl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import pl.jz.pojo.message.Message;
import pl.jz.pojo.message.ThermometerMessage;

public class DataReader {

  private final ObjectMapper objectMapper;

  public DataReader() {
    this.objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public List<Message> readMessagesFromFile() {
    var messagesList = new ArrayList<Message>();
    var messagesIs = getClass().getClassLoader().getResourceAsStream("pl/jz/messages.txt");
    if(messagesIs == null) {
      throw new RuntimeException("Problem with reading messages file");
    }
    try (InputStreamReader streamReader =
             new InputStreamReader(messagesIs, StandardCharsets.UTF_8);
         BufferedReader reader = new BufferedReader(streamReader)) {

      String line;
      while ((line = reader.readLine()) != null) {
        if(line.contains("\"value\"")) {
          messagesList.add(objectMapper.readValue(line, ThermometerMessage.class));
        } else {
          messagesList.add(objectMapper.readValue(line, Message.class));
        }
      }

    } catch (IOException e) {
      System.out.println("Something is no yes");
    }


    return messagesList;
  }
}
