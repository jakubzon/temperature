package pl.jz.pojo.message;

import lombok.Getter;
import lombok.Setter;
import pl.jz.pojo.enums.MessageType;
import pl.jz.pojo.thermometer.Thermometer;

@Getter
@Setter
public class Message {

  protected MessageType type;
  protected Thermometer thermometer;
}
