// code by jph
package ch.ethz.idsc.sophus.ply.crd;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;

/** Hint: only DISCRETE_HARMONIC allows {@link Quantity} as coordinates
 * 
 * Reference:
 * "Power Coordinates: A Geometric Construction of Barycentric Coordinates on Convex Polytopes"
 * Max Budninskiy, Beibei Liu, Yiying Tong, Mathieu Desbrun, 2016
 * 
 * "Interpolation via Barycentric Coordinates" by Pierre Alliez, 2017 */
public enum Barycentric implements ScalarUnaryOperator {
  /** Section 3.1
   * 
   * Quote:
   * "Ju et al. noticed that Wachspress coordinates can be expressed in terms of the polar dual of the
   * input polytope P, i.e., using a dual cell D(x) for which the distance from x to a dual face f_i is
   * d_i = 1/|v_i −x|. This is, in fact, a particular case of our generalized notion of dual, and using
   * Eq. (2) we directly conclude that Wachspress coordinates are expressed in arbitrary dimensions as" */
  WACHSPRESS() {
    @Override
    public Scalar apply(Scalar norm) {
      return norm.multiply(norm).reciprocal();
    }
  }, //
  // SEMI_15() {
  // final ScalarUnaryOperator POWER = Power.function(-1.5);
  //
  // @Override
  // public Scalar apply(Scalar norm) {
  // return POWER.apply(norm);
  // }
  // }, //
  /** Section 3.3, eqs (12)
   * mean value coordinates seem to be the most robust */
  MEAN_VALUE() {
    @Override
    public Scalar apply(Scalar norm) {
      return norm.reciprocal();
    }
  }, //
  // SEMI05() {
  // final ScalarUnaryOperator POWER = Power.function(-0.5);
  //
  // @Override
  // public Scalar apply(Scalar norm) {
  // return POWER.apply(norm);
  // }
  // }, //
  /** Section 3.2
   * 
   * Quote:
   * "The power form of discrete harmonic coordinates are in general not smooth, only continuous, see
   * Fig. 4. However, note that this variant is quite different from a simple thresholding of the
   * discrete harmonic homogeneous coordinates: instead, our power coordinates are linear precise, a
   * property that simply discarding negative homogeneous coordinates (i.e., thresholding them to zero)
   * would not enforce." */
  DISCRETE_HARMONIC() {
    @Override
    public Scalar apply(Scalar norm) {
      // the return value was originally 1/2. however, the value 1 seems to be more consistent.
      return RealScalar.ONE;
    }
  }, //
  ;
}