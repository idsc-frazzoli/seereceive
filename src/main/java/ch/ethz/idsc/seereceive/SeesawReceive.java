package ch.ethz.idsc.seereceive;

import ch.ethz.idsc.seereceive.core.SeesawClient;
import ch.ethz.idsc.seereceive.core.UartClientInterface;
import ch.ethz.idsc.seereceive.core.UartServer;

public class SeesawReceive {
  public static void main(String[] args) {
    System.out.println("now reading from the seesaw...");
    // private final String PORT = "/dev/stlinkv2_console";
    UartClientInterface seesawclient = new SeesawClient("/dev/ttyACM0");
    UartServer.create(seesawclient);
  }
}
