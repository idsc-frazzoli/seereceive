package ch.ethz.idsc.seereceive.core;
//code by jph

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UartServer {
  private static final int RX_SIZE = 16573; // this is a prime for absolutely
  // no reason
  private ByteBuffer rxByteBuffer = ByteBuffer.wrap(new byte[RX_SIZE]);
  private int rx_uart = 0;
  private int rx_available = 0;
  private int rx_main = 0;
  private int rx_total = 0;
  private int tx_total = 0;
  // ---
  private volatile boolean isLaunched = true;
  private Process myProcess = null;
  private InputStream myInputStream = null;
  private OutputStream myOutputStream = null;
  private final Timer myTimer = new Timer();
  private final UartClientInterface myUartClientInterface;
  private final int baud;

  private UartServer(UartClientInterface myUartClientInterface) {
    this.myUartClientInterface = myUartClientInterface;
    baud = myUartClientInterface.getBaud();
  }

  public static UartServer create(UartClientInterface myUartClientInterface) {
    final UartServer myUartServer = new UartServer(myUartClientInterface);
    try {
      myUartClientInterface.initialize(myUartServer);
      myUartServer.connect();
    } catch (Exception myException) {
      Logger.getGlobal().log(Level.SEVERE,
          "failed to launch UartServer " + myUartClientInterface.getPort() + " " + myUartClientInterface.getBaud() + " " + myException.getMessage() + "\n");
      return null;
    }
    return myUartServer;
  }

  private void connect() throws Exception {
    String myPort = myUartClientInterface.getPort();
    File exec = SerialBinary.getExecutable();
    String myCommand = exec.toString() + " " + myPort + " " + baud;
    System.out.println("now executing: ");
    System.out.println(myCommand);
    myProcess = Runtime.getRuntime().exec(myCommand);
    myInputStream = myProcess.getInputStream();
    myOutputStream = myProcess.getOutputStream();
    new Thread(new Runnable() {
      @Override
      public void run() {
        Logger.getGlobal().log(Level.INFO,
            "UartServer open at port " + myUartClientInterface.getPort() + " with baud " + myUartClientInterface.getBaud() + "\n");
        while (isLaunched)
          try {
            int length = myInputStream.available();
            if (0 < length) {
              byte[] myByte = new byte[length];
              int numRead = myInputStream.read(myByte);
              for (int c0 = 0; c0 < numRead; ++c0) {
                rxByteBuffer.put(rx_uart, myByte[c0]);
                ++rx_uart;
                rx_uart %= rxByteBuffer.capacity();
              }
              rx_available += length;
              rx_total += length;
              // ---
              myUartClientInterface.rxBufferEvent();
            }
            try {
              myProcess.exitValue();
              isLaunched = false;
              myUartClientInterface.exitValue();
            } catch (Exception myException) {
            }
            Thread.sleep(10); // this is necessary otherwise cpu
            // goes up
          } catch (Exception myException) {
            myException.printStackTrace();
          }
      }
    }).start();
  }

  /** poll should be called from within myUartClientInterface.rxBufferEvent();
   * copying only happens when at least length number of bytes are in the rx
   * buffer
   * 
   * @param myByte
   * array into which bytes from the rx buffer will be copied into
   * @param length
   * number of bytes
   * @return true if bytes were copied to myByte */
  public boolean poll(byte[] myByte, int length) {
    boolean longEnough = length <= rx_available;
    if (longEnough)
      for (int c0 = 0; c0 < length; ++c0)
        myByte[c0] = rxByteBuffer.get((rx_main + c0) % RX_SIZE);
    return longEnough;
  }

  /** typically called after a successful poll operation advance should be
   * called from within myUartClientInterface.rxBufferEvent();
   * 
   * @param myInt
   * number of bytes to discard from the rx buffer */
  public void advance(int myInt) {
    if (myInt <= rx_available) {
      rx_main += myInt;
      rx_main %= RX_SIZE;
      rx_available -= myInt;
    } else {
      rx_available = 0;
      new Exception("cannot advance more bytes than in buffer").printStackTrace();
    }
  }

  /** should be called from within myUartClientInterface.rxBufferEvent();
   * 
   * @return */
  public byte[] clearRx() {
    byte[] myByte = new byte[rx_available];
    poll(myByte, myByte.length);
    advance(myByte.length);
    return myByte;
  }

  // could remove timer because synchronized was added, could also use
  // semaphore instead, but we leave it because heavy duty testing (many
  // clients) is seldom
  // however, timer ensures that method is non-blocking
  public synchronized void put(final byte[] myByte) {
    if (isLaunched)
      myTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          if (isLaunched)
            try {
              // String myString =
              // FriendlyFormat.hexString(myByte, 0,
              // myByte.length, "%02x");
              String myString = "++"; // TODO what about the
              // friendlyFormat?
              tx_total += myByte.length;
              myOutputStream.write(myString.getBytes());
              myOutputStream.flush();
              long myLong = (long) (myByte.length) * 9 * 1000 / baud;
              // 9 bits, 1000 ms, blocks until all bytes have been
              // sent
              Thread.sleep(myLong);
            } catch (Exception myException) {
              myException.printStackTrace();
            }
        }
      }, 0);
  }

  public void close() {
    isLaunched = false;
    myTimer.cancel();
    if (myProcess != null)
      try {
        Thread.sleep(10);
        myProcess.destroy();
        myProcess.waitFor();
        Logger.getGlobal().log(Level.INFO, "UartServer terminated with " + myProcess.exitValue() + ".\n");
      } catch (Exception myException) {
        myException.printStackTrace();
      }
  }

  public int getRxTotal() {
    return rx_total;
  }

  public int getTxTotal() {
    return tx_total;
  }
};