package ch.ethz.idsc.seereceive;

import ch.ethz.idsc.seereceive.core.SeesawClient;
import ch.ethz.idsc.seereceive.core.UartClientInterface;
import ch.ethz.idsc.seereceive.core.UartServer;

/** Hello world! */
public class SeesawReceive {
  public static void main(String[] args) {
    System.out.println("now reading from the seesaw...");
    UartClientInterface seesawclient = new SeesawClient();
    UartServer seesawServer = UartServer.create(seesawclient);
  }
}
