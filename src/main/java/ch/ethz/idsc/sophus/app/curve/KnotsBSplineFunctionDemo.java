// code by jph
package ch.ethz.idsc.sophus.app.curve;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Arrays;

import javax.swing.JSlider;

import ch.ethz.idsc.owl.gui.GraphicsUtil;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.sophus.app.api.AbstractDemo;
import ch.ethz.idsc.sophus.app.api.DubinsGenerator;
import ch.ethz.idsc.sophus.app.api.GeodesicDisplay;
import ch.ethz.idsc.sophus.app.api.GeodesicDisplays;
import ch.ethz.idsc.sophus.app.misc.CurveCurvatureRender;
import ch.ethz.idsc.sophus.app.util.SpinnerLabel;
import ch.ethz.idsc.sophus.curve.GeodesicBSplineFunction;
import ch.ethz.idsc.sophus.curve.GeodesicDeBoor;
import ch.ethz.idsc.sophus.math.CentripetalKnotSpacingHelper;
import ch.ethz.idsc.sophus.math.KnotSpacingSchemes;
import ch.ethz.idsc.sophus.sym.SymLinkImage;
import ch.ethz.idsc.sophus.sym.SymLinkImages;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Last;
import ch.ethz.idsc.tensor.alg.Subdivide;

public class KnotsBSplineFunctionDemo extends BaseCurvatureDemo {
  private final SpinnerLabel<KnotSpacingSchemes> spinnerKnotSpacing = new SpinnerLabel<>();
  private final JSlider jSliderCentripetalExponent = new JSlider(0, 100, 100);

  public KnotsBSplineFunctionDemo() {
    super(GeodesicDisplays.CLOTH_SE2_R2);
    spinnerKnotSpacing.setList(Arrays.asList(KnotSpacingSchemes.values()));
    spinnerKnotSpacing.setValue(KnotSpacingSchemes.CHORDAL);
    spinnerKnotSpacing.addToComponentReduced(timerFrame.jToolBar, new Dimension(100, 28), "knot spacing");
    // ---
    jSliderCentripetalExponent.setPreferredSize(new Dimension(500, 28));
    timerFrame.jToolBar.add(jSliderCentripetalExponent, "CentripetalExponent");
    // ---
    Tensor dubins = Tensors.fromString("{{1,0,0},{1,0,0},{2,0,2.5708},{1,0,2.1},{1.5,0,0},{2.3,0,-1.2},{1.5,0,0},{4,0,3.14159},{2,0,3.14159},{2,0,0}}");
    setControlPointsSe2(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
        Tensor.of(dubins.stream().map(row -> row.pmul(Tensors.vector(2, 1, 1))))));
  }

  @Override // from RenderInterface
  protected Tensor protected_render(GeometricLayer geometricLayer, Graphics2D graphics, int degree, int levels, Tensor control) {
    GeodesicDisplay geodesicDisplay = geodesicDisplay();
    Tensor knots = null;
    switch (spinnerKnotSpacing.getValue()) {
    case UNIFORM:
      knots = CentripetalKnotSpacingHelper.uniform(geodesicDisplay::parametricDistance).apply(control);
      break;
    case CHORDAL:
      knots = CentripetalKnotSpacingHelper.chordal(geodesicDisplay::parametricDistance).apply(control);
      break;
    default:
      knots = CentripetalKnotSpacingHelper.centripetal(//
          geodesicDisplay::parametricDistance, RationalScalar.of(jSliderCentripetalExponent.getValue(), jSliderCentripetalExponent.getMaximum()))
          .apply(control);
      break;
    }
    final Scalar upper = (Scalar) Last.of(knots);
    final Scalar parameter = RationalScalar.of(jSlider.getValue(), jSlider.getMaximum()).multiply(upper);
    // ---
    GeodesicBSplineFunction scalarTensorFunction = //
        GeodesicBSplineFunction.of(geodesicDisplay.geodesicInterface(), degree, knots, control);
    if (jToggleSymi.isSelected()) {
      GeodesicDeBoor geodesicDeBoor = scalarTensorFunction.deBoor(parameter);
      SymLinkImage symLinkImage = SymLinkImages.deboor(geodesicDeBoor, geodesicDeBoor.degree() + 1, parameter);
      graphics.drawImage(symLinkImage.bufferedImage(), 0, 0, null);
    }
    // ---
    GraphicsUtil.setQualityHigh(graphics);
    renderControlPoints(geometricLayer, graphics); // control points
    Tensor refined = Subdivide.of(RealScalar.ZERO, upper, Math.max(1, control.length() * (1 << levels))).map(scalarTensorFunction);
    {
      Tensor selected = scalarTensorFunction.apply(parameter);
      geometricLayer.pushMatrix(geodesicDisplay.matrixLift(selected));
      Path2D path2d = geometricLayer.toPath2D(geodesicDisplay.shape());
      graphics.setColor(Color.DARK_GRAY);
      graphics.fill(path2d);
      geometricLayer.popMatrix();
    }
    Tensor render = Tensor.of(refined.stream().map(geodesicDisplay::toPoint));
    CurveCurvatureRender.of(render, false, geometricLayer, graphics);
    if (levels < 5)
      renderPoints(geodesicDisplay, refined, geometricLayer, graphics);
    return refined;
  }

  public static void main(String[] args) {
    AbstractDemo abstractDemo = new KnotsBSplineFunctionDemo();
    abstractDemo.timerFrame.jFrame.setBounds(100, 100, 1200, 600);
    abstractDemo.timerFrame.jFrame.setVisible(true);
  }
}