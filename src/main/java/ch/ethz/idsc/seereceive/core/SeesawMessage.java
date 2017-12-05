package ch.ethz.idsc.seereceive.core;

import java.util.Arrays;

import ch.ethz.idsc.seereceive.utils.GlobalAssert;

public enum SeesawMessage {
  ;
  private static final byte[] header = { 'E', 'T', 'H' };
  private static final int length = 33;

  public static int length() {
    return length;
  }

  public static int headerlength() {
    return header.length;
  }

  public static boolean startsWithHeader(byte[] byteMsg) {
    GlobalAssert.that(byteMsg.length >= headerlength());
    byte[] identifier = new byte[headerlength()];
    for (int i = 0; i < headerlength(); ++i) {
      identifier[i] = byteMsg[i];
    }
    return Arrays.equals(identifier, header);
  }

  /** @param byteMsg
   * @param charIdent
   * @return */
  public static boolean compare(byte[] byteMsg, byte[] charIdent) {
    GlobalAssert.that(byteMsg.length == charIdent.length);
    return Arrays.equals(byteMsg, charIdent);
  }
}
