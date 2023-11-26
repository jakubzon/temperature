package pl.jz.pojo.thermometer;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThermometerValue {

  private BigDecimal temperature;
  private BigDecimal duration;
}
