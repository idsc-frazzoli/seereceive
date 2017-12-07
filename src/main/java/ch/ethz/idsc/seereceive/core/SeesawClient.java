package ch.ethz.idsc.seereceive.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ch.ethz.idsc.seereceive.utils.SaveUtils;
import ch.ethz.idsc.seereceive.utils.UserHome;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class SeesawClient implements UartClientInterface {
  private static final int COUNT = 3000; // TODO magic const
  // ---
  private final int BAUD = 9600;
  private final String PORT;
  private UartServer uartServer;
  private int tLen = SeesawMessage.length() * 2;
  private int hLen = SeesawMessage.headerlength();
  public Tensor stateReceived = Tensors.empty();

  public SeesawClient(String port) {
    PORT = port;
  }

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
        // System.out.println("starts with header");
        if (uartServer.poll(bytesReceived, tLen)) {
          // System.out.println("poll done");
          ByteBuffer byteBuffer = ByteBuffer.wrap(bytesReceived);
          byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
          if (checksumCorrect(byteBuffer)) {
            // System.out.println("checksum ok");
            processMessage(byteBuffer);
            uartServer.advance(SeesawMessage.length());
          } else {
            System.err.println("checksum nok");
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
    char c1 = (char) byteBuffer.get();
    char c2 = (char) byteBuffer.get();
    char c3 = (char) byteBuffer.get();
    System.out.println(c1 + "" + c2 + "" + c3);
    byteBuffer.position(hLen); // skip header
    SeesawState seesawState = new SeesawState(byteBuffer);
    stateReceived.append(seesawState.toTensor());
    // read time
    System.out.println("t = " + seesawState.getTime());
    System.out.println("e = " + seesawState.getError());
    System.out.println("u = " + seesawState.getControl());
    // System.out.println("test = " + seesawState.getTest());
    System.out.println("numReceived = " + stateReceived.length());
    if (stateReceived.length() == COUNT) {
      try {
        SaveUtils.saveFile(stateReceived, "seesawState", UserHome.file(""));
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
