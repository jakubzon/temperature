package pl.jz.bl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.jz.exceptions.RecieviedMessageException;
import pl.jz.pojo.enums.MessageType;
import pl.jz.pojo.enums.PointType;
import pl.jz.pojo.message.Message;
import pl.jz.pojo.message.ThermometerMessage;
import pl.jz.pojo.thermometer.Thermometer;
import pl.jz.pojo.thermometer.ThermometerValue;

public class DataProcesser {

  //I would add thermometer name here
  private static final String THERMOMETER_DATA_INFO_TEMPLATE =
      "Averange temperature over time: {0}: {1}, {2}: {3}, {4}: {5}";
  private final Map<Thermometer, List<ThermometerValue>> thermometersValues = new HashMap<>();

  public void processMessage(Message message) {
    validateMessage(message);
    if (message instanceof ThermometerMessage) {
      addThermometerMessage((ThermometerMessage) message);
    } else {
      addMessage(message);
    }
  }

  private void addMessage(Message message) {
    if (message.getType().equals(MessageType.STOP)) {
      var thermometer = thermometersValues.keySet().stream().filter(therm -> therm.equals(message.getThermometer()))
          .findFirst();
      thermometer.ifPresent(t -> t.setDisabled(true));
    }
  }

  private void addThermometerMessage(ThermometerMessage message) {
    if (message.getValue() != null) {
      var thermOpt = thermometersValues.keySet().stream().filter(therm -> therm.equals(message.getThermometer()))
          .findFirst();
      //add thermometer value (or whole thermometer if not present inside map)
      var thermometerValues = thermometersValues.get(message.getThermometer());
      if (thermometerValues == null) {
        thermometerValues = new ArrayList<>();
        thermometerValues.add(message.getValue());
        thermometersValues.put(message.getThermometer(), thermometerValues);
      } else if (thermOpt.isPresent() && thermOpt.get().isDisabled()) {
        //if thermometer is already stopped reject data
        System.out.println("Data rejcted, theremometer is stopped");
        return;
      } else {
        thermometerValues.add(message.getValue());
      }
      //calculate average for added thermometer
      thermOpt = thermometersValues.keySet().stream().filter(therm -> therm.equals(message.getThermometer()))
          .findFirst();
      if (thermOpt.isPresent()) {
        calculateCurrentThermometerAverage(thermOpt.get(), thermometerValues);
      }

      //get all points of this thermometer
      var allThermometerPoints = thermometersValues.keySet().stream()
          .filter(therm -> therm.getName().equals(message.getThermometer().getName()))
          .toList();


      BigDecimal insideAverage = BigDecimal.ZERO;
      BigDecimal matterAverage = BigDecimal.ZERO;
      BigDecimal outsideAverage = BigDecimal.ZERO;
      for (Thermometer thermometer : allThermometerPoints) {
        switch (thermometer.getPoint()) {
          case MATTER -> matterAverage = thermometer.getAverage();
          case INSIDE -> insideAverage = thermometer.getAverage();
          case OUTSIDE -> outsideAverage = thermometer.getAverage();
        }
      }
      System.out.println(
          MessageFormat.format(THERMOMETER_DATA_INFO_TEMPLATE, PointType.INSIDE, insideAverage, PointType.OUTSIDE,
              outsideAverage, PointType.MATTER, matterAverage));
    }
  }

  private void calculateCurrentThermometerAverage(Thermometer thermometer, List<ThermometerValue> values) {
    BigDecimal average = BigDecimal.ZERO;
    var wholeDuration = values.stream().map(ThermometerValue::getDuration).reduce(BigDecimal.ZERO, BigDecimal::add);
    for (ThermometerValue v : values) {
      average =
          average.add(v.getTemperature().multiply(v.getDuration().divide(wholeDuration, RoundingMode.FLOOR)));
    }
    thermometer.setAverage(average);
  }

  private void validateMessage(Message message) {
    if (message == null) {
      throw new RecieviedMessageException("Recieved empty message");
    }
    if (message.getType() == null) {
      throw new RecieviedMessageException("Recieved message has unknown type");
    }
    if (message.getThermometer() == null) {
      throw new RecieviedMessageException("Recieved message has no information about thermometer");
    }
    if (message.getThermometer().getPoint() == null) {
      throw new RecieviedMessageException("Recieved message has unknown thermometer point");
    }
    if (message instanceof ThermometerMessage) {
      var thermometerValue = ((ThermometerMessage) message).getValue();
      if (thermometerValue != null
          && thermometerValue.getDuration() != null
          && thermometerValue.getDuration().compareTo(BigDecimal.ZERO) <= 0) {
        throw new RecieviedMessageException("Recieved message has incorrect duration value");
      }
    }
  }


}
