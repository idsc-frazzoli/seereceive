//code by jph
package ch.ethz.idsc.seereceive.utils;

public class GlobalAssert {
  public static void that(boolean status) {
    assert status;
    if (!status)
      throw new RuntimeException();
  }
}
