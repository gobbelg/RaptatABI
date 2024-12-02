/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.analysis.scorer;

import java.io.File;
import org.apache.commons.lang3.builder.ToStringBuilder;
import src.main.gov.va.vha09.grecc.raptat.gg.core.Constants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.RaptatStringStyle;
import src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.io.FileUtilities;

/**
 * Stores the various parameters supplied in the command line when scoring performance via a
 * PerformanceScorer instance
 *
 * @author Glenn Gobbel
 *
 */
public class ScoringParameterObject {

    // Static nested Builder class
    public static class Builder {
        // Builder fields
        private String textDirectoryPath;
        private String raptatDirectoryPath;
        private String referenceDirectoryPath;
        private String schemaFilePath;
        private String conceptsFilePath;
        private AnnotationApp annotationApp;
        private boolean removeUnmatched;

        public Builder annotationApp(AnnotationApp annotationApp) {
            this.annotationApp = annotationApp;
            return this;
        }

        // Build method to create an instance of MyClass
        public ScoringParameterObject build() {
            return new ScoringParameterObject(this);
        }

        public Builder conceptsFilePath(String conceptsFilePath) {
            this.conceptsFilePath = conceptsFilePath;
            return this;
        }

        public Builder raptatDirectoryPath(String raptatDirectoryPath) {
            this.raptatDirectoryPath = raptatDirectoryPath;
            return this;
        }

        public Builder referenceDirectoryPath(String referenceDirectoryPath) {
            this.referenceDirectoryPath = referenceDirectoryPath;
            return this;
        }

        public Builder removeUnmatched(boolean removeUnmatched) {
            this.removeUnmatched = removeUnmatched;
            return this;
        }

        public Builder schemaFilePath(String schemaFilePath) {
            this.schemaFilePath = schemaFilePath;
            return this;
        }

        // Setter methods for each field, returning the Builder itself for chaining
        public Builder textDirectoryPath(String textDirectoryPath) {
            this.textDirectoryPath = textDirectoryPath;
            return this;
        }
    }

    public final String textDirectoryPath;
    public final String raptatDirectoryPath;
    public final String referenceDirectoryPath;
    public final String schemaFilePath;
    public final String conceptsFilePath;
    public final AnnotationApp annotationApp;


    public final boolean removeUnmatched;


    public ScoringParameterObject(String textDirectoryPath, String raptatDirectoryPath,
            String referenceDirectoryPath, String schemaFilePath, String conceptsFilePath,
            AnnotationApp annotationApp, String removeUnmatched) {
        super();
        this.textDirectoryPath = textDirectoryPath;
        this.raptatDirectoryPath = raptatDirectoryPath;
        this.referenceDirectoryPath = referenceDirectoryPath;
        this.schemaFilePath = schemaFilePath;
        this.conceptsFilePath = conceptsFilePath;
        this.annotationApp = annotationApp;
        this.removeUnmatched = Boolean.parseBoolean(removeUnmatched);
    }

    // Private constructor to enforce the use of the Builder
    private ScoringParameterObject(Builder builder) {
        this.textDirectoryPath = builder.textDirectoryPath;
        this.raptatDirectoryPath = builder.raptatDirectoryPath;
        this.referenceDirectoryPath = builder.referenceDirectoryPath;
        this.schemaFilePath = builder.schemaFilePath;
        this.conceptsFilePath = builder.conceptsFilePath;
        this.annotationApp = builder.annotationApp;
        this.removeUnmatched = builder.removeUnmatched;
    }

    @Override
    public String toString() {
        ToStringBuilder.setDefaultStyle(RaptatStringStyle.getInstance());

        ToStringBuilder sb =
                new ToStringBuilder(this).append("textDirectoryPath", this.textDirectoryPath)
                        .append("raptatDirectoryPath", this.raptatDirectoryPath)
                        .append("referenceDirectoryPath", this.referenceDirectoryPath)
                        .append("schemaFilePath", this.schemaFilePath)
                        .append("conceptsFilePath", this.conceptsFilePath)
                        .append("annotationApp", this.annotationApp)
                        .append("checkPhraseOffsets", this.removeUnmatched);

        return sb.toString();
    }

    public static String getAnnotatorDirectoryPath(int i) {
        String title = "Select directory containing annotator " + i + " annotations";
        File annotationFileDirectory = FileUtilities.getDirectory(title);
        return annotationFileDirectory == null ? null : annotationFileDirectory.getAbsolutePath();
    }

    public static String getSchemaFilePath() {
        String title = "Select schema file";
        File schemaFile = FileUtilities.getFile(title);
        return schemaFile == null ? null : schemaFile.getAbsolutePath();
    }


    public static String getTextDirectoryPath() {
        String title = "Select directory containing text files";
        File textDirectory = FileUtilities.getDirectory(title);
        return textDirectory == null ? null : textDirectory.getAbsolutePath();
    }
}
