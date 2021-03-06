// code by gjoel
package ch.ethz.idsc.owl.math.pursuit;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

public final class NaiveEntryFinder extends TrajectoryEntryFinder {
  public static final TrajectoryEntryFinder INSTANCE = new NaiveEntryFinder();

  // ---
  private NaiveEntryFinder() {
    // ---
  }

  @Override // from TrajectoryEntryFinder
  protected TrajectoryEntry protected_apply(Tensor waypoints, Scalar var) {
    int index = var.number().intValue();
    return new TrajectoryEntry(0 <= index && index < waypoints.length()//
        ? waypoints.get(index)
        : null, var);
  }

  @Override // from TrajectoryEntryFinder
  protected Stream<Scalar> sweep_variables(Tensor waypoints) {
    return IntStream.range(0, waypoints.length()).mapToObj(RealScalar::of);
  }
}
