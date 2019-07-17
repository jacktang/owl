// code by gjoel
package ch.ethz.idsc.owl.bot.rn;

import java.util.stream.IntStream;

import ch.ethz.idsc.owl.rrts.adapter.AbstractTransition;
import ch.ethz.idsc.owl.rrts.core.TransitionWrap;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.TensorRuntimeException;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.alg.Subdivide;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.sca.Ceiling;

public class RnTransition extends AbstractTransition {
  public RnTransition(Tensor start, Tensor end) {
    super(start, end, Norm._2.between(start, end));
  }

  @Override // from Transition
  public Tensor sampled(Scalar minResolution) {
    int steps = Ceiling.FUNCTION.apply(length().divide(minResolution)).number().intValue();
    return Tensor.of(Subdivide.of(start(), end(), steps).stream().limit(steps));
  }

  @Override // from Transition
  public TransitionWrap wrapped(int steps) {
    if (steps < 1)
      throw TensorRuntimeException.of(length(), RealScalar.of(steps));
    Scalar resolution = length().divide(RealScalar.of(steps));
    Tensor spacing = Array.zeros(steps);
    // TODO JPH
    IntStream.range(0, steps).forEach(i -> spacing.set(i > 0 //
        ? resolution //
        : start().Get(0).zero(), i));
    return new TransitionWrap(sampled(resolution), spacing);
  }

  @Override // from RenderTransition
  public Tensor linearized(Scalar minResolution) {
    return Tensors.of(start(), end());
  }
}
