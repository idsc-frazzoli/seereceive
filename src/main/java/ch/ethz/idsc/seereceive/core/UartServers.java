// code by jph
package ch.ethz.idsc.seereceive.core;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** UartServers manages UartServers based on the port number. For each port
 * number there can only be one UartServer. */
public class UartServers {
  public static final UartServers INSTANCE = new UartServers();
  // ---
  private final Map<String, UartServer> map = new HashMap<>();

  private UartServers() {
  }

  public UartServer requestNew(UartClientInterface uartClientInterface) {
    UartServer uartServer = null;
    if (!map.containsKey(uartClientInterface.getPort())) {
      uartServer = UartServer.create(uartClientInterface);
      if (uartServer != null)
        map.put(uartClientInterface.getPort(), uartServer);
    }
    return uartServer;
  }

  public UartServer getFirst() {
    return map.values().iterator().next();
  }

  public void close(String port) {
    if (map.containsKey(port)) {
      map.get(port).close();
      map.remove(port);
    } else
      Logger.getGlobal().log(Level.SEVERE, "port " + port + " was not open!\n");
  }

  public void closeAll() {
    for (UartServer uartServer : INSTANCE.map.values())
      uartServer.close();
    map.clear();
  }
}