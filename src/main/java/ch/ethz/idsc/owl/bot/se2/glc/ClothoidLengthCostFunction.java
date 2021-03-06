// code by gjoel
package ch.ethz.idsc.owl.bot.se2.glc;

import java.util.Objects;
import java.util.function.Predicate;

import ch.ethz.idsc.sophus.crv.clothoid.Clothoid;
import ch.ethz.idsc.sophus.crv.clothoid.Clothoid.Curvature;
import ch.ethz.idsc.tensor.DoubleScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.opt.TensorScalarFunction;
import ch.ethz.idsc.tensor.qty.Quantity;

/** only applied in {@link ClothoidPursuitControl} resp. {@link Se2Letter5Demo} */
/* package */ class ClothoidLengthCostFunction implements TensorScalarFunction {
  private final Predicate<Scalar> isCompliant;

  public ClothoidLengthCostFunction(Predicate<Scalar> isCompliant) {
    this.isCompliant = Objects.requireNonNull(isCompliant);
  }

  @Override
  public Scalar apply(Tensor xya) {
    Clothoid clothoid = new Clothoid(xya.map(Scalar::zero), xya);
    Curvature curvature = clothoid.new Curvature();
    if (isCompliant.test(curvature.head()) && //
        isCompliant.test(curvature.tail()))
      return clothoid.new Curve().length();
    // TODO GJOEL filter out via collision check, units
    if (xya.Get(0) instanceof Quantity)
      return Quantity.of(DoubleScalar.POSITIVE_INFINITY, ((Quantity) xya.Get(0)).unit());
    return DoubleScalar.POSITIVE_INFINITY;
  }
}
