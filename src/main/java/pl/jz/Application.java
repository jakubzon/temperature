package pl.jz;

import pl.jz.bl.DataProcesser;
import pl.jz.bl.DataReader;

public class Application {

  private static final DataReader dataReader = new DataReader();
  private static final DataProcesser dataProcesser = new DataProcesser();

  public static void main(String[] args) {
    var messages = dataReader.readMessagesFromFile();
    messages.forEach(dataProcesser::processMessage);

    System.out.println(1);

  }


}
