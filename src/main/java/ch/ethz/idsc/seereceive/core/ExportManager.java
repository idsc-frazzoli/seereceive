// code by jph
package ch.ethz.idsc.seereceive.core;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.ethz.idsc.seereceive.utils.UserHome;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Put;

public enum ExportManager {
  ;
  private static final DateFormat FILE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

  public static String file() {
    return FILE_FORMAT.format(new Date());
  }

  public static File autoSave(Tensor stateReceived) throws IOException {
    File home = UserHome.file("");
    File root = new File(home, "seesawState");
    root.mkdir();
    File curr = new File(root, file());
    curr.mkdir();
    Export.of(new File(curr, "seesawState.m"), stateReceived);
    Export.of(new File(curr, "seesawState.csv"), stateReceived);
    Put.of(new File(curr, "seesawState.mathematica"), stateReceived);
    return curr;
  }
}
