// code by jph
package ch.ethz.idsc.sophus.app.jph;

import java.io.IOException;

import ch.ethz.idsc.sophus.filter.GeodesicCenter;
import ch.ethz.idsc.sophus.filter.GeodesicCenterFilter;
import ch.ethz.idsc.sophus.group.LieDifferences;
import ch.ethz.idsc.sophus.group.Se2CoveringExponential;
import ch.ethz.idsc.sophus.group.Se2Geodesic;
import ch.ethz.idsc.sophus.group.Se2Group;
import ch.ethz.idsc.sophus.math.SmoothingKernel;
import ch.ethz.idsc.sophus.math.WindowCenterSampler;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.io.HomeDirectory;
import ch.ethz.idsc.tensor.io.Put;
import ch.ethz.idsc.tensor.io.ResourceData;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;

/* package */ enum EugocData {
  ;
  private static final LieDifferences LIE_DIFFERENCES = //
      new LieDifferences(Se2Group.INSTANCE, Se2CoveringExponential.INSTANCE);

  public static void main(String[] args) throws IOException {
    System.out.println("here");
    Tensor tensor = ResourceData.of("/dubilab/app/pose/2r/20180820T165637_2.csv");
    // System.out.println(Dimensions.of(tensor));
    Tensor poses = Tensors.empty();
    for (Tensor row : tensor) {
      Tensor xyt = row.extract(1, 4);
      poses.append(xyt);
    }
    System.out.println(Dimensions.of(poses));
    Put.of(HomeDirectory.file("gokart_poses.file"), poses);
    {
      Tensor delta = LIE_DIFFERENCES.apply(poses);
      Put.of(HomeDirectory.file("gokart_delta.file"), delta);
    }
    {
      WindowCenterSampler centerWindowSampler = new WindowCenterSampler(SmoothingKernel.GAUSSIAN);
      TensorUnaryOperator tensorUnaryOperator = //
          GeodesicCenterFilter.of(GeodesicCenter.of(Se2Geodesic.INSTANCE, centerWindowSampler), 6);
      Tensor smooth = tensorUnaryOperator.apply(poses);
      Put.of(HomeDirectory.file("gokart_poses_gauss.file"), smooth);
      Tensor delta = LIE_DIFFERENCES.apply(smooth);
      Put.of(HomeDirectory.file("gokart_delta_gauss.file"), delta);
    }
    {
      WindowCenterSampler centerWindowSampler = new WindowCenterSampler(SmoothingKernel.HAMMING);
      TensorUnaryOperator tensorUnaryOperator = //
          GeodesicCenterFilter.of(GeodesicCenter.of(Se2Geodesic.INSTANCE, centerWindowSampler), 6);
      Tensor smooth = tensorUnaryOperator.apply(poses);
      Put.of(HomeDirectory.file("gokart_poses_hammi.file"), smooth);
      Tensor delta = LIE_DIFFERENCES.apply(smooth);
      Put.of(HomeDirectory.file("gokart_delta_hammi.file"), delta);
    }
  }
}
