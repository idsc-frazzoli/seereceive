// code by jph
package ch.ethz.idsc.seereceive.util.port;

public interface SerialPortInterface extends RingBufferReader {
  /** writes given data via serial port
   * 
   * @param data
   * @return */
  int write(byte[] data);
}
