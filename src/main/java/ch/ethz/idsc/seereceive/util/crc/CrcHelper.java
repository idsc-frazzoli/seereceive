// code by jph
package ch.ethz.idsc.seereceive.util.crc;

import java.nio.ByteBuffer;

public enum CrcHelper {
  ;
  /** computes 2-byte crc-value for message from byte message[0] up to byte
   * message[length-3] with polynomial 0x8005 and compares the checksum to the
   * values in message[length-2] and message[length-1].
   * 
   * @param message
   * array of bytes of length at least length+2
   * @return true, if the computed checksum is identical to the value in
   * message[length] and message[length+1] */
  public static boolean verifyChecksum(byte[] message) {
    return verifyChecksum(message, message.length);
  }

  /** computes 2-byte crc-value for message from byte message[0] up to byte
   * message[length-1] with polynomial 0x8005 and compares the checksum to the
   * values in message[length] and message[length+1].
   * 
   * @param message
   * array of bytes of length at least length+2
   * @param length
   * number of bytes the checksum should be computed on
   * @return true, if the computed checksum is identical to the value in
   * message[length] and message[length+1] */
  public static boolean verifyChecksum(byte[] message, int length) {
    CrcChecker crcChecker = new CrcChecker();
    crcChecker.update(message, 0, message.length);
    return 0 == crcChecker.publish();
  }

  /** computes 2-byte crc-value for message from byte message[0] up to byte
   * message[length-1] with polynomial 0x8005 and writes the checksum to
   * message[length] and message[length+1].
   * 
   * @param message
   * array of bytes of length at least length+2
   * @param length
   * number of bytes the checksum should be computed on
   * @return 2-bytes crc-value as integer */
  public static int append(byte[] message, int length) {
    if (message.length < length + 2) {
      new Exception("allocate memory for checksum! message not altered, function returns checksum 0.").printStackTrace();
      return 0;
    }
    CrcChecker crcChecker = new CrcChecker();
    crcChecker.update(message, 0, length);
    int crc = crcChecker.publish();
    ByteBuffer myByteBuffer = ByteBuffer.wrap(message, length, 2);
    myByteBuffer.putShort((short) crc);
    return crc;
  }
}
