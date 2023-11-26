package pl.jz.pojo.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import pl.jz.pojo.thermometer.ThermometerValue;

@Getter
@Setter
public class ThermometerMessage extends Message {

  @JsonProperty("value")
  private ThermometerValue value;
}
