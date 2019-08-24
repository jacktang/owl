// code by gjoel, jph
package ch.ethz.idsc.sophus.util.plot;

import java.io.Serializable;
import java.util.Objects;

/* package */ class ComparableLabel implements Comparable<ComparableLabel>, Serializable {
  private final int index;
  /** may not be null */
  private String string;

  public ComparableLabel(int index) {
    this.index = index;
    string = "";
  }

  @Override
  public int compareTo(ComparableLabel comparableLabel) {
    return Integer.compare(index, comparableLabel.index);// TODO JPH test coverage
  }

  public void setString(String string) {
    this.string = Objects.requireNonNull(string);
  }

  @Override
  public String toString() {
    return string;
  }
}