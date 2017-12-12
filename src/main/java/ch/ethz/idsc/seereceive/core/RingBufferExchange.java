// code by jph
package ch.ethz.idsc.seereceive.core;

public interface RingBufferExchange {
  boolean peek(byte[] data, int length);

  boolean advance(int length);

  void write(byte[] buffer);
}
