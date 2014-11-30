package restx.build;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:08 PM
 */
public class IvySupport implements RestxBuild.Generator {
    private String eaVersion = "0.9";

    public void generate(ModuleDescriptor md, Writer w) throws IOException {
        if (md.hasClassifier()) {
        	w.write("<ivy-module version=\"2.0\" xmlns:ea=\"http://www.easyant.org\" xmlns:e=\"http://ant.apache.org/ivy/extra\">\n");
        } else {
        	w.write("<ivy-module version=\"2.0\" xmlns:ea=\"http://www.easyant.org\">\n");        	
        }

        w.write("    <info organisation=\"" + md.getGav().getGroupId() +
                "\" module=\"" + md.getGav().getArtifactId() +
                "\" revision=\"" + md.getGav().getVersion().replaceAll("\\-SNAPSHOT$", "") +
                "\" status=\"integration\">\n");

        String buildType = md.getPackaging().equals("war") ? "build-webapp-java" : "build-std-java";
        w.write("        <ea:build organisation=\"org.apache.easyant.buildtypes\"" +
                " module=\""+buildType+"\" revision=\"" + eaVersion + "\"\n");

        for (Map.Entry<String, String> entry : md.getProperties().entrySet()) {
            if (entry.getKey().equals("java.version")) {
                w.write("            compile.java.source.version=\"" + entry.getValue() + "\"\n");
                w.write("            compile.java.target.version=\"" + entry.getValue() + "\"\n");
            } else if (entry.getKey().endsWith(".version")) {
                // versions are inlined, no need to put them here
            } else {
                w.write("            " + entry.getKey() + "=\"" + entry.getValue() + "\"\n");
            }
        }

        w.write("        />\n" +
                "    </info>\n" +
                "    <configurations>\n" +
                "        <conf name=\"default\"/>\n" +
                "        <conf name=\"runtime\"/>\n" +
                "        <conf name=\"test\"/>\n" +
                "    </configurations>\n" +
                "    <publications>\n" +
                "        <artifact type=\"" + md.getPackaging() + "\"/>\n" +
                "    </publications>\n");

        w.write("    <dependencies>\n");
        for (String scope : md.getDependencyScopes()) {
            for (ModuleDependency dependency : md.getDependencies(scope)) {
                String groupId = dependency.getGav().getGroupId();
                String version = RestxBuildHelper.expandProperty(dependency.getGav().getVersion(), "module.version", md.getGav().getVersion());
                String expandedVersion = RestxBuildHelper.expandProperties(md.getProperties(), version);
                if (expandedVersion.endsWith("-SNAPSHOT")) {
                    expandedVersion = "latest.integration";
                }
                if (dependency.getGav().getClassifier() == null) {
	                w.write(String.format("        <dependency org=\"%s\" name=\"%s\" rev=\"%s\" conf=\"%s\" />\n",
	                            groupId, dependency.getGav().getArtifactId(),
	                            expandedVersion,
	                            "compile".equals(scope) ? "default" : scope +"->default"));
                } else {
	                w.write(String.format("        <dependency org=\"%s\" name=\"%s\" rev=\"%s\" conf=\"%s\">\n                <artifact name=\"%s\" e:classifier=\"%s\" />\n        </dependency>\n",
                            groupId, dependency.getGav().getArtifactId(),
                            expandedVersion,
                            "compile".equals(scope) ? "default" : scope +"->default",
                            dependency.getGav().getArtifactId(), dependency.getGav().getClassifier()));                	
                }
            }
        }
        w.write("    </dependencies>\n");

        w.write("</ivy-module>\n");
    }

    @Override
    public String getDefaultFileName() {
        return "module.ivy";
    }
}
