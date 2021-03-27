package dev.mck.mvnmon.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class TableFormatterTest {
    @Test
    public void test() {
        var tf = new TableFormatter()
          .add("foo", "1")
          .add("foo", "long12345")
                .add("longcolumnname", "1")
                .add("longcolumnname", "2")
          .add("bar", "1")
          .add("bar", "long123");
        
        String s = tf.toString(); 

        String expected = """
          foo       longcolumnname bar
          1         1              1
          long12345 2              long123""";

        assertThat(s).isEqualTo(expected);
    }
}
