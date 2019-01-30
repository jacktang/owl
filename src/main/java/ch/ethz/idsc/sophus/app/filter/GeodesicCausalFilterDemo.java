// code by jph
package ch.ethz.idsc.sophus.app.filter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.ethz.idsc.owl.gui.GraphicsUtil;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.sophus.app.api.AbstractDemo;
import ch.ethz.idsc.sophus.app.api.GeodesicDisplay;
import ch.ethz.idsc.sophus.filter.GeodesicFIRnFilter;
import ch.ethz.idsc.sophus.filter.GeodesicIIRnFilter;
import ch.ethz.idsc.sophus.group.LieDifferences;
import ch.ethz.idsc.sophus.group.Se2CoveringExponential;
import ch.ethz.idsc.sophus.group.Se2Group;
import ch.ethz.idsc.sophus.group.Se2Utils;
import ch.ethz.idsc.sophus.math.SmoothingKernel;
import ch.ethz.idsc.sophus.math.WindowSideSampler;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;

class GeodesicCausalFilterDemo extends DatasetKernelDemo {
  private final JToggleButton jToggleStep = new JToggleButton("step");
  private final JToggleButton jToggleIIR = new JToggleButton("IIR");
  private Scalar alpha = RationalScalar.of(3, 4);

  GeodesicCausalFilterDemo() {
    // JTextField jTextField = new JTextField(10);
    // jTextField.setPreferredSize(new Dimension(100, 28));
    // timerFrame.jToolBar.add(jTextField);
    // ---
    jToggleStep.setSelected(false);
    timerFrame.jToolBar.add(jToggleStep);
    // ---
    jToggleIIR.setSelected(true);
    timerFrame.jToolBar.add(jToggleIIR);
    // ---
  }

  @Override
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    Tensor control = control();
    GeodesicDisplay geodesicDisplay = geodesicDisplay();
    final SmoothingKernel smoothingKernel = spinnerFilter.getValue();
    final Tensor shape = geodesicDisplay.shape().multiply(RealScalar.of(.3));
    final int radius = spinnerRadius.getValue();
    if (jToggleStep.isSelected()) {
      _control = Tensors.of(Array.zeros(3));
      for (int i = 0; i < 300; ++i) {
        if (i < 100) {
          _control.append(Tensors.vector(i * 0.01, 0, 0));
        } else //
        if (i > 200) {
          _control.append(Tensors.vector(i * 0.01, 0, 0));
        } else {
          _control.append(Tensors.vector(i * 0.01, 1, 0));
        }
      }
    }
    if (jToggleWait.isSelected())
      return;
    GraphicsUtil.setQualityHigh(graphics);
    if (jToggleData.isSelected()) {
      if (jToggleLine.isSelected())
        pathRenderCurve.setCurve(control, false).render(geometricLayer, graphics);
      for (Tensor point : control) {
        geometricLayer.pushMatrix(geodesicDisplay.matrixLift(point));
        Path2D path2d = geometricLayer.toPath2D(shape);
        path2d.closePath();
        graphics.setColor(new Color(255, 128, 128, 64));
        graphics.fill(path2d);
        graphics.setColor(COLOR_SHAPE);
        graphics.draw(path2d);
        geometricLayer.popMatrix();
      }
    }
    WindowSideSampler windowSideSampler = new WindowSideSampler(smoothingKernel);
    Tensor mask = windowSideSampler.apply(radius);
    mask.append(alpha);
    TensorUnaryOperator geodesicCenterFilter;
    if (jToggleIIR.isSelected()) {
      geodesicCenterFilter = new GeodesicIIRnFilter(geodesicDisplay.geodesicInterface(), mask);
    } else {
      geodesicCenterFilter = new GeodesicFIRnFilter(geodesicDisplay.geodesicInterface(), mask);
    }
    final Tensor refined = Tensor.of(control.stream().map(geodesicCenterFilter));
    // // TensorUnaryOperator geodesicCenterFilter = new GeodesicIIR2Filter(Se2Geodesic.INSTANCE, alpha);
    // // TensorUnaryOperator geodesicCenterFilter = new GeodesicFIR3Filter(Se2Geodesic.INSTANCE, alpha, beta);
    // // TensorUnaryOperator geodesicCenterFilter = new GeodesicIIR3Filter(Se2Geodesic.INSTANCE, alpha, beta);
    // Tensor mask = Tensors.of(alpha, beta, RealScalar.of(.4), RealScalar.of(.4), RealScalar.of(.4));
    // TensorUnaryOperator geodesicCenterFilter = new GeodesicFIRnFilter(Se2Geodesic.INSTANCE, mask);
    // final Tensor refined = Tensor.of(_control.stream().map(geodesicCenterFilter));
    if (jToggleDiff.isSelected()) {
      final int baseline_y = 200;
      {
        graphics.setColor(Color.BLACK);
        graphics.drawLine(0, baseline_y, 300, baseline_y);
      }
      ColorDataIndexed colorDataIndexed = ColorDataLists._097.cyclic();
      {
        int piy = 30;
        graphics.setColor(colorDataIndexed.getColor(0));
        graphics.drawString("Tangent velocity", 0, piy += 15);
        graphics.setColor(colorDataIndexed.getColor(1));
        graphics.drawString("Side slip", 0, piy += 15);
        graphics.setColor(colorDataIndexed.getColor(2));
        graphics.drawString("Rotational rate", 0, piy += 15);
      }
      LieDifferences lieDifferences = //
          new LieDifferences(Se2Group.INSTANCE, Se2CoveringExponential.INSTANCE);
      Tensor speeds = lieDifferences.apply(refined);
      for (int c = 0; c < 3; ++c) {
        graphics.setColor(colorDataIndexed.getColor(c));
        Path2D path2d = plotFunc(graphics, speeds.get(Tensor.ALL, c).multiply(RealScalar.of(400)), baseline_y);
        graphics.setStroke(new BasicStroke(1.3f));
        graphics.draw(path2d);
      }
    }
    graphics.setStroke(new BasicStroke(1f));
    if (jToggleLine.isSelected()) {
      graphics.draw(geometricLayer.toPath2D(refined));
      pathRenderCurve.setCurve(control, false).render(geometricLayer, graphics);
    }
    for (Tensor point : refined) {
      geometricLayer.pushMatrix(Se2Utils.toSE2Matrix(point.copy().append(RealScalar.ZERO)));
      Path2D path2d = geometricLayer.toPath2D(shape);
      if (jToggleStep.isSelected()) {
        path2d = geometricLayer.toPath2D(shape);
      }
      geometricLayer.popMatrix();
      path2d.closePath();
      graphics.setColor(COLOR_SHAPE);
      graphics.fill(path2d);
      graphics.setColor(Color.BLACK);
      graphics.draw(path2d);
    }
    {
      JSlider jSlider = new JSlider(1, 999, 500);
      jSlider.setPreferredSize(new Dimension(500, 28));
      jSlider.addChangeListener(new ChangeListener() {
        @Override
        public void stateChanged(ChangeEvent changeEvent) {
          alpha = RationalScalar.of(jSlider.getValue(), 1000);
          System.out.println(alpha);
        }
      });
      timerFrame.jToolBar.add(jSlider);
    }
  }

  public static void main(String[] args) {
    AbstractDemo abstractDemo = new GeodesicCausalFilterDemo();
    abstractDemo.timerFrame.jFrame.setBounds(100, 100, 1200, 800);
    abstractDemo.timerFrame.jFrame.setVisible(true);
  }
}
