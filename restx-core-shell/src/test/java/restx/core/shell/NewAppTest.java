package restx.core.shell;

import com.googlecode.junittoolbox.ParallelParameterized;
import jline.console.ConsoleReader;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import restx.CoreModule;
import restx.build.RestxJsonSupport;
import restx.common.OSUtils;
import restx.factory.Factory;
import restx.shell.RestxShell;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author fcamblor
 */
@RunWith(ParallelParameterized.class)
public class NewAppTest {

    @Rule
    public TemporaryFolder workDirectory = new TemporaryFolder();

    private final AppShellCommand.NewAppDescriptor descriptor;
    private String initialRestxShellHomeValue;

    @Before
    public void setup() throws IOException {
        // Avoiding "restx.shell.home_IS_UNDEFINED/ dir due to logs
        this.initialRestxShellHomeValue = System.getProperty("restx.shell.home");
        if(this.initialRestxShellHomeValue == null) {
            System.setProperty("restx.shell.home", workDirectory.newFolder(".restx").getAbsolutePath());
        }
    }

    @After
    public void teardown() {
        if(this.initialRestxShellHomeValue == null) {
            System.getProperties().remove("restx.shell.home");
        }
    }

    @Parameterized.Parameters(name="{0}")
    public static Iterable<Object[]> data() throws IOException {
        return Arrays.asList(
            new Object[]{ "Simplest app", createDescriptor("test1", false, Arrays.asList(ModuleDescriptorType.values())) },
            new Object[]{ "App with hello resource + all descriptors", createDescriptor("test2", true, Arrays.asList(ModuleDescriptorType.values())) },
            new Object[]{ "App with hello resource + maven descriptor only", createDescriptor("test3", true, Arrays.asList(ModuleDescriptorType.MAVEN)) }
            // Disabled because executing maven goals won't work with ivy-only file
            // new Object[]{ "App with hello resource + ivy descriptor only", createDescriptor("test4", true, Arrays.asList(ModuleDescriptorType.IVY)) }
        );
    }

    public NewAppTest(String testName, AppShellCommand.NewAppDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Test
    public void should_app_generated_executes_maven_well() throws IOException, VerificationException {
        if ("true".equals(System.getenv("DRONE"))) {
            // executing maven causes problem on drone
            return;
        }

        RestxShell shell = prepareRestxShell();

        AppShellCommand.NewAppCommandRunner appCommandRunner =
                new AppShellCommand(new CoreModule().uuidGenerator()).new NewAppCommandRunner();
        Path appPath = appCommandRunner.generateApp(descriptor, shell);

        Verifier mavenVerifier = new Verifier(appPath.toString());

        if (OSUtils.isMacOSX()) {
            // on MacOSX, when using the verifier JAVA_HOME is not always properly detected
            // sometimes it points to JRE, making javadoc call fail.
            // Here is a workaround for that problem
            String javaHome = System.getProperty("java.home");
            if (javaHome.endsWith("jre")) {
                javaHome = new File(javaHome).getParentFile().getCanonicalPath();
                mavenVerifier.setEnvironmentVariable("JAVA_HOME", javaHome);
            }
        }
        if(System.getProperty("skip-offline") == null) {
            mavenVerifier.addCliOption("--offline");
        }
        mavenVerifier.executeGoal("package");
        mavenVerifier.verifyErrorFreeLog();
    }

    private RestxShell prepareRestxShell() throws IOException {
        ConsoleReader consoleReader = new ConsoleReader();
        RestxShell shell = spy(new RestxShell(consoleReader, Factory.builder().build()));
        doNothing().when(shell).println(anyString());
        doReturn(this.workDirectory.getRoot().toPath()).when(shell).currentLocation();
        return shell;
    }

    private static AppShellCommand.NewAppDescriptor createDescriptor(String name, boolean generateHelloResource, List<ModuleDescriptorType> buildFiles) throws IOException {
        String restxVersion = System.getProperty("restxVersion");
        if(restxVersion == null) {
            throw new IllegalArgumentException("You need to put -DrestxVersion=xxx while executing this test !");
        }

        AppShellCommand.NewAppDescriptor desc = new AppShellCommand.NewAppDescriptor();
        desc.appName = name+"App";
        desc.groupId = "com.foo";
        desc.artifactId = name;
        desc.targetPath = name;
        desc.mainPackage = "com.foo";
        desc.version = "0.1-SNAPSHOT";
        desc.buildFiles = buildFiles;
        desc.signatureKey = "blah blah blah";
        desc.adminPassword = "pwd";
        desc.defaultPort = "8080";
        desc.baseAPIPath = "/api";
        desc.javaVersion = "1.7";
        desc.restxVersion = restxVersion;
        desc.includeStatsModule = false;
        desc.generateHelloResource = generateHelloResource;
        return desc;
    }
}
