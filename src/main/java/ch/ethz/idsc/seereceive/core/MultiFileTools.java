package ch.ethz.idsc.seereceive.core;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import ch.ethz.idsc.seereceive.utils.GlobalAssert;

/** @author Claudio Ruch */
public enum MultiFileTools {
  ;
  /** @return {@link File} of current working directory
   * @throws IOException */
  public static File getWorkingDirectory() throws IOException {
    return new File("").getCanonicalFile();
  }

  /** @return all directories in filesDirectory sorted by name */
  public static File[] getAllDirectoriesSorted(File filesDirectory) {
    GlobalAssert.that(filesDirectory.isDirectory());
    return Stream.of(filesDirectory.listFiles())//
        .filter(f -> f.isDirectory())//
        .sorted().toArray(File[]::new);
  }
}
