package ch.ethz.idsc.seereceive.core;

import java.nio.ByteBuffer;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

public class SeesawState {
	private static final double MAGIC = 60.0/6600.0; // 60 Grad / 6600 MC output
	private final long timeStamp;
	private final double e;
	private final double u;
	private final double test;

	public SeesawState(ByteBuffer byteBuffer) {
		timeStamp = byteBuffer.getInt() & 0xffffffff;
		e = byteBuffer.getDouble();
		u = byteBuffer.getDouble();
		test = byteBuffer.getDouble();
	}

	public Tensor toTensor() {
		return Tensors.vector(timeStamp, e, u, test);
	}

	public Scalar getTime() {
		return Quantity.of(timeStamp*0.001, "s");
	}

	public Scalar getError() {
		return Quantity.of(e * MAGIC, "deg");
	}

	public Scalar getControl() {
		return Quantity.of(u, "N"); // TODO really netwon ? 
	}

	public Scalar getTest() {
		return RealScalar.of(test);
	}

}
