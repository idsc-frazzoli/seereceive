// code by clruch
package ch.ethz.idsc.seereceive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import com.fazecast.jSerialComm.SerialPort;

import ch.ethz.idsc.seereceive.dev.saw.SeesawClient;
import ch.ethz.idsc.seereceive.util.port.RingBufferReader;
import ch.ethz.idsc.seereceive.util.port.SerialPortWrap;

/** the port has to be configured and may even change between two connects.
 * 
 * on ubuntu ports are
 * /dev/stlinkv2_console
 * /dev/ttyACM0
 * /dev/ttyACM1 */
public enum SeesawReceive {
  ;
  private static final String PORT_KEY = "port";
  private static final int NUM_DATA_BITS = 8;
  private static final int BAUD_RATE = 9600;

  private static SerialPort create(String port) {
    SerialPort serialPort = SerialPort.getCommPort(port);
    serialPort.setComPortParameters(BAUD_RATE, NUM_DATA_BITS, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
    serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    boolean success = serialPort.openPort();
    if (!success)
      throw new RuntimeException("cannot open port: " + port);
    return serialPort;
  }

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
      File file = new File("port.properties");
      if (!file.exists()) {
        System.err.println("file missing: " + file);
        System.exit(0);
      }
      properties.load(new FileInputStream(file));
      if (properties.containsKey(PORT_KEY)) {
        port = properties.getProperty(PORT_KEY);
      } else {
        System.err.println(file + " does not specify port");
        System.exit(0);
      }
    }
    System.out.println("using port: " + port);
    RingBufferReader ringBufferReader = new SerialPortWrap(create(port.trim()));
    new SeesawClient(ringBufferReader);
  }
}
