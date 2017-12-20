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
    public static List<Object[]> data() {
        return Arrays.asList(
                // Parsing regular artefacts
                new Object[]{ "fr.4sh.pom-parents:4sh-uberpom:0.8", GAV.builder().g("fr.4sh.pom-parents").a("4sh-uberpom").v("0.8").create() },
                new Object[]{ "restx:restx-common:0.2-SNAPSHOT", GAV.builder().g("restx").a("restx-common").v("0.2-SNAPSHOT").create() },
                new Object[]{ "joda-time:joda-time:${joda-time.version}", GAV.builder().g("joda-time").a("joda-time").v("${joda-time.version}").create() },

                // classifiers
                new Object[]{ "restx:restx-ui:0.2:zip:jdk8", GAV.builder().g("restx").a("restx-ui").v("0.2").t("zip").c("jdk8").create() },
                new Object[]{ "restx:restx-ui:0.2:jar:jdk8", GAV.builder().g("restx").a("restx-ui").v("0.2").t("jar").c("jdk8").create() },

                // optionals
                new Object[]{ "restx:restx-ui:0.2!optional", GAV.builder().g("restx").a("restx-ui").v("0.2").opt(true).create() },
                new Object[]{ "restx:restx-ui:0.2:zip!optional", GAV.builder().g("restx").a("restx-ui").v("0.2").t("zip").opt(true).create() },
                new Object[]{ "restx:restx-ui:0.2:jar:jdk8!optional", GAV.builder().g("restx").a("restx-ui").v("0.2").t("jar").c("jdk8").opt(true).create() },

                // type
                new Object[]{ "restx:restx-ui:0.2:zip", GAV.builder().g("restx").a("restx-ui").v("0.2").t("zip").create() }
        );
    }

    private String gavStr;
    private GAV expectedGAV;

    public GAVConsistenciesTest(String gavStr, GAV expectedGAV) {
        this.gavStr = gavStr;
        this.expectedGAV = expectedGAV;
    }

    @Test
    public void parsed_gav_should_be_valid() {
        assertThat(GAV.parse(this.gavStr)).isEqualTo(this.expectedGAV);
    }

    @Test
    public void gav_should_be_consistent_when_reparsed() {
        assertThat(GAV.parse(this.gavStr).toParseableString()).isEqualTo(this.gavStr);
    }
}
