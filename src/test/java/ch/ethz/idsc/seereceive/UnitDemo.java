// code by jph
package ch.ethz.idsc.seereceive;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

enum UnitDemo {
  ;
  public static void main(String[] args) {
    Scalar q1 = Quantity.of(3, "s");
    Scalar q2 = Quantity.of(2, "m");
    System.out.println(q1);
    System.out.println(q2);
    System.out.println(q1.multiply(q2));
  }
}
