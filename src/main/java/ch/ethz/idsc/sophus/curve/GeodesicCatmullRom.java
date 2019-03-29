// code by ob
package ch.ethz.idsc.sophus.curve;

import java.util.Objects;

import ch.ethz.idsc.sophus.group.Se2ParametricDistance;
import ch.ethz.idsc.sophus.math.GeodesicInterface;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.TensorRuntimeException;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.opt.ScalarTensorFunction;

/** CatmullRom denotes the function that is defined
 * by control points over a sequence of knots.
 * 
 * Reference:
 * Freeform Curves on Spheres of Arbitrary Dimension
 * Scott Schaefer and Ron Goldman
 * Proceedings of Pacific Graphics 2005, pages 160-162
 * http://faculty.cs.tamu.edu/schaefer/research/sphereCurves.pdf */
public class GeodesicCatmullRom implements ScalarTensorFunction {
  /** @param geodesicInterface non null
   * @param alpha = 0: uniform knot vector; alpha = 0.5: centripetal; alpha = 1: chordal
   * @return
   * @throws Exception if given control contains more than 4 points */
  public static GeodesicCatmullRom of(GeodesicInterface geodesicInterface, Tensor control, double alpha) {
    if (control.length() != 4)
      throw TensorRuntimeException.of(control);
    return new GeodesicCatmullRom(Objects.requireNonNull(geodesicInterface), control, alpha);
  }

  // ---
  private final GeodesicInterface geodesicInterface;
  private final Tensor control;
  private final double alpha;

  /** @param degree
   * @param control points of length 4 */
  /* package */ GeodesicCatmullRom(GeodesicInterface geodesicInterface, Tensor control, double alpha) {
    this.geodesicInterface = geodesicInterface;
    this.control = control;
    this.alpha = alpha;
  }

  private static Tensor knots(Tensor sequence, double alpha) {
    // TODO OB: make geodesic display depending on geod.Interface => suiting parametric distance;
    Tensor t = Tensors.vector(0);
    if (sequence.length() != 4)
      throw TensorRuntimeException.of(sequence);
    for (int index = 0; index < 3; index++)
      t.append(RealScalar.of(Math.pow(Se2ParametricDistance.of(sequence.get(index), sequence.get(index + 1)).number().intValue(), alpha)).add(t.Get(0)));
    return t;
  }

  // TODO OB: compare with schafer and implement
  @Override
  public Tensor apply(Scalar t) {
    // These tensors need to be streamed from control
    Tensor p0 = control.get(0);
    Tensor p1 = control.get(1);
    Tensor p2 = control.get(2);
    Tensor p3 = control.get(3);
    Tensor seq = Tensors.of(p0, p1, p2, p3);
    double alpha = 0.5;
    Tensor knots = knots(seq, alpha);
    // TODO OB: these three steps can be merged in one by introducing a second loop
    // First pyramidal layer
    Tensor A = Tensors.empty();
    for (int index = 0; index < 3; index++) {
      Scalar num = t.subtract(knots.Get(index));
      Scalar denum = knots.Get(index + 1).subtract(knots.Get(index));
      A.append(geodesicInterface.split(seq.get(index), seq.get(index + 1), num.divide(denum)));
    }
    // Second pyramidal layer
    Tensor B = Tensors.empty();
    for (int index = 0; index < 2; index++) {
      Scalar num = t.subtract(t.Get(index));
      Scalar denum = knots.Get(index + 2).subtract(knots.Get(0));
      B.append(geodesicInterface.split(A.get(index), A.get(index + 1), num.divide(denum)));
    }
    // Third and final Pyramidal layer
    Scalar num = t.subtract(t.get(1));
    Scalar denum = knots.Get(2).subtract(knots.Get(1));
    Tensor C = geodesicInterface.split(B.get(0), B.get(1), num.divide(denum));
    return C;
  }

  public Tensor control() {
    return control.unmodifiable();
  }
}
