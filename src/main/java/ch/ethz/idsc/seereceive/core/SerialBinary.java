// code by jph
package ch.ethz.idsc.seereceive.core;

import java.io.File;

public enum SerialBinary {
  ;
  public static File getExecutable() {
    String string = System.getProperty("os.name").toLowerCase();
    if (string.equals("mac os x"))
      return new File("mac_javacom").getAbsoluteFile();
    return new File(string + "_javacom").getAbsoluteFile();
  }
}
