package restx.build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: xavierhanin
 * Date: 4/14/13
 * Time: 2:21 PM
 */
public class GAV {
    private static final String OPTIONAL_SUFFIX = "!optional";
    private static final Pattern EXCLUSIONS_SUFFIX = Pattern.compile("(.*)!excluding\\(([^)]+)\\)(.*)");

    public static class Exclusion {
        private final String groupId;
        private final String artifactId;

        public Exclusion(String groupId, String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Exclusion exclusion = (Exclusion) o;

            if (groupId != null ? !groupId.equals(exclusion.groupId) : exclusion.groupId != null) return false;
            return artifactId != null ? artifactId.equals(exclusion.artifactId) : exclusion.artifactId == null;
        }

        @Override
        public int hashCode() {
            int result = groupId != null ? groupId.hashCode() : 0;
            result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return groupId + ':' + artifactId;
        }

        public static List<Exclusion> parse(String str) {
            String[] exclusionsStr = str.split(",");
            List<Exclusion> exclusions = new ArrayList();
            for(int i=0; i<exclusionsStr.length; i++) {
                String[] parts = exclusionsStr[i].split(":");
                exclusions.add(new Exclusion(parts[0], parts[1]));
            }
            return exclusions;
        }
    }

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String type;
    private final String classifier;
    private final boolean optional;
    private final List<Exclusion> exclusions;

    public GAV(String groupId, String artifactId, String version, final boolean optional) {
        this(groupId, artifactId, version, null, null, optional);
    }

    public GAV(String groupId, String artifactId, String version, String type, final boolean optional) {
        this(groupId, artifactId, version, type, null, optional);
    }
    
    public GAV(final String groupId, final String artifactId, final String version, final String type, final String classifier, final boolean optional) {
        this(groupId, artifactId, version, type, classifier, optional, null);
    }

    public GAV(final String groupId, final String artifactId, final String version, final String type, final String classifier, final boolean optional, List<Exclusion> exclusions) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.type = type;
        this.classifier = classifier;
        this.optional = optional;
        this.exclusions = exclusions;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getClassifier() {
    	return classifier;
    }

    public boolean isOptional() {
        return optional;
    }

    public List<Exclusion> getExclusions() {
        return exclusions;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GAV gav = (GAV) o;

        if (optional != gav.optional) return false;
        if (!groupId.equals(gav.groupId)) return false;
        if (!artifactId.equals(gav.artifactId)) return false;
        if (!version.equals(gav.version)) return false;
        if (type != null ? !type.equals(gav.type) : gav.type != null) return false;
        if (classifier != null ? !classifier.equals(gav.classifier) : gav.classifier != null) return false;
        return exclusions != null ? exclusions.equals(gav.exclusions) : gav.exclusions == null;
    }

    @Override
    public int hashCode() {
        int result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + (optional ? 1 : 0);
        result = 31 * result + (exclusions != null ? exclusions.hashCode() : 0);
        return result;
    }

    /**
     * toString() will only generate simple GAV whereas toParseableString()
     * should generate a String that should return the same GAV content when parsed
     * through GAV.parse()
     */
    public String toParseableString(){
        StringBuilder builder = new StringBuilder();
        builder.append(groupId + ":" + artifactId + ":" + version);
        if(type == null) {
            // nothing to add
        } else {
            builder.append(":" + type);
            if(classifier != null) {
                builder.append(":" + classifier);
            }
        }

        if(optional) {
            builder.append(OPTIONAL_SUFFIX);
        }

        if(exclusions != null) {
            builder.append("!excluding(");
            for(Exclusion exclusion: exclusions) {
                builder.append(exclusion).append(",");
            }
            builder.setCharAt(builder.length()-1, ')');
        }

        return builder.toString();
    }

    public static GAV parse(String gav) {
        Builder gavBuilder = GAV.builder();
        if(gav.contains(OPTIONAL_SUFFIX)) {
            gavBuilder.opt(true);
            gav = gav.replace(OPTIONAL_SUFFIX, "");
        }
        Matcher exclusionsMatcher = EXCLUSIONS_SUFFIX.matcher(gav);
        if(exclusionsMatcher.matches()) {
            List<Exclusion> exclusions = Exclusion.parse(exclusionsMatcher.group(2));
            gavBuilder.excl(exclusions.toArray(new Exclusion[0]));
            gav = gav.replaceAll(EXCLUSIONS_SUFFIX.pattern(), "$1$3");
        }

        String[] parts = gav.split(":");
        if (parts.length < 3 || parts.length > 5) {
            throw new IllegalArgumentException("can't parse '" + gav + "' as a module coordinates (GAV). " +
                    "It must have at least 3 parts separated by columns. (4th and 5th are optional and correspond to artifact type and classifier)");
        }

        gavBuilder.g(parts[0]).a(parts[1]).v(parts[2]);
        if(parts.length >= 4) {
            gavBuilder.t(parts[3]);
        }
        if(parts.length >= 5) {
            gavBuilder.c(parts[4]);
        }
        return gavBuilder.create();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String groupId;
        private String artifactId;
        private String version;
        private String type;
        private String classifier;
        private boolean optional = false;
        private List<Exclusion> exclusions = null;

        public Builder g(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder a(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder v(String version) {
            this.version = version;
            return this;
        }

        public Builder t(String type) {
            this.type = type;
            return this;
        }

        public Builder c(String classifier) {
            this.classifier = classifier;
            return this;
        }

        public Builder opt(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder excl(Exclusion... exclusions) {
            this.exclusions = Arrays.asList(exclusions);
            return this;
        }

        public GAV create(){
            return new GAV(groupId, artifactId, version, type, classifier, optional, exclusions);
        }
    }
}
