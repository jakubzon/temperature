package pl.jz.pojo.thermometer;

import java.math.BigDecimal;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import pl.jz.pojo.enums.PointType;

@Getter
@Setter
public class Thermometer {

  private String name;
  private PointType point;

  private boolean disabled;
  private BigDecimal average = BigDecimal.ZERO;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Thermometer that = (Thermometer) o;
    return Objects.equals(name, that.name) && point == that.point;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, point);
  }
}
