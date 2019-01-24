//code by jph
//adpated by clruch
package ch.ethz.idsc.seereceive.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrcChecker {
  private static final Map<Integer, List<Integer>> MAP = new HashMap<>();
  // ---
  private final int ply; // only used as reference
  private int crc = 0;
  private boolean isFinal = false;
  private final List<Integer> list;

  static List<Integer> init(int ply) {
    List<Integer> list = new ArrayList<>();
    int phi = (ply >> 8) & 0xff;
    // To produce control bytes from every possible top byte
    for (int c1 = 0; c1 < 256; ++c1) {
      int[] ctr = new int[8];
      int top = c1;
      for (int c0 = 0; c0 < 8; ++c0) {
        // Check top bit of the register
        if ((top & 0x80) == 0x80) {
          top = ((top << 1) & 0xFE) ^ phi;
          ctr[c0] = 1;
        } else {
          top <<= 1;
          ctr[c0] = 0;
        }
      }
      int crc = 0;
      for (int c0 = 0; c0 < 8; ++c0)
        if (0 != ctr[c0])
          crc ^= ply << (7 - c0);
      list.add(crc);
    }
    return list;
  }

  public static CrcChecker withSeed(int seed) {
    CrcChecker crcChecker = new CrcChecker();
    crcChecker.crc = seed & 0xffff;
    return crcChecker;
  }

  public CrcChecker() {
    ply = 0x8005;
    if (!MAP.containsKey(ply))
      MAP.put(ply, init(ply));
    list = MAP.get(ply);
  }

  public CrcChecker(int ply) {
    ply &= 0xffff; // must only be two bytes
    this.ply = ply;
    if (!MAP.containsKey(ply))
      MAP.put(ply, init(ply));
    list = MAP.get(ply);
  }

  private void rotate(int data) {
    crc = ((crc << 8) + data) ^ list.get((crc >> 8) & 0xFF);
    crc &= 0xffff;
  }

  public void update(byte[] message, int offset, int length) {
    if (isFinal)
      new Exception("crc already final").printStackTrace();
    for (int idx = 0; idx < length; ++idx)
      rotate(message[offset + idx] & 0xff);
  }

  public int publish() {
    rotate(0);
    rotate(0);
    isFinal = true;
    return crc;
  }
}