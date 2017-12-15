// code by clruch
package ch.ethz.idsc.seereceive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import ch.ethz.idsc.seereceive.core.FazeClient;
import ch.ethz.idsc.seereceive.core.RingBufferExchange;
import ch.ethz.idsc.seereceive.core.SeesawClient;

/** the port has to be configured and may even change between two connects.
 * 
 * on ubuntu ports are
 * /dev/stlinkv2_console
 * /dev/ttyACM0
 * /dev/ttyACM1 */
public enum SeesawReceive {
  ;
  /** @param args
   * @throws FileNotFoundException
   * @throws IOException */
  public static void main(String[] args) throws FileNotFoundException, IOException {
    System.out.println("now reading from the seesaw...");
    System.out.println("arguments " + Arrays.asList(args));
    String port = "";
    if (0 < args.length) {
      port = args[0];
    } else {
      Properties properties = new Properties();
      properties.load(new FileInputStream(new File("port.properties")));
      port = properties.getProperty("port");
    }
    RingBufferExchange ringBufferExchange = new FazeClient(port.trim());
    new SeesawClient(ringBufferExchange);
  }
}
