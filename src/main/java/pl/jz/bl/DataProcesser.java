package pl.jz.bl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pl.jz.exceptions.RecievedMessageException;
import pl.jz.pojo.enums.MessageType;
import pl.jz.pojo.enums.PointType;
import pl.jz.pojo.message.Message;
import pl.jz.pojo.message.ThermometerMessage;
import pl.jz.pojo.thermometer.Thermometer;
import pl.jz.pojo.thermometer.ThermometerValue;

//printing from console can be replaced by some logging framework
public class DataProcesser {

  //I would add thermometer name here
  private static final String THERMOMETER_DATA_ADD_INFO_TEMPLATE =
      "Averange temperature over time: {0}: {1}, {2}: {3}, {4}: {5}";

  private static final String THERMOMETER_DATA_STOP_INFO_TEMPLATE =
      "Thermometer {0} {1} average temperature over time: {2}, Duration sum of stopped thermometers: {3}";
  private final Map<Thermometer, List<ThermometerValue>> thermometersValues = new HashMap<>();
  DecimalFormat df = new DecimalFormat("#0.00");

  public void processMessage(Message message) {
    validateMessage(message);
    if (message instanceof ThermometerMessage) {
      addThermometerMessage((ThermometerMessage) message);
    } else {
      addMessage(message);
    }
  }

  private void addMessage(Message message) {
    //stop message is weird. Why there is a thermometer point in stopping request if I suppose to stop all 3 points?
    if (message.getType().equals(MessageType.STOP)) {
      var thermometersToDisable =
          thermometersValues.keySet().stream().filter(t -> t.getName().equals(message.getThermometer().getName()))
              .toList();
      thermometersToDisable.forEach(t -> {
        t.setDisabled(true);
        calculateCurrentThermometerAverage(t);
      });

      thermometersValues.keySet().stream().filter(therm -> therm.equals(message.getThermometer()))
          .findFirst().ifPresentOrElse(thermometer -> System.out.println(
                  MessageFormat.format(THERMOMETER_DATA_STOP_INFO_TEMPLATE, thermometer.getName(), thermometer.getPoint(),
                      df.format(thermometer.getAverage()), calculateDurationSumOfStoppedThermometers())),
              () -> System.out.println(
                  MessageFormat.format(THERMOMETER_DATA_STOP_INFO_TEMPLATE, message.getThermometer().getName(),
                      message.getThermometer().getPoint(), df.format(0), calculateDurationSumOfStoppedThermometers())));
    }
  }

  private BigDecimal calculateDurationSumOfStoppedThermometers() {
    var disabledThermometers = thermometersValues.keySet().stream().filter(Thermometer::isDisabled).toList();
    var wholeDuration = BigDecimal.ZERO;
    for (Thermometer thermometer : disabledThermometers) {
      var values = thermometersValues.get(thermometer);
      wholeDuration = wholeDuration.add(
          values.stream().map(ThermometerValue::getDuration).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
    return wholeDuration;
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
      thermometersValues.keySet().stream().filter(therm -> therm.equals(message.getThermometer()))
          .findFirst().ifPresent(this::calculateCurrentThermometerAverage);

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
          MessageFormat.format(THERMOMETER_DATA_ADD_INFO_TEMPLATE, PointType.INSIDE, df.format(insideAverage),
              PointType.OUTSIDE,
              df.format(outsideAverage), PointType.MATTER, df.format(matterAverage)));
    }
  }

  private void calculateCurrentThermometerAverage(Thermometer thermometer) {
    BigDecimal average = BigDecimal.ZERO;
    var values = thermometersValues.get(thermometer);
    var wholeDuration = values.stream().map(ThermometerValue::getDuration).reduce(BigDecimal.ZERO, BigDecimal::add);
    for (ThermometerValue v : values) {
      average =
          average.add(v.getTemperature().multiply(v.getDuration()));
    }
    thermometer.setAverage(average.divide(wholeDuration, RoundingMode.HALF_UP));
  }

  private void validateMessage(Message message) {
    if (message == null) {
      throw new RecievedMessageException("Recieved empty message");
    }
    if (message.getType() == null) {
      throw new RecievedMessageException("Recieved message has unknown type");
    }
    if (message.getThermometer() == null) {
      throw new RecievedMessageException("Recieved message has no information about thermometer");
    }
    if (message.getThermometer().getPoint() == null) {
      throw new RecievedMessageException("Recieved message has unknown thermometer point");
    }
    if (message instanceof ThermometerMessage) {
      var thermometerValue = ((ThermometerMessage) message).getValue();
      if (thermometerValue != null
          && thermometerValue.getDuration() != null
          && thermometerValue.getDuration().compareTo(BigDecimal.ZERO) <= 0) {
        throw new RecievedMessageException("Recieved message has incorrect duration value");
      }
    }
  }


}
