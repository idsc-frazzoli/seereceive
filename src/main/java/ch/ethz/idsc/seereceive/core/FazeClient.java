// code by swisstrolley+
// code by jph
package ch.ethz.idsc.seereceive.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.fazecast.jSerialComm.SerialPort;

public class FazeClient implements AutoCloseable, Runnable, RingBufferExchange {
  private static final int BUFFER_SIZE = 16384;
  private static final int NUM_DATA_BITS = 8;
  private static final int BAUD_RATE = 9600;
  // ---
  private final SerialPort serialPort;
  private boolean isLaunched = true;
  private final Thread thread;
  // ---
  private ByteBuffer rxByteBuffer = ByteBuffer.wrap(new byte[BUFFER_SIZE]);
  private int rxHead = 0;
  private int rxTail = 0;
  private int nBytesInBuffer = 0;
  private int writeError;
  private Object lock = new Object();

  public FazeClient(String port) {
    serialPort = SerialPort.getCommPort(port);
    serialPort.setComPortParameters(BAUD_RATE, NUM_DATA_BITS, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
    serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    boolean success = serialPort.openPort();
    if (!success)
      throw new RuntimeException("cannot open port: " + port);
    rxByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    // ---
    thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    byte[] buffer = new byte[1];
    while (isLaunched) {
      int nRead = serialPort.readBytes(buffer, 1);
      if (nRead == 1) {
        // System.out.println(buffer[0]);
        synchronized (lock) {
          rxByteBuffer.put(rxHead, buffer[0]);
          rxHead++;
          rxHead %= rxByteBuffer.capacity();
          nBytesInBuffer++;
        }
      } else {
        try {
          Thread.sleep(0, 500_000);
        } catch (InterruptedException e) {
          // ---
        }
      }
    }
  }

  public boolean closePort() {
    boolean success = serialPort.closePort();
    if (!success) {
      return false;
    }
    isLaunched = false;
    return true;
  }

  @Override
  public void close() {
    isLaunched = false;
    closePort();
  }

  @Override
  public boolean peek(byte[] data, int length) {
    synchronized (lock) {
      if (nBytesInBuffer >= length) {
        for (int i = 0; i < length; ++i) {
          data[i] = rxByteBuffer.get((rxTail + i) % rxByteBuffer.capacity());
        }
        return true;
      }
      return false;
    }
  }

  @Override
  public boolean advance(int length) {
    synchronized (lock) {
      rxTail += length;
      rxTail %= rxByteBuffer.capacity();
      nBytesInBuffer -= length;
      return true;
    }
  }

  @Override
  public synchronized void write(byte[] buffer) {
    int bytesWritten = serialPort.writeBytes(buffer, buffer.length);
    if (bytesWritten == -1) {
      // Error writing to serial port
      if (writeError == 0) {
        writeError = 1;
      }
    }
  }
}
