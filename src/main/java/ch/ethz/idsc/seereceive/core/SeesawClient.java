package ch.ethz.idsc.seereceive.core;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JOptionPane;

import ch.ethz.idsc.seereceive.utils.ExportManager;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class SeesawClient implements Runnable {
  private static final int COUNT = 3000; // TODO magic const
  private static final int hLen = SeesawMessage.headerlength();
  private static final int tLen = SeesawMessage.length() * 2;
  // ---
  private RingBufferExchange uartServer;
  public Tensor stateReceived = Tensors.empty();
  private final Thread thread;

  public SeesawClient(RingBufferExchange uartServer) {
    this.uartServer = uartServer;
    thread = new Thread(this);
    thread.start();
  }

  @Override
  public void run() {
    while (true) {
      byte[] bytesReceived = new byte[tLen];
      if (uartServer.peek(bytesReceived, hLen)) {
        if (SeesawMessage.startsWithHeader(bytesReceived)) {
          // System.out.println("starts with header");
          if (uartServer.peek(bytesReceived, tLen)) {
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
      } else {
        try {
          Thread.sleep(10);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static boolean checksumCorrect(ByteBuffer byteBuffer) {
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
    System.out.println("r = " + seesawState.getReference());
    System.out.println("y = " + seesawState.getMeasurement());
    System.out.println("u = " + seesawState.getControl());
    uartServer.advance(SeesawMessage.length());
    System.out.println("numReceived = " + stateReceived.length());
    if (stateReceived.length() == COUNT) {
      try {
        File dir = ExportManager.autoSave(stateReceived);
        String string = "files saved in\n" + dir;
        System.out.println(string);
        JOptionPane.showMessageDialog(null, string, "data collection complete", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}
