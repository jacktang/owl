// code by jph
package ch.ethz.idsc.sophus.flt.ga;

import java.io.IOException;
import java.util.function.Function;

import ch.ethz.idsc.sophus.crv.spline.MonomialExtrapolationMask;
import ch.ethz.idsc.sophus.lie.rn.RnGeodesic;
import ch.ethz.idsc.sophus.lie.se2.Se2Geodesic;
import ch.ethz.idsc.sophus.math.win.HalfWindowSampler;
import ch.ethz.idsc.sophus.math.win.SmoothingKernel;
import ch.ethz.idsc.tensor.ExactScalarQ;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Range;
import ch.ethz.idsc.tensor.io.Serialization;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;
import ch.ethz.idsc.tensor.sca.Chop;
import ch.ethz.idsc.tensor.sca.ScalarUnaryOperator;
import ch.ethz.idsc.tensor.sca.Sin;
import ch.ethz.idsc.tensor.sca.win.DirichletWindow;
import junit.framework.TestCase;

public class GeodesicExtrapolationTest extends TestCase {
  public void testEmptyFail() {
    TensorUnaryOperator tensorUnaryOperator = GeodesicExtrapolation.of(RnGeodesic.INSTANCE, DirichletWindow.FUNCTION);
    try {
      tensorUnaryOperator.apply(Tensors.empty());
      fail();
    } catch (Exception exception) {
      // ---
    }
  }

  public void testDirichlet() throws ClassNotFoundException, IOException {
    TensorUnaryOperator tensorUnaryOperator = Serialization.copy(GeodesicExtrapolation.of(RnGeodesic.INSTANCE, DirichletWindow.FUNCTION));
    {
      Tensor tensor = tensorUnaryOperator.apply(Tensors.vector(12));
      assertEquals(tensor, RealScalar.of(12));
      ExactScalarQ.require(tensor.Get());
    }
    {
      Tensor tensor = tensorUnaryOperator.apply(Tensors.vector(1, 2));
      assertEquals(tensor, RealScalar.of(3));
      ExactScalarQ.require(tensor.Get());
    }
    {
      Tensor tensor = tensorUnaryOperator.apply(Tensors.vector(1, 2, 3));
      assertEquals(tensor, RealScalar.of(4));
      ExactScalarQ.require(tensor.Get());
    }
    {
      Tensor tensor = tensorUnaryOperator.apply(Tensors.vector(1, 2, 1));
      assertEquals(tensor, RationalScalar.of(2, 3));
      ExactScalarQ.require(tensor.Get());
    }
  }

  public void testMonomial() {
    TensorUnaryOperator tensorUnaryOperator = GeodesicExtrapolation.of(RnGeodesic.INSTANCE, MonomialExtrapolationMask.INSTANCE);
    {
      Tensor tensor = tensorUnaryOperator.apply(Tensors.vector(12));
      assertEquals(tensor, RealScalar.of(12));
      ExactScalarQ.require(tensor.Get());
    }
    {
      Tensor tensor = tensorUnaryOperator.apply(Tensors.vector(1, 2));
      assertEquals(tensor, RealScalar.of(3));
      ExactScalarQ.require(tensor.Get());
    }
    {
      Tensor tensor = tensorUnaryOperator.apply(Tensors.vector(1, 2, 3));
      assertEquals(tensor, RealScalar.of(4));
      ExactScalarQ.require(tensor.Get());
    }
    {
      Tensor tensor = tensorUnaryOperator.apply(Tensors.vector(1, 2, 1));
      assertEquals(tensor, RationalScalar.of(-2, 1));
      ExactScalarQ.require(tensor.Get());
    }
  }

  public void testSimple() {
    for (SmoothingKernel smoothingKernel : SmoothingKernel.values()) {
      TensorUnaryOperator tensorUnaryOperator = GeodesicExtrapolation.of(RnGeodesic.INSTANCE, smoothingKernel);
      for (int index = 2; index < 10; ++index) {
        Scalar result = tensorUnaryOperator.apply(Range.of(0, index)).Get();
        Chop._12.requireClose(result, RealScalar.of(index));
      }
    }
  }

  public void testSingle() {
    for (SmoothingKernel smoothingKernel : SmoothingKernel.values()) {
      TensorUnaryOperator tensorUnaryOperator = GeodesicExtrapolation.of(RnGeodesic.INSTANCE, smoothingKernel);
      Scalar result = tensorUnaryOperator.apply(Tensors.vector(10)).Get();
      Chop._12.requireClose(result, RealScalar.of(10));
    }
  }

  public void testSplits2() {
    Tensor mask = Tensors.vector(.5, .5);
    Tensor result = GeodesicExtrapolation.Splits.of(mask);
    assertEquals(Tensors.vector(2), result);
  }

  public void testElaborate() {
    Function<Integer, Tensor> halfWindowSampler = HalfWindowSampler.of(SmoothingKernel.GAUSSIAN);
    Tensor mask = halfWindowSampler.apply(7);
    assertEquals(mask.length(), 7);
    Tensor result = GeodesicExtrapolation.Splits.of(mask);
    Tensor expect = Tensors.vector( //
        0.6045315182147757, 0.4610592079176246, 0.3765618899029577, //
        0.3135075836053491, 0.2603421497384919, 1.3568791242517575);
    Chop._12.requireClose(expect, result);
  }

  public void testNoExtrapolation() {
    Tensor mask = Tensors.vector(1);
    Tensor result = GeodesicExtrapolation.Splits.of(mask);
    assertEquals(Tensors.vector(1), result);
  }

  public void testAffinityFail() {
    Tensor mask = Tensors.vector(.5, .8);
    try {
      GeodesicExtrapolation.Splits.of(mask);
      fail();
    } catch (Exception exception) {
      // ----
    }
  }

  public void testNullFail1() {
    try {
      GeodesicExtrapolation.of(null, Sin.FUNCTION);
      fail();
    } catch (Exception exception) {
      // ---
    }
  }

  public void testNullFailITF() {
    try {
      GeodesicExtrapolation.of(Se2Geodesic.INSTANCE, (Function<Integer, Tensor>) null);
      fail();
    } catch (Exception exception) {
      // ---
    }
  }

  public void testNullFailSUO() {
    try {
      GeodesicExtrapolation.of(Se2Geodesic.INSTANCE, (ScalarUnaryOperator) null);
      fail();
    } catch (Exception exception) {
      // ---
    }
  }
}
