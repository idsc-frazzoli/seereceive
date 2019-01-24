// code by jph
package ch.ethz.idsc.seereceive.dev.saw;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.io.Put;

/* package */ enum ExportManager {
  ;
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

  public static String date() {
    return DATE_FORMAT.format(new Date());
  }

  public static File autoSave(Tensor stateReceived) throws IOException {
    File directory = HomeDirectory.file("seesawState", date());
    directory.mkdirs();
    Export.of(new File(directory, "seesawState.m"), stateReceived);
    Export.of(new File(directory, "seesawState.csv"), stateReceived);
    Put.of(new File(directory, "seesawState.mathematica"), stateReceived);
    return directory;
  }
}
