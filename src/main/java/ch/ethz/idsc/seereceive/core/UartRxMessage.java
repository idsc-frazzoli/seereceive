package ch.ethz.idsc.seereceive.core;

//code by jph
public interface UartRxMessage {
  ParseResult checkOut(UartServer myUartServer);
}