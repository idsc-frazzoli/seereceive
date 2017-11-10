package ch.ethz.idsc.seereceive.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.ethz.idsc.seereceive.utils.SaveUtils;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class SeesawClient implements UartClientInterface {

	private final int BAUD = 9600;
	private final String PORT = "/dev/stlinkv2_console";
	private UartServer uartServer;
	private int tLen = SeesawMessage.length() * 2;
	private int hLen = SeesawMessage.headerlength();

	public Tensor eReceived = Tensors.empty();
	public Tensor uReceived = Tensors.empty();
	public Tensor tReceived = Tensors.empty();
	public Tensor testReceived = Tensors.empty();

	public void initialize(UartServer uartServer) {
		this.uartServer = uartServer;
	}

	@Override
	public String getPort() {
		return PORT;
	}

	@Override
	public int getBaud() {
		return BAUD;
	}

	@Override
	public void rxBufferEvent() {

		byte[] bytesReceived = new byte[tLen];
		if (uartServer.poll(bytesReceived, hLen)) {
			if (SeesawMessage.startsWithHeader(bytesReceived)) {
				if (uartServer.poll(bytesReceived, tLen)) {
					ByteBuffer byteBuffer = ByteBuffer.wrap(bytesReceived);
					byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
					if (checksumCorrect(byteBuffer)) {
						processMessage(byteBuffer);
					} else {
						uartServer.advance(1);
					}

				} else {
					// System.out.println("wait");
					// message is not complete
				}
			} else {
				// System.out.println("invalid hdr");
				uartServer.advance(1);
			}
		}
	}

	@Override
	public void exitValue() {
		System.out.println(" we are now exiting!");
		// TODO do we need to add anything here?
	}

	private boolean checksumCorrect(ByteBuffer byteBuffer) {

		byte[] message = new byte[SeesawMessage.length()];
		for (int i = 0; i < SeesawMessage.length(); ++i) {
			message[i] = byteBuffer.get(i);
		}
		CRCChecker myCheck = new CRCChecker();
		myCheck.update(message, 0, SeesawMessage.length() - 2);
		int crc = myCheck.publish();

		byteBuffer.position(hLen + 4 + 8 + 8 + 8);
		short crcBoard = byteBuffer.getShort();

		String crcSCompu = String.format("%04x", crc);
		String crcSBoard = String.format("%04x", crcBoard);

		boolean works = (crcSCompu.equals(crcSBoard));

		return works;

	}

	private void processMessage(ByteBuffer byteBuffer) {

		System.out.println("=====");

		// print header
		System.out.println((char) byteBuffer.get(0));
		System.out.println((char) byteBuffer.get(1));
		System.out.println((char) byteBuffer.get(2));

		// read time
		byteBuffer.position(hLen); // skip header
		int signed = byteBuffer.getInt();
		long t = signed & 0xffff;
		tReceived.append(RealScalar.of(t));
		System.out.println("signed = " + signed);
		System.out.println("t = " + t);

		// read e
		byteBuffer.position(hLen + 4);
		double e = byteBuffer.getDouble();
		eReceived.append(RealScalar.of(e));
		System.out.println("e = " + e);

		// read test
		byteBuffer.position(hLen + 4 + 8);
		double test = byteBuffer.getDouble();
		testReceived.append(RealScalar.of(test));
		System.out.println("test = " + test);

		// read u
		byteBuffer.position(hLen + 4 + 8 + 8);
		double u = byteBuffer.getDouble();
		uReceived.append(RealScalar.of(u));
		System.out.println("u = " + u);

		uartServer.advance(SeesawMessage.length());

		System.out.println("numReceived = " + eReceived.length());

		if (eReceived.length() == 10000) {
			try {
				SaveUtils.saveFile(eReceived, "e", MultiFileTools.getWorkingDirectory());
				SaveUtils.saveFile(uReceived, "u", MultiFileTools.getWorkingDirectory());
				SaveUtils.saveFile(tReceived, "t", MultiFileTools.getWorkingDirectory());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}
