package ch.ethz.idsc.seereceive.core;

import java.nio.ByteBuffer;

import ch.ethz.idsc.owly.data.GlobalAssert;

public enum DataTransform {
  ;
  public static char frombyte(byte b) {
    return (char) (b & 0xff);
  }

  public static byte fromchar(char c) {
    return (byte) c;
  }

  public static double frombyte(byte[] bytes) {
    GlobalAssert.that(bytes.length == 8);
    return ByteBuffer.wrap(bytes).getDouble();
  }

  public static byte[] fromDouble(double d) {
    byte[] bytes = new byte[8];
    ByteBuffer.wrap(bytes).putDouble(d);
    return bytes;
  }
}
