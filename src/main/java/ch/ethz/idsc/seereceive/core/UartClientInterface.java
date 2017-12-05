// code by jph
package ch.ethz.idsc.seereceive.core;

public interface UartClientInterface {
  String getPort();

  int getBaud();

  void rxBufferEvent();

  void exitValue();

  void initialize(UartServer uartServer);
}