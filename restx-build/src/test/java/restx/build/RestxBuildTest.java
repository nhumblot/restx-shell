package restx.build;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.CoreMatchers.is;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 1:57 PM
 */
@RunWith(Parameterized.class)
public class RestxBuildTest {

    private final String moduleFileNameToTest;
    private final RestxBuild.Parser parser;
    private final String expectedModuleFileName;
    private final RestxBuild.Generator generator;
    private Boolean bijectionTest;

    @Parameterized.Parameters(name="{0}")
    public static List<Object[]> data() {
        RestxJsonSupport json = new RestxJsonSupport();
        MavenSupport maven = new MavenSupport();
        IvySupport ivy = new IvySupport();

        return Arrays.asList(
                new Object[]{ "Module1.restx.json", json, "Module1.pom.xml", maven, Boolean.TRUE },
                new Object[]{ "Module3.restx.json", json, "Module3.pom.xml", maven, Boolean.FALSE }, // No bijection because of fragment
                new Object[]{ "Module4.restx.json", json, "Module4.pom.xml", maven, Boolean.TRUE },
                new Object[]{ "Module5.restx.json", json, "Module5.pom.xml", maven, Boolean.FALSE }, // No bijection because of @file
                new Object[]{ "Module6.restx.json", json, "Module6.pom.xml", maven, Boolean.FALSE }, // No bijection because of @file
                new Object[]{ "Module7.restx.json", json, "Module7.pom.xml", maven, Boolean.FALSE }, // No bijection because of fragment
                new Object[]{ "Module8.restx.json", json, "Module8.pom.xml", maven, Boolean.TRUE },
                new Object[]{ "Module9.restx.json", json, "Module9.pom.xml", maven, Boolean.TRUE },
                new Object[]{ "Module1.restx.json", json, "Module1.ivy", ivy, Boolean.FALSE }, // No bijection because ivy is ot a parser
                new Object[]{ "Module2.restx.json", json, "Module2.ivy", ivy, Boolean.FALSE }, // No bijection because ivy is ot a parser
                new Object[]{ "Module4.restx.json", json, "Module4.ivy", ivy, Boolean.FALSE }, // No bijection because ivy is ot a parser
                new Object[]{ "Module5.restx.json", json, "Module5.ivy", ivy, Boolean.FALSE }  // No bijection because ivy is ot a parser
        );
    }

    public RestxBuildTest(String moduleFileNameToTest, RestxBuild.Parser parser, String expectedModuleFileName, RestxBuild.Generator generator, Boolean bijectionTest) {
        this.moduleFileNameToTest = moduleFileNameToTest;
        this.parser = parser;
        this.expectedModuleFileName = expectedModuleFileName;
        this.generator = generator;
        this.bijectionTest = bijectionTest;
    }

    @Test
    public void should_parsing_equals_generation() throws Exception {
        shouldGenerate(moduleFileNameToTest, parser, expectedModuleFileName, generator);
    }

    @Test
    public void should_generation_equals_parsing() throws Exception {
        Assume.assumeThat(bijectionTest, is(Boolean.TRUE));
        shouldGenerate(expectedModuleFileName, (RestxBuild.Parser) generator, moduleFileNameToTest, (RestxBuild.Generator) parser);
    }

    private void shouldGenerate(String module, RestxBuild.Parser parser, String expected, RestxBuild.Generator generator) throws IOException {
        URL resource = getClass().getResource(module);
        ModuleDescriptor md;
        if (resource.getProtocol().equals("file")) {
            File f;
            try {
              f = new File(resource.toURI());
            } catch(URISyntaxException e) {
              f = new File(resource.getPath());
            }
            md = parser.parse(f.toPath());
        } else {
            try (InputStream stream = resource.openStream()) {
                md = parser.parse(stream);
            }
        }
        StringWriter w = new StringWriter();
        generator.generate(md, w);
        assertThat(w.toString()).isEqualTo(RestxBuildHelper.toString(getClass().getResourceAsStream(expected)));
    }

}
