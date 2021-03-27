package dev.mck.mvnmon.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Formats columnar data for console output.
 *
 * <p>Example:
 *
 * <pre>
 * COLUMN A        LONGCOLUMNNAME COLUMN C
 * foo             bar            baz
 * longColumnValue bar            baz
 * </pre>
 *
 * Note that the rightmost column will not include trailing whitespace, and the final row will not
 * include a trailing line-separator.
 */
public class TableFormatter {
  private final Map<String, Pair<MaximumInt, List<String>>> columns;

  public TableFormatter() {
    this.columns = new LinkedHashMap<>(); // insertion order is crucial
  }

  /**
   * Add a row to the formatted table. The initial order in which columns are added will determine
   * their order in the formatted output.
   *
   * @param columnName
   * @param value
   * @return this, for method chaining.
   */
  public TableFormatter add(String columnName, String value) {
    var column =
        columns.computeIfAbsent(
            columnName,
            ignored -> Pair.of(new MaximumInt().consider(columnName.length()), new ArrayList<>()));
    column.getLeft().consider(value.length());
    column.getRight().add(value);
    return this;
  }

  /**
   * @return formatted table; time complexity is O(cr) where c is the number of columns and r is the
   *     number of rows.
   */
  @Override
  public String toString() {
    int rows = -1;
    PaddedStringBuilder s = new PaddedStringBuilder();
    int i = 0;
    for (var column : columns.entrySet()) {
      if (rows == -1) {
        rows = column.getValue().getRight().size();
      } else if (rows != column.getValue().getRight().size()) {
        throw new IllegalStateException("row count is not consistent! columns=" + columns);
      }
      if (i == columns.size() - 1) {
        s.append(column.getKey());
      } else {
        s.padWith(column.getKey(), ' ', column.getValue().getLeft().get() + 1);
      }
      i++;
    }
    List<Pair<MaximumInt, List<String>>> values = new ArrayList<>(columns.values());
    for (i = 0; i < rows; i++) {
      s.append(System.lineSeparator());
      for (int j = 0; j < values.size(); j++) {
        if (j == values.size() - 1) {
          s.append(values.get(j).getRight().get(i));
        } else {
          s.padWith(values.get(j).getRight().get(i), ' ', values.get(j).getLeft().get() + 1);
        }
      }
    }
    return s.toString();
  }
}
