package ch.ethz.idsc.seereceive.core;
//code by jph

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** UartServers manages UartServers based on the port number. For each port
 * number there can only be one UartServer. */
public class UartServers {
  public static UartServers instance = new UartServers();
  // ---
  Map<String, UartServer> myMap = new HashMap<String, UartServer>();

  private UartServers() {
  }

  public UartServer requestNew(UartClientInterface myUartClientInterface) {
    UartServer myUartServer = null;
    if (!myMap.containsKey(myUartClientInterface.getPort())) {
      myUartServer = UartServer.create(myUartClientInterface);
      if (myUartServer != null)
        myMap.put(myUartClientInterface.getPort(), myUartServer);
    }
    return myUartServer;
  }

  public UartServer getFirst() {
    return myMap.values().iterator().next();
  }

  public UartServer get(int port) {
    if (myMap.containsKey(port))
      return myMap.get(port);
    return null;
  }

  public void close(String port) {
    if (myMap.containsKey(port)) {
      myMap.get(port).close();
      myMap.remove(port);
    } else
      Logger.getGlobal().log(Level.SEVERE, "port " + port + " was not open!\n");
  }

  public void closeAll() {
    for (UartServer myUartServer : instance.myMap.values())
      myUartServer.close();
    myMap.clear();
  }
}