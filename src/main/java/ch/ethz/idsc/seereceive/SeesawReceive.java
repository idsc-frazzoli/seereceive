// code by clruch
package ch.ethz.idsc.seereceive;

import ch.ethz.idsc.seereceive.core.SeesawClient;
import ch.ethz.idsc.seereceive.core.UartClientInterface;
import ch.ethz.idsc.seereceive.core.UartServer;

/** the port has to be configured and may even change between two connects. */
enum SeesawReceive {
  ;
  public static void main(String[] args) {
    System.out.println("now reading from the seesaw...");
    // private final String PORT = "/dev/stlinkv2_console";
    // /dev/ttyACM1
    UartClientInterface seesawclient = new SeesawClient("/dev/tty.usbmodem1413");
    UartServer.create(seesawclient);
  }
}
