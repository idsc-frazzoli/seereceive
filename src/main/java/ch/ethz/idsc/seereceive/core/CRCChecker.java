package ch.ethz.idsc.seereceive.core;

//code by jph
//adpated by clruch

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CRCChecker {
	static Map<Integer, List<Integer>> myMap = new HashMap<Integer, List<Integer>>();
	// ---
	private final int ply; // only used as reference
	int crc = 0; // private
	private boolean isFinal = false;
	final List<Integer> myList;

	static List<Integer> init(int ply) {
		List<Integer> myList = new ArrayList<Integer>();
		int phi = (ply >> 8) & 0xff;
		// To produce control bytes from every possible top byte
		for (int c1 = 0; c1 < 256; ++c1) {
			int[] ctr = new int[8];
			int top = c1;
			for (int c0 = 0; c0 < 8; ++c0) {
				// Check top bit of the register
				if ((top & 0x80) == 0x80) {
					top = ((top << 1) & 0xFE) ^ phi;
					ctr[c0] = 1;
				} else {
					top <<= 1;
					ctr[c0] = 0;
				}
			}
			int crc = 0;
			for (int c0 = 0; c0 < 8; ++c0)
				if (0 != ctr[c0])
					crc ^= ply << (7 - c0);
			myList.add(crc);
		}
		return myList;
	}

	public static CRCChecker withSeed(int seed) {
		CRCChecker myCrCheck = new CRCChecker();
		myCrCheck.crc = seed & 0xffff;
		return myCrCheck;
	}

	public CRCChecker() {
		ply = 0x8005;
		if (!myMap.containsKey(ply))
			myMap.put(ply, init(ply));
		myList = myMap.get(ply);
	}

	public CRCChecker(int ply) {
		ply &= 0xffff; // must only be two bytes
		this.ply = ply;
		if (!myMap.containsKey(ply))
			myMap.put(ply, init(ply));
		myList = myMap.get(ply);
	}

	private void rotate(int data) {
		crc = ((crc << 8) + data) ^ myList.get((crc >> 8) & 0xFF);
		crc &= 0xffff;
	}

	public void update(byte[] message, int offset, int length) {
		if (isFinal)
			new Exception("crc already final").printStackTrace();
		for (int idx = 0; idx < length; ++idx)
			rotate(byte2int(message[offset + idx]));
	}

	public int publish() {
		rotate(0);
		rotate(0);
		isFinal = true;
		return crc;
	}

	/**
	 * computes 2-byte crc-value for message from byte message[0] up to byte
	 * message[length-3] with polynomial 0x8005 and compares the checksum to the
	 * values in message[length-2] and message[length-1].
	 * 
	 * @param message
	 *            array of bytes of length at least length+2
	 * @return true, if the computed checksum is identical to the value in
	 *         message[length] and message[length+1]
	 */
	public static boolean verifyChecksum(byte[] message) {
		return verifyChecksum(message, message.length);
	}

	/**
	 * computes 2-byte crc-value for message from byte message[0] up to byte
	 * message[length-1] with polynomial 0x8005 and compares the checksum to the
	 * values in message[length] and message[length+1].
	 * 
	 * @param message
	 *            array of bytes of length at least length+2
	 * @param length
	 *            number of bytes the checksum should be computed on
	 * @return true, if the computed checksum is identical to the value in
	 *         message[length] and message[length+1]
	 */
	public static boolean verifyChecksum(byte[] message, int length) {
		CRCChecker myCheck = new CRCChecker();
		myCheck.update(message, 0, message.length);
		return 0 == myCheck.publish();
	}

	/**
	 * computes 2-byte crc-value for message from byte message[0] up to byte
	 * message[length-1] with polynomial 0x8005 and writes the checksum to
	 * message[length] and message[length+1].
	 * 
	 * @param message
	 *            array of bytes of length at least length+2
	 * @param length
	 *            number of bytes the checksum should be computed on
	 * @return 2-bytes crc-value as integer
	 */
	public static int append(byte[] message, int length) {
		if (message.length < length + 2) {
			new Exception("allocate memory for checksum! message not altered, function returns checksum 0.")
					.printStackTrace();
			return 0;
		}
		CRCChecker myCheck = new CRCChecker();
		myCheck.update(message, 0, length);
		int crc = myCheck.publish();
		ByteBuffer myByteBuffer = ByteBuffer.wrap(message, length, 2);
		myByteBuffer.putShort((short) crc);
		return crc;
	}

	/**
	 * Casts a byte to an unsigned int value. For instance, the byte 0xff will
	 * result in 255 instead of -1
	 * 
	 * @param myByte
	 *            the byte, for instance 0xff
	 * @return the non-negative interpretation of byte
	 */
	private static int byte2int(byte myByte) {
		int myInt = myByte;
		return myInt < 0 ? myInt + 256 : myInt;
	}

}