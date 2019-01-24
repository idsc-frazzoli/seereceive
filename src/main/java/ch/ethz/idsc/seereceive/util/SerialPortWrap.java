// code by swisstrolley+
// code by jph
package ch.ethz.idsc.seereceive.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortWrap implements AutoCloseable, Runnable, SerialPortInterface {
  private static final int BUFFER_SIZE = 4096;
  // ---
  private final SerialPort serialPort;
  private final Thread thread;
  /** storage of received bytes */
  private final ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[BUFFER_SIZE]);
  // ---
  private boolean isLaunched = true;
  private int rxHead = 0;
  private int rxTail = 0;
  private int nBytesInBuffer = 0;

  /** @param port for instance "/dev/ttyACM0" */
  public SerialPortWrap(SerialPort serialPort) {
    if (!serialPort.isOpen())
      throw new RuntimeException();
    this.serialPort = serialPort;
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    // ---
    thread = new Thread(this);
    thread.start();
  }

  @Override // from Runnable
  public void run() {
    byte[] buffer = new byte[1];
    while (isLaunched) {
      int nRead = serialPort.readBytes(buffer, 1);
      if (nRead == 1) {
        // System.out.println(buffer[0]);
        synchronized (byteBuffer) {
          byteBuffer.put(rxHead, buffer[0]);
          ++rxHead;
          rxHead %= BUFFER_SIZE;
          ++nBytesInBuffer;
        }
      } else
        try {
          Thread.sleep(0, 500_000);
        } catch (InterruptedException e) {
          // ---
        }
    }
  }

  @Override // from AutoCloseable
  public void close() {
    isLaunched = false;
    thread.interrupt();
    serialPort.closePort();
  }

  @Override // from RingBufferExchange
  public boolean peek(byte[] data, int length) {
    synchronized (byteBuffer) {
      if (length <= nBytesInBuffer) {
        for (int i = 0; i < length; ++i)
          data[i] = byteBuffer.get((rxTail + i) % BUFFER_SIZE);
        return true;
      }
    }
    return false;
  }

  @Override // from RingBufferExchange
  public void advance(int length) {
    synchronized (byteBuffer) {
      rxTail += length;
      rxTail %= BUFFER_SIZE;
      nBytesInBuffer -= length;
    }
  }

  @Override // from RingBufferExchange
  public synchronized int write(byte[] data) {
    return serialPort.writeBytes(data, data.length);
  }
}
