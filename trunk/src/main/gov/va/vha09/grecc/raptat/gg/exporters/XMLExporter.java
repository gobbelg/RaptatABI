package src.main.gov.va.vha09.grecc.raptat.gg.exporters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs.ButtonQueryDialog;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotatedPhrase;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.ConceptRelation;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.RaptatAttribute;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.OffsetConverter;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.StartOffsetPhraseComparator;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.UTFToAnnotatorOffsetConverter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * @author VHATVHJAYARS
 *         <p>
 *         XMLExporter is used for exporting the annotations to an XML file in eHOST format.
 *         <p>
 *         Should be replaced with XMLExporterRevised
 */
@Deprecated
public class XMLExporter {

  private String sourceFile = null;


  private String pathToOriginalTextSource;


  private Document doc;


  private Element root;


  private boolean ignoreOverwrites = false;


  private OffsetConverter offsetConverter = null;

  public XMLExporter() {}

  /**
   * ******************************************************** @Constructor
   *
   * @param srcFileName
   * @param pathToOriginalTextSource
   * @param path ********************************************************
   */
  public XMLExporter(String pathToOriginalTextSource) {
    this.sourceFile = new File(pathToOriginalTextSource).getName();
    this.pathToOriginalTextSource = pathToOriginalTextSource;
    this.offsetConverter =
        new UTFToAnnotatorOffsetConverter(pathToOriginalTextSource, AnnotationApp.EHOST);
    this.doc = new Document();
    this.root = new Element("annotations");
    this.root.setAttribute("textSource", this.sourceFile);
    this.doc.setRootElement(this.root);
  }

  /**
   * ********************************************
   *
   * @param textSourcePath
   * @param ignoreOverwrites
   * @author Glenn Gobbel - Jul 1, 2012 ********************************************
   */
  public XMLExporter(String textSourcePath, boolean ignoreOverwrites) {
    this(textSourcePath);
    this.ignoreOverwrites = ignoreOverwrites;
  }

