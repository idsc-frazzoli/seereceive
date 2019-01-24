// code by jph
package ch.ethz.idsc.seereceive.dev.vmu;

import java.nio.ByteBuffer;

import com.fazecast.jSerialComm.SerialPort;

import ch.ethz.idsc.seereceive.util.port.SerialPortWrap;
import ch.ethz.idsc.seereceive.util.port.SerialPorts;

/** based on the document
 * Inertial Measurement Unit VMU931
 * User Guide
 * Version 1.3, March 2018, Variense inc. */
public class Vmu931 implements Runnable {
  private static final int MESSAGE_DATA_BEG = 1;
  private static final int MESSAGE_DATA_END = 4;
  private static final int MESSAGE_TEXT_BEG = 2;
  private static final int MESSAGE_TEXT_END = 3;
  /** accelerometer */
  private static final byte TYPE_A = 'a';
  /** gyroscope */
  private static final byte TYPE_G = 'g';
  /** magnetometer */
  private static final byte TYPE_C = 'c';
  /** quaternion */
  private static final byte TYPE_Q = 'q';
  /** euler angle */
  private static final byte TYPE_E = 'e';
  /** heading */
  private static final byte TYPE_H = 'h';
  /** self test */
  private static final byte TYPE_T = 't';
  /** status of sensor
   * results in a reply of size == 11 */
  private static final byte TYPE_S = 's';
  /***************************************************/
  private final byte[] data = new byte[256];
  // private final
  private final SerialPortWrap serialPortWrap;
  private final Thread thread;

  public byte[] command(byte type) {
    return new byte[] { 'v', 'a', 'r', type };
  }

  /** @param serialPort open */
  public Vmu931(SerialPort serialPort) {
    serialPortWrap = new SerialPortWrap(serialPort);
    serialPortWrap.write(command(TYPE_A));
    serialPortWrap.write(command(TYPE_H));
    serialPortWrap.write(command(TYPE_S));
    thread = new Thread(this);
    thread.start();
  }

  public void handle_data(byte[] data) {
    char type = (char) (data[2] & 0xff);
    ByteBuffer byteBuffer = ByteBuffer.wrap(data); // big endian
    byteBuffer.position(3);
    switch (type) {
    case TYPE_A:
    case TYPE_G:
    case TYPE_C:
    case TYPE_E: {
      int timestamp_ms = byteBuffer.getInt();
      float x = byteBuffer.getFloat();
      float y = byteBuffer.getFloat();
      float z = byteBuffer.getFloat();
      // System.out.println(type + " " + timestamp_ms + " " + x + " " + y);
      break;
    }
    case TYPE_Q: {
      int timestamp_ms = byteBuffer.getInt();
      float w = byteBuffer.getFloat();
      float x = byteBuffer.getFloat();
      float y = byteBuffer.getFloat();
      float z = byteBuffer.getFloat();
      // System.out.println(type + " " + timestamp_ms + " " + x + " " + y);
      break;
    }
    case TYPE_H: {
      int timestamp_ms = byteBuffer.getInt();
      float heading = byteBuffer.getFloat(); // in [deg]
      // System.out.println(type + " " + timestamp_ms + " " + heading);
      break;
    }
    case TYPE_S: {
      byte status = byteBuffer.get(); // should equal 7 (3 lowest bits set)
      byte resolution = byteBuffer.get();
      byte rate = byteBuffer.get();
      int current = byteBuffer.getInt();
      System.out.println(type + " " + status + " " + resolution + " " + rate + " " + current);
      break;
    }
    default:
      break;
    }
  }

  @Override
  public void run() {
    try {
      while (true) {
        if (serialPortWrap.peek(data, 1)) {
          int head = data[0] & 0xff;
          switch (head) {
          case MESSAGE_DATA_BEG:
            if (serialPortWrap.peek(data, 3)) {
              int size = data[1] & 0xff;
              byte type = data[2];
              if (serialPortWrap.peek(data, size)) {
                int term = data[size - 1];
                if (term == MESSAGE_DATA_END) {
                  if (size != 12 && size != 20 && size != 24)
                    System.out.println("SIZE=" + size + " " + (char) type);
                  serialPortWrap.advance(size);
                  handle_data(data);
                } else {
                  serialPortWrap.advance(1);
                  System.err.println("discard because term");
                }
              } else
                Thread.sleep(1);
            }
            break;
          case MESSAGE_TEXT_BEG:
            if (serialPortWrap.peek(data, 3)) {
              System.out.println("TEXT");
              int size = data[1] & 0xff;
              char type = (char) (data[2] & 0xff);
              if (serialPortWrap.peek(data, size)) {
                int term = data[size - 1];
                if (term == MESSAGE_TEXT_END) {
                  System.out.println("SIZE=" + size + " " + type + " " + term);
                  serialPortWrap.advance(size);
                } else {
                  serialPortWrap.advance(1);
                  System.err.println("discard");
                }
              } else
                Thread.sleep(1);
            }
            break;
          default:
            serialPortWrap.advance(1);
            System.err.println("discard because head");
            break;
          }
        } else
          Thread.sleep(1);
      }
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  // ---
  public static void main(String[] args) {
    SerialPort serialPort = SerialPorts.create("/dev/ttyACM0");
    new Vmu931(serialPort);
  }
}
