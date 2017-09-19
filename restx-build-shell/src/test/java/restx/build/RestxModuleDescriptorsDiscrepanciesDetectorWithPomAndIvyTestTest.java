package restx.build;

import com.googlecode.junittoolbox.ParallelParameterized;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import restx.build.testing.AbstractRestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTest;

import java.util.Collections;


@RunWith(ParallelParameterized.class)
public class RestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTestTest extends AbstractRestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTest {

    private static final String RESTX_SHELL_SOURCES_ROOT_DIR_SYSPROP = "restxShellSourcesRootDir";

    @Parameterized.Parameters(name="{0}/{1}")
    public static Iterable<Object[]> data(){
        return AbstractRestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTest.data(RESTX_SHELL_SOURCES_ROOT_DIR_SYSPROP, Collections.<String>emptyList());
    }

    public RestxModuleDescriptorsDiscrepanciesDetectorWithPomAndIvyTestTest(String moduleName, GenerationType generationType) {
        super(moduleName, generationType);
    }

    @Override
    protected String getRestxSourcesDirSysProp() {
        return RESTX_SHELL_SOURCES_ROOT_DIR_SYSPROP;
    }
}