  public void overwriteXmlFile(String filePath) {
    File outputFile = new File(filePath + File.separator + this.sourceFile + ".knowtator.xml");

    try {
      XMLOutputter out = new XMLOutputter();
      out.setFormat(Format.getPrettyFormat());
      FileWriter writer = new FileWriter(outputFile, false);
      out.output(this.doc, writer);
      writer.close();
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  public void writeAllDataObj(List<AnnotatedPhrase> allAnnData) {
    this.insertPhraseStrings(allAnnData);
    this.offsetConverter.convertAnnotatedPhraseOffsetList(allAnnData);
    this.writeAnnDataObj(allAnnData);
  }

  public void writeAllDataObj(List<AnnotatedPhrase> allAnnData, boolean correctOffsets) {
    this.insertPhraseStrings(allAnnData);
    if (correctOffsets) {
      this.offsetConverter.convertAnnotatedPhraseOffsetList(allAnnData);
    }
    this.writeAnnDataObj(allAnnData);
  }


  /**
   * Write EHost compliant XML knowtator file using the Annotated Phrase Data
   *
   * @author Sanjib Saha - July 31, 2014
   * @param allAnnData annotated phrase data
   * @param sourceFile
   * @param resultFolder
   * @param correctOffsets
   */
  public void writeAllDataObjEHostVersion(List<AnnotatedPhrase> allAnnData, File sourceFile,
      File resultFolder, boolean correctOffsets) {
    if (correctOffsets) {
      this.offsetConverter.convertAnnotatedPhraseOffsetList(allAnnData);
    }
    this.writeAllDataObjEHostVersion(allAnnData, sourceFile, resultFolder.getAbsolutePath());
  }


  /**
   * Write EHost compliant XML knowtator file using the Annotated Phrase Data
   *
   * @author Sanjib Saha - July 31, 2014
   * @param allAnnData annotated phrase data
   * @param parentFolderPath
   */
  public void writeAllDataObjEHostVersion(List<AnnotatedPhrase> allAnnData, File sourceFile,
      String parentFolderPath) {
    this.doc = new Document();
    this.doc.setRootElement(this.writeAnnDataObjEHostVersion(allAnnData));

    if (parentFolderPath == null || parentFolderPath.isEmpty()) {
      parentFolderPath = sourceFile.getParent();
    }
    this.writeXMLFile(parentFolderPath, "", this.sourceFile, this.doc);
  }


  public void writeTags(String tagName, String[] phraseData) {
    if (tagName == "annotations") {
      Element annotation = new Element("annotation");
      Element mentionId = new Element("mentionId");
      mentionId.setAttribute("id", phraseData[0]);
      annotation.addContent(mentionId);
      Element span = new Element("span");
      span.setAttribute("start", phraseData[1]);
      span.setAttribute("end", phraseData[2]);
      annotation.addContent(span);
      Element spannedText = new Element("spannedText");
      spannedText.setText(phraseData[3]);
      annotation.addContent(spannedText);
      this.root.addContent(annotation);

    } else if (tagName == "classMention") {
      Element classMention = new Element("classMention");
      classMention.setAttribute("id", phraseData[0]);
      Element mentionClass = new Element("mentionClass");
      mentionClass.setAttribute("id", phraseData[4]);
      mentionClass.setText(phraseData[4]);
      classMention.addContent(mentionClass);
      this.root.addContent(classMention);
    }
  }


  public String writeXMLFile(String filePath) {
    return this.writeXMLFile(filePath, "");
  }


  public String writeXMLFile(String filePath, String fileMarker) {
    return this.writeXMLFile(filePath, fileMarker, this.sourceFile, this.doc);
  }


  public String writeXMLFile(String filePath, String fileMarker, String theSourceFile,
      Document xmlDocument) {
    int overwriteFile = JOptionPane.YES_OPTION;
    File outputFile =
        new File(filePath + File.separator + theSourceFile + fileMarker + ".knowtator.xml");

    try {
      if (!this.ignoreOverwrites && outputFile.createNewFile() != true) {
        overwriteFile = JOptionPane.showConfirmDialog(null, "An XML file with this name\n"
            + "already exists in this directory.\n" + "Do you want to replace it?");
      }
      if (overwriteFile == JOptionPane.YES_OPTION) {
        XMLOutputter out = new XMLOutputter();
        out.setFormat(Format.getPrettyFormat());
        FileWriter writer = new FileWriter(outputFile, false);
        out.output(xmlDocument, writer);
        writer.close();

        return outputFile.getAbsolutePath();
      }
    } catch (IOException e) {
      System.err.println(e);
    }
    return null;
  }


  private Document createXMLDocument(RaptatDocument inputDocument) {
    Document resultDocument = new Document();
    Element rootElement = new Element("annotations");
    rootElement.setAttribute("textSource", inputDocument.getTextSource().get());
    resultDocument.setRootElement(rootElement);
    return resultDocument;
  }


  /**
   * ***********************************************************
   *
   * @param allAnnData
   * @author Glenn Gobbel - Jun 21, 2012 ***********************************************************
   */
  private void insertPhraseStrings(List<AnnotatedPhrase> allAnnData) {
    this.insertPhraseStrings(allAnnData, this.pathToOriginalTextSource);
  }


  /**
   * ***********************************************************
   *
   * @param allAnnData
   * @author Glenn Gobbel - Jun 21, 2012 ***********************************************************
   */
  private void insertPhraseStrings(List<AnnotatedPhrase> allAnnData, String documentPath) {
    BufferedReader br = null;
    int curStreamPosition = 0;
    int startOffset, endOffset;
    Collections.sort(allAnnData, new StartOffsetPhraseComparator());

    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(documentPath), "UTF-8"));

      for (AnnotatedPhrase curPhrase : allAnnData) {
        startOffset = Integer.parseInt(curPhrase.getRawTokensStartOff());
        endOffset = Integer.parseInt(curPhrase.getRawTokensEndOff());
        char[] phrase = new char[endOffset - startOffset];
        br.skip(startOffset - curStreamPosition);
        br.read(phrase);
        curStreamPosition = endOffset;
        String phraseForInsert = new String(phrase);
        curPhrase.setPhraseStringUnprocessed(phraseForInsert);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          System.out.println(e);
          e.printStackTrace();
        }
      }
    }
  }


  private void writeAnnDataObj(List<AnnotatedPhrase> thePhrases) {
    this.writeAnnDataObj(thePhrases, this.root);
  }


  /**
   * @param thePhrases
   * @param inputRoot
   */
  private void writeAnnDataObj(List<AnnotatedPhrase> thePhrases, Element inputRoot) {
    /** Write annotation elements followed by classMention elements */
    for (AnnotatedPhrase phraseData : thePhrases) {
      Element annotation = new Element("annotation");

      Element mentionId = new Element("mention");
      mentionId.setAttribute("id", "RAPTAT_INSTANCE_" + phraseData.getMentionId());
      annotation.addContent(mentionId);

      Element annotator = new Element("annotator");
      annotator.setAttribute("id", "RAPTAT");
      annotator.setText("RAPTAT");
      annotation.addContent(annotator);

      Element span = new Element("span");
      span.setAttribute("start", phraseData.getRawTokensStartOff());
      span.setAttribute("end", phraseData.getRawTokensEndOff());
      annotation.addContent(span);

      Element spannedText = new Element("spannedText");
      spannedText.setText(phraseData.getPhraseStringUnprocessed());
      annotation.addContent(spannedText);

      Element creationDate = new Element("creationDate");
      creationDate.setText(phraseData.getCreationDate());
      annotation.addContent(creationDate);

      inputRoot.addContent(annotation);

      // Class mention data
      Element classMention = new Element("classMention");
      classMention.setAttribute("id", "RAPTAT_INSTANCE_" + phraseData.getMentionId());

      Element mentionClass = new Element("mentionClass");
      mentionClass.setAttribute("id", phraseData.getConceptName());
      mentionClass.setText(phraseData.getConceptName());
      classMention.addContent(mentionClass);

      /*
       * Annotation attributes - Added by sanjib - April 1, 2015 to add attribute and relationdata
       * for annotations
       */
      for (RaptatAttribute attr : phraseData.getPhraseAttributes()) {
        mentionClass = new Element("hasSlotMention");
        mentionClass.setAttribute("id", "RAPTAT_INSTANCE_" + attr.getID());
        classMention.addContent(mentionClass);

        Element slotMention = new Element("stringSlotMention");
        slotMention.setAttribute("id", "RAPTAT_INSTANCE_" + attr.getID());

        Element slotClass = new Element("mentionSlot");
        slotClass.setAttribute("id", attr.getName());
        slotMention.addContent(slotClass);

        for (String value : attr.getValues()) {
          Element slotValue = new Element("stringSlotMentionValue");
          slotValue.setAttribute("value", value);
          slotMention.addContent(slotValue);
        }

        inputRoot.addContent(slotMention);
      }

      try {
        // Annotation relations
        for (ConceptRelation concept : phraseData.getConceptRelations()) {
          mentionClass = new Element("hasSlotMention");
          mentionClass.setAttribute("id", "RAPTAT_INSTANCE_" + concept.getID());
          classMention.addContent(mentionClass);

          Element conceptMention = new Element("complexSlotMention");
          conceptMention.setAttribute("id", "RAPTAT_INSTANCE_" + concept.getID());

          Element conceptClass = new Element("mentionSlot");
          conceptClass.setAttribute("id", concept.getConceptRelationName());
          conceptMention.addContent(conceptClass);

          String value = concept.getRelatedAnnotationID();

          Element slotValue = new Element("complexSlotMentionValue");
          slotValue.setAttribute("value", "RAPTAT_INSTANCE_" + value);
          conceptMention.addContent(slotValue);

          inputRoot.addContent(conceptMention);
        }
      } catch (Exception ex) {
        // Skip over it
      }

      inputRoot.addContent(classMention);
    }
  }


  /**
   * Prepare the elements for the EHost compatible XML file output using annotated phrase list
   *
   * @param thePhrases annotated phrases
   * @return element containing XML output file data
   */
  private Element writeAnnDataObjEHostVersion(List<AnnotatedPhrase> thePhrases) {
    Element rootElement = new Element("annotations");
    rootElement.setAttribute("textSource", this.sourceFile);
    return this.writeAnnDataObjEHostVersion(thePhrases, rootElement);
  }


  /**
   * Prepare the elements for the EHost compatible XML file output using annotated phrase list Note:
   * Modified by Glenn Gobbel on 10-28-14 to act as a helper method to
   * writeAnnDataObjEHostVersion(List <AnnotatedPhrase> thePhrases), which was originally write by
   * Sanjib Saha
   *
   * @param thePhrases annotated phrases )
   * @return element containing XML output file data
   */
  private Element writeAnnDataObjEHostVersion(List<AnnotatedPhrase> thePhrases,
      Element rootElement) {
    AnnotatedPhrase iterator;

    for (AnnotatedPhrase phraseData : thePhrases) {
      if (phraseData.getPrevPhrase() != null) {
        continue;
      }

      List<String> slotMentionList = new ArrayList<>();

      Element annotation = new Element("annotation");

      Element mentionId = new Element("mention");
      mentionId.setAttribute("id", "RAPTAT_INSTANCE_" + phraseData.getMentionId());
      annotation.addContent(mentionId);

      Element annotator = new Element("annotator");
      annotator.setAttribute("id", "RAPTAT");
      annotator.setText("RAPTAT");
      annotation.addContent(annotator);

      Element span = new Element("span");
      span.setAttribute("start", phraseData.getRawTokensStartOff());
      span.setAttribute("end", phraseData.getRawTokensEndOff());
      annotation.addContent(span);

      String spanText = phraseData.getPhraseStringUnprocessed();

      // Scope for the multiple spanned annotations
      iterator = phraseData;

      while ((iterator = iterator.getNextPhrase()) != null) {
        span = new Element("span");
        span.setAttribute("start", iterator.getRawTokensStartOff());
        span.setAttribute("end", iterator.getRawTokensEndOff());
        annotation.addContent(span);

        spanText += " ... " + iterator.getPhraseStringUnprocessed();
      }

      Element spannedText = new Element("spannedText");
      spannedText.setText(spanText);
      annotation.addContent(spannedText);

      Element creationDate = new Element("creationDate");
      creationDate.setText(phraseData.getCreationDate());
      annotation.addContent(creationDate);

      rootElement.addContent(annotation);

      for (RaptatAttribute phraseAttribute : phraseData.getPhraseAttributes()) {
        Element stringSlotMention = new Element("stringSlotMention");
        stringSlotMention.setAttribute("id", "RAPTAT_INSTANCE_" + phraseAttribute.getID());

        Element mentionSlot = new Element("mentionSlot");
        mentionSlot.setAttribute("id", phraseAttribute.getName());
        stringSlotMention.addContent(mentionSlot);

        for (String value : phraseAttribute.getValues()) {
          Element stringSlotMentionValue = new Element("stringSlotMentionValue");
          stringSlotMentionValue.setAttribute("value", value);
          stringSlotMention.addContent(stringSlotMentionValue);
        }

        slotMentionList.add("RAPTAT_INSTANCE_" + phraseAttribute.getID());
        rootElement.addContent(stringSlotMention);
      }

      for (ConceptRelation relation : phraseData.getConceptRelations()) {
        Element complexSlotMention = new Element("complexSlotMention");
        complexSlotMention.setAttribute("id", "RAPTAT_INSTANCE_" + relation.getID());

        Element mentionSlot = new Element("mentionSlot");
        mentionSlot.setAttribute("id", relation.getConceptRelationName());
        complexSlotMention.addContent(mentionSlot);

        for (RaptatAttribute conceptAttribute : relation.getAttributes()) {
          for (String value : conceptAttribute.getValues()) {
            Element attribute = new Element("attribute");
            attribute.setAttribute("id", conceptAttribute.getName());
            attribute.setText(value);
            complexSlotMention.addContent(attribute);
          }
        }

        String annotationID = relation.getRelatedAnnotationID();
        Element complexSlotMentionValue = new Element("complexSlotMentionValue");
        complexSlotMentionValue.setAttribute("value", "RAPTAT_INSTANCE_" + annotationID);
        complexSlotMention.addContent(complexSlotMentionValue);

        slotMentionList.add("RAPTAT_INSTANCE_" + relation.getID());
        rootElement.addContent(complexSlotMention);
      }

      Element classMention = new Element("classMention");
      classMention.setAttribute("id", "RAPTAT_INSTANCE_" + phraseData.getMentionId());

      for (String slotMention : slotMentionList) {
        Element hasSlotMention = new Element("hasSlotMention");
        hasSlotMention.setAttribute("id", slotMention);
        classMention.addContent(hasSlotMention);
      }

      Element mentionClass = new Element("mentionClass");
      mentionClass.setAttribute("id", phraseData.getConceptName());
      mentionClass.setText(spanText);

      classMention.addContent(mentionClass);
      rootElement.addContent(classMention);
    }

    return rootElement;
  }


  public static void exportAnnotationGroup(AnnotationGroup inputGroup, File exportDirectory) {
    exportAnnotationGroup(inputGroup, exportDirectory, true);
  }


  public static String exportAnnotationGroup(AnnotationGroup inputGroup, File exportDirectory,
      boolean insertPhraseStrings) {
    XMLExporter exporter = new XMLExporter();
    RaptatDocument raptatDocument = inputGroup.getRaptatDocument();
    Document xmlDocument = exporter.createXMLDocument(raptatDocument);
    if (insertPhraseStrings) {
      exporter.insertPhraseStrings(inputGroup.raptatAnnotations,
          raptatDocument.getTextSourcePath().get());
    }
    exporter.writeAnnDataObj(inputGroup.raptatAnnotations, xmlDocument.getRootElement());
    return exporter.writeXMLFile(exportDirectory.getAbsolutePath(), "",
        raptatDocument.getTextSource().get(), xmlDocument);
  }


  /*
   * Use this method to write out results as XML that can be imported and viewed in eHOST
   */ public static void exportAnnotationGroup(AnnotationGroup inputGroup, File resultFolder,
      boolean ignoreOverwrites, boolean correctOffsets, boolean insertPhraseStrings) {
    RaptatDocument raptatDocument = inputGroup.getRaptatDocument();
    String docPath = raptatDocument.getTextSourcePath().get();
    System.out.println("Exporting:" + docPath);
    XMLExporter exporter = new XMLExporter(docPath, ignoreOverwrites);
    if (insertPhraseStrings) {
      exporter.insertPhraseStrings(inputGroup.raptatAnnotations,
          raptatDocument.getTextSourcePath().get());
    }
    exporter.writeAllDataObjEHostVersion(inputGroup.raptatAnnotations, new File(docPath),
        resultFolder, correctOffsets);
  }


  /**
   * @param annotationGroups
   * @param resultFolder
   * @param testSample
   * @param ignoreOverwrites
   * @param correctOffsets
   * @param insertPhraseStrings
   */
  public static void exportAnnotationGroups(List<AnnotationGroup> annotationGroups,
      File resultFolder, boolean ignoreOverwrites, boolean correctOffsets,
      boolean insertPhraseStrings) {
    for (AnnotationGroup group : annotationGroups) {
      RaptatDocument raptatDocument = group.getRaptatDocument();
      String docPath = raptatDocument.getTextSourcePath().get();
      System.out.println("Exporting:" + docPath);
      XMLExporter exporter = new XMLExporter(docPath, ignoreOverwrites);
      if (insertPhraseStrings) {
        exporter.insertPhraseStrings(group.raptatAnnotations,
            raptatDocument.getTextSourcePath().get());
      }
      exporter.writeAllDataObjEHostVersion(group.raptatAnnotations, new File(docPath), resultFolder,
          correctOffsets);
    }
  }


  public static XMLExporter getXMLExporter() {
    String queryString = "Application used to create annotations?";
    String[] buttonNames = new String[AnnotationApp.values().length];
    int i = 0;
    for (AnnotationApp app : AnnotationApp.values()) {
      buttonNames[i++] = app.getCommonName();
    }
    ButtonQueryDialog bqd = new ButtonQueryDialog(buttonNames, queryString);
    String annotationSource = bqd.showQuery();
    AnnotationApp annotationApp = AnnotationApp.EHOST;
    for (AnnotationApp app : AnnotationApp.values()) {
      if (annotationSource.toLowerCase().equals(app.toString().toLowerCase())) {
        annotationApp = app;
        break;
      }
    }
    return new XMLExporter();
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {}
    });
  }
}
