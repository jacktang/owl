// code by jph
package ch.ethz.idsc.sophus.crv.clothoid;

import java.io.Serializable;

import ch.ethz.idsc.sophus.math.HeadTailInterface;
import ch.ethz.idsc.tensor.Scalar;

/** clothoid is tangent at start and end points */
public class ClothoidTerminalRatio implements HeadTailInterface, Serializable {
  private final Scalar head;
  private final Scalar tail;

  ClothoidTerminalRatio(Scalar head, Scalar tail) {
    this.head = head;
    this.tail = tail;
  }

  @Override
  public Scalar head() {
    return head;
  }

  @Override
  public Scalar tail() {
    return tail;
  }

  @Override // from Object
  public final String toString() {
    return "{\"head\": " + head() + ", \"tail\": " + tail() + "}";
  }
}