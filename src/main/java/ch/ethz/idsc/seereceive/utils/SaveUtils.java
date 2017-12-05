// code by clruch
package ch.ethz.idsc.seereceive.utils;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import ch.ethz.idsc.tensor.Tensor;

// TODO @ Lukas, clean it up
public enum SaveUtils {
  ;
  /** saves the matrix in three formats to a folder with the specified name in the directory saveToFolder
   * 
   * @param matrix
   * @param name
   * @param saveToFolder
   * @throws Exception */
  public static void saveFile(Tensor matrix, String name, File saveToFolder) throws Exception {
    saveFile(matrix, name, saveToFolder, SaveFormats.values());
  }

  /** // TODO make comments.
   * @param matrix
   * @param name
   * @param saveToFolder
   * @param formats
   * @throws Exception */
  public static void saveFile(Tensor matrix, String name, File saveToFolder, SaveFormats... formats) throws Exception {
    GlobalAssert.that(saveToFolder.isDirectory() && saveToFolder.exists());
    File folder = createFileDir(name, saveToFolder);
    Set<SaveFormats> saveFormats = EnumSet.copyOf(Arrays.asList(formats));
    for (SaveFormats format : saveFormats) {
      format.save(matrix, folder, name);
      System.out.println(format.toString());
    }
  }

  private static File createFileDir(String name, File saveToFolder) {
    File folder = new File(saveToFolder, name);
    int i = 0;
    while (folder.exists() && folder.isDirectory()) {
      folder = new File(folder + " (copy)");
      ++i;
      GlobalAssert.that(i < 20);
    }
    folder.mkdir();
    return folder;
  }
}
