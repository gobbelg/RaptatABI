/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */
package src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;

/** @author vhatvhsahas1 */
public class MedListSectionMarker implements MajorSectionMarker {
  private static final Logger LOGGER = Logger.getLogger(MedListSectionMarker.class);


  public boolean medSection = false;

  public boolean sectionStartsMustHaveColons = true;
  private Pattern[] medListStartPattern;
  private Pattern[] medListFalseStartPattern;
  private Pattern[] medListFalseEndPattern;
  private Pattern twoLineBreakPattern;
  private Pattern reconciliationWordPattern;
  private Pattern startWithAlphaPattern;
  private Pattern startWithNonAlphaEndWithColonPattern;

  private final String medListSectionMarker = RaptatConstants.MED_SECTION_TAG;

  public MedListSectionMarker() {
    MedListSectionMarker.LOGGER.setLevel(Level.INFO);
    initialize();
  }


  /**
   * This should only be called with all sentences from a document, and the sentences must be in
   * order. This uses a finite state machine that determines whether we are currently within part of
   * a medication section and marks the sentences accordingly.
   *
   * @param documentSentences
   */
  @Override
  public void markDocumentSentences(List<AnnotatedPhrase> documentSentences, String documentText) {
    String line;
    String[] lines;
    boolean withinMedSection = false;
    int sentenceEnd = 0;

    for (AnnotatedPhrase sentence : documentSentences) {
      MedListSectionMarker.LOGGER.debug("SENTENCE: " + sentence.getPhraseStringUnprocessed());
      /*
       * If we are within a med section of the document, check whether two or more blank lines
       * separate the last from the current sentence and, if true, set withinMedSection to false
       * before processing this sentence further.
       */
      int sentenceStart = Integer.parseInt(sentence.getRawTokensStartOff());
      if (withinMedSection && this.twoLineBreakPattern
          .matcher(documentText.substring(sentenceEnd, sentenceStart)).find()) {
        withinMedSection = false;
      }
      sentenceEnd = Integer.parseInt(sentence.getRawTokensEndOff());

      lines = sentence.getPhraseStringUnprocessed().split("(?:\r\n)|(?:\r)|(?:\n)|(?:$)");

      for (int i = 0; i < lines.length; i++) {
        line = lines[i];
        line = line.replaceFirst("^( )*", "");

        /*
         * medSection set to false initially, so that initially we are only looking for beginning of
         * medSection, not the end
         */
        if (withinMedSection) {
          withinMedSection = !isNewSection(line);

          if (withinMedSection) {
            sentence.addSentenceAssociatedConcept(this.medListSectionMarker);
          }
        }

        if (!withinMedSection) {
          withinMedSection = isMedSectionStart(line);

          if (withinMedSection) {
            sentence.addSentenceAssociatedConcept(this.medListSectionMarker);
          }
        }
      }
    }
  }


  private void initialize() {
    String[] medStart = {
        /* checks if the sentence contains "sig:" "expire:" or "qty:" */
        "[\\w\\W]*(?i:(sig:|expire:|qty:))[\\w\\W]*",

        /*
         * This regular expression searches for word "medication" ignoring the case and also ending
         * the sentence with colon. It is done not to exclude other sentences with word medication
         */
        "[\\w\\W]*(?i:medication)[\\w\\W]*:(\\s)*",

        /* checks sentences end with medication(s) */
        "[\\w\\W]*(?i:medication)(s)?\\s*(?i:status)*\\s*$",

        /*
         * checks if the sentence contains "meds" "drug" or "prescription" and ends with ":"
         */
        "[\\w\\W]*(?i:(meds|drug|prescription)).*:(\\s)*",

        /*
         * checks if the sentence contains "meds" "drug" or "prescription" and ends with white space
         * or hyphen
         */
        "[\\w\\W]*(?i:(meds|drug|prescription))[(\\s)*-]"};
    this.medListStartPattern = new Pattern[medStart.length];

    for (int i = 0; i < medStart.length; i++) {
      this.medListStartPattern[i] = Pattern.compile(medStart[i]);
    }

    String[] medStartFalse = {
        /* contains word "reconciliation" or "reconciled" */
        "[\\w\\W]*(?i:reconcil)[\\w\\W]+",

        /* checks for sentences that end with period */
        "[\\w\\W]*[.]"};
    this.medListFalseStartPattern = new Pattern[medStartFalse.length];

    for (int i = 0; i < medStartFalse.length; i++) {
      this.medListFalseStartPattern[i] = Pattern.compile(medStartFalse[i]);
    }

    String[] medEndFalse = {
        /* if it is only one word */
        "(\\w)+(\\s)*[:-]?(\\r)?", "[\\w\\W]*(?i:(sig|expire|qty))[\\w\\W]*",

        /* detects format like "Simvastatin 20 mg" */
        "[a-zA-Z\\W]+(\\s)+[0-9]*(\\s+)[\\w\\W]+"};
    this.medListFalseEndPattern = new Pattern[medEndFalse.length];

    for (int i = 0; i < medEndFalse.length; i++) {
      this.medListFalseEndPattern[i] = Pattern.compile(medEndFalse[i]);
    }

    this.reconciliationWordPattern =
        Pattern.compile("[\\w\\W]*(?i:(reconciliation|allergies|reconciled))[\\w\\W]+");
    this.startWithAlphaPattern = Pattern.compile("[a-zA-Z]+[\\w\\W]*");
    this.twoLineBreakPattern = Pattern.compile("((?:\r\n)|(?:\r)|(?:\n))\\s*\\1+");
    this.startWithNonAlphaEndWithColonPattern = Pattern.compile("[a-zA-Z]+[\\w\\W]*:[\\w\\W]*");
  }


  private boolean isMedSectionStart(String line) {
    for (int i = 0; i < this.medListFalseStartPattern.length; i++) {
      Matcher matcher = this.medListFalseStartPattern[i].matcher(line);
      if (matcher.matches()) {
        return false;
      }
    }

    for (int i = 0; i < this.medListStartPattern.length; i++) {
      if (this.medListStartPattern[i].matcher(line).matches()) {
        return true;
      }
    }

    return false;
  }


  private boolean isNewSection(String line) {

    for (int i = 0; i < this.medListFalseEndPattern.length; i++) {
      if (this.medListFalseEndPattern[i].matcher(line).matches()) {
        return false;
      }
    }

    if (this.reconciliationWordPattern.matcher(line).matches()) {
      return true;
    }

    if (!this.sectionStartsMustHaveColons) {
      if (this.startWithAlphaPattern.matcher(line).matches()) {
        return true;
      }
    }

    if (this.startWithNonAlphaEndWithColonPattern.matcher(line).matches()) {
      return true;
    }

    return false;
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {}
    });
  }
}
