// code by clruch, jph
package ch.ethz.idsc.seereceive.dev.saw;

import java.nio.ByteBuffer;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

public class SeesawState {
  private static final double MSEC_to_SEC = 1E-3;
  // ---
  private final long timeStamp;
  private final double r;
  private final double y;
  private final double u;

  public SeesawState(ByteBuffer byteBuffer) {
    timeStamp = byteBuffer.getInt() & 0xffffffff;
    r = byteBuffer.getDouble();
    y = byteBuffer.getDouble();
    u = byteBuffer.getDouble();
  }

  public Tensor toTensor() {
    return Tensors.vector(timeStamp * MSEC_to_SEC, r, y, u);
  }

  public Scalar getTime() {
    return Quantity.of(timeStamp * MSEC_to_SEC, "s");
  }

  public Scalar getReference() {
    return Quantity.of(r, "ticks");
  }

  public Scalar getMeasurement() {
    return Quantity.of(y, "ticks");
  }

  public Scalar getControl() {
    return Quantity.of(u, "PWM");
  }
}
