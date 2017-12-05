package ch.ethz.idsc.seereceive.utils;

import java.io.File;
import java.io.IOException;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Put;

public enum SaveFormats {
  MATHEMATICA {
    @Override
    public File save(Tensor tensor, File folder, String name) throws IOException {
      File file = new File(folder, name + ".mathematica");
      Put.of(file, tensor);
      return file;
    }
  },
  CSV {
    @Override
    public File save(Tensor tensor, File folder, String name) throws IOException {
      File file = new File(folder, name + ".csv");
      Export.of(file, tensor);
      return file;
    }
  },
  MATLAB {
    @Override
    public File save(Tensor tensor, File folder, String name) throws IOException {
      File file = new File(folder, name + ".m");
      Export.of(file, tensor);
      return file;
    }
  };
  public abstract File save(Tensor tensor, File folder, String name) throws IOException;
}
