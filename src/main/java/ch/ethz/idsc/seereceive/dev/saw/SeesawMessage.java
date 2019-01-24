// code by clruch
package ch.ethz.idsc.seereceive.dev.saw;

import java.util.Arrays;

import ch.ethz.idsc.seereceive.util.GlobalAssert;

/* package */ enum SeesawMessage {
  ;
  private static final int LENGTH = 33;
  private static final byte[] HEADER = { 'E', 'T', 'H' };

  public static int length() {
    return LENGTH;
  }

  public static int headerlength() {
    return HEADER.length;
  }

  public static boolean startsWithHeader(byte[] byteMsg) {
    GlobalAssert.that(byteMsg.length >= headerlength());
    byte[] identifier = new byte[headerlength()];
    for (int i = 0; i < headerlength(); ++i) {
      identifier[i] = byteMsg[i];
    }
    return Arrays.equals(identifier, HEADER);
  }

  /** @param byteMsg
   * @param charIdent
   * @return */
  public static boolean compare(byte[] byteMsg, byte[] charIdent) {
    GlobalAssert.that(byteMsg.length == charIdent.length);
    return Arrays.equals(byteMsg, charIdent);
  }
}
