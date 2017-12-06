// code by clruch
package ch.ethz.idsc.seereceive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import ch.ethz.idsc.seereceive.core.SeesawClient;
import ch.ethz.idsc.seereceive.core.UartClientInterface;
import ch.ethz.idsc.seereceive.core.UartServer;

/** the port has to be configured and may even change between two connects. */
enum SeesawReceive {
  ;
  public static void main(String[] args) throws FileNotFoundException, IOException {
    System.out.println("now reading from the seesaw...");
    // private final String PORT = "/dev/stlinkv2_console";
    // /dev/ttyACM1
    Properties properties = new Properties();
    properties.load(new FileInputStream(new File("port.properties")));
    
    UartClientInterface seesawclient = new SeesawClient(properties.getProperty("port"));
    UartServer.create(seesawclient);
  }
}
