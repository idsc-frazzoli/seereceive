package ch.ethz.idsc.seereceive.core;

//code by jph
public interface UartClientInterface {
  String getPort();

  int getBaud();

  void rxBufferEvent();

  void exitValue();

  void initialize(UartServer uartServer);
}