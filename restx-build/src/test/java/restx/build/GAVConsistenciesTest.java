package restx.build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:26 PM
 */
@RunWith(Parameterized.class)
public class GAVConsistenciesTest {

    @Parameterized.Parameters(name="{0}")
    public static List<String[]> data() {
        return Arrays.asList(
                // Parsing regular artefacts
                new String[]{ "fr.4sh.pom-parents:4sh-uberpom:0.8" },
                new String[]{ "restx:restx-common:0.2-SNAPSHOT" },
                new String[]{ "joda-time:joda-time:${joda-time.version}" },

                // classifiers
                new String[]{ "restx:restx-ui:0.2:zip:jdk8" },
                new String[]{ "restx:restx-ui:0.2:jar:jdk8" },

                // optionals
                new String[]{ "restx:restx-ui:0.2!optional" },
                new String[]{ "restx:restx-ui:0.2:zip!optional" },
                new String[]{ "restx:restx-ui:0.2:jar:jdk8!optional" },

                // type
                new String[]{ "restx:restx-ui:0.2:zip" }
        );
    }

    private String gavStr;

    public GAVConsistenciesTest(String gavStr) {
        this.gavStr = gavStr;
    }

    @Test
    public void gav_should_be_consistent_when_reparsed() {
        assertThat(GAV.parse(this.gavStr).toParseableString()).isEqualTo(this.gavStr);
    }
}
