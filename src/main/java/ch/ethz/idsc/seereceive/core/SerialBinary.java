// code by jph
package ch.ethz.idsc.seereceive.core;

import java.io.File;

public enum SerialBinary {
  ;
  public static File getExecutable() {
    String string = System.getProperty("os.name").toLowerCase();
    return new File(string + "_javacom").getAbsoluteFile();
  }
}
