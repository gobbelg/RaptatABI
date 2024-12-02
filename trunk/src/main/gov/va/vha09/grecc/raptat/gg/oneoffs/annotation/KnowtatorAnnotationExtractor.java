/*
 * The contents of this file are subject to the Mozilla Public License Version 1.1 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF
 * ANY KIND, either express or implied. See the License for the specific language governing rights
 * and limitations under the License.
 *
 * The Original Code is Knowtator.
 *
 * The Initial Developer of the Original Code is University of Colorado. Copyright (C) 2005-2008.
 * All Rights Reserved.
 *
 * Knowtator was developed by the Center for Computational Pharmacology (http://compbio.uchcs.edu)
 * at the University of Colorado Health Sciences Center School of Medicine with support from the
 * National Library of Medicine.
 *
 * Current information about Knowtator can be obtained at http://knowtator.sourceforge.net/
 *
 * Contributor(s): Philip V. Ogren <philip@ogren.info> (Original Author)
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.annotation;

import java.io.File;
import java.util.Collection;
import java.util.List;
import javax.swing.JFileChooser;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.ProjectSettings;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollection;
import edu.uchsc.ccp.knowtator.textsource.TextSourceIterator;
import edu.uchsc.ccp.knowtator.util.ProjectUtil;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.MultiFileFilter;

/**
 * This is an example script that shows how to programmatically extract annotation data from a
 * Knowtator project. It is not intended to be very useful by itself other than to provide the
 * necessary code to write your own scripts for extracting annotation data from Knowtator.
 *
 * <p>
 * Make sure you have knowtator.jar, protege.jar, and looks-<version>.jar (from Protege
 * installation) on your classpath.
 *
 * <p>
 * NOTE: You will probably have to run this script from the directory that your knowtator project
 * resides in. This is because Knowtator uses relative paths to determine the location of the text
 * source collection when possible. This can be accomlished in Eclipse by setting the "Working
 * Directory" which is located in the "Arguments" tab of the java run configuration manager.
 *
 * @author Philip Ogren
 */
public class KnowtatorAnnotationExtractor {

  public static void main(String[] args) throws Exception {
    System.out.println("Usage: java ExtractAnnotationsFromKnowtator <project-name>");

    JFileChooser fc = new JFileChooser();
    fc.addChoosableFileFilter(new MultiFileFilter(RaptatConstants.IMPORTABLE_FILETYPES));
    fc.setDialogTitle("Choose File");
    fc.setFileHidingEnabled(true);
    int fileChosen = fc.showOpenDialog(null);
    if (fileChosen == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fc.getSelectedFile();
      String projectFileName = selectedFile.getAbsolutePath();
      Project project = ProjectUtil.openProject(projectFileName);

      KnowledgeBase kb = project.getKnowledgeBase();
      KnowtatorProjectUtil kpu = new KnowtatorProjectUtil(kb);
      KnowtatorManager knowtatorManager = new KnowtatorManager(kpu);
      AnnotationUtil annotationUtil = knowtatorManager.getAnnotationUtil();
      MentionUtil mentionUtil = knowtatorManager.getMentionUtil();

      TextSourceCollection textSourceCollection =
          ProjectSettings.getRecentTextSourceCollection(project);
      TextSourceIterator textSourceIterator = textSourceCollection.iterator();

      while (textSourceIterator.hasNext()) {
        TextSource textSource = textSourceIterator.next();

        System.out.println("\n\n" + textSource.getName());
        Collection<SimpleInstance> annotations = annotationUtil.getAnnotations(textSource);
        if (annotations != null) {
          for (SimpleInstance annotation : annotations) {
            System.out.println("annotation: " + annotation.getBrowserText());
            List<Span> spans = annotationUtil.getSpans(annotation);
            for (Span span : spans) {
              System.out.println("\tspan: " + span.toString());
            }
            SimpleInstance annotator = annotationUtil.getAnnotator(annotation);
            if (annotator != null) {
              System.out.println("\tannotator: " + annotator.getBrowserText());
            }
            String comment = annotationUtil.getComment(annotation);
            if (comment != null && comment.length() > 0) {
              System.out.println("\tcomment: " + comment);
            }

            SimpleInstance mention = annotationUtil.getMention(annotation);
            if (mention == null) {
              continue;
            }
            Cls mentionCls = mentionUtil.getMentionCls(mention);
            System.out.println("\tmention: " + mentionCls.getName());

            List<SimpleInstance> slotMentions = mentionUtil.getSlotMentions(mention);
            for (SimpleInstance slotMention : slotMentions) {
              Slot slot = mentionUtil.getSlotMentionSlot(slotMention);
              String slotName = slot.getName();
              System.out.println("\t\tslot=" + slotName);
              List<Object> slotValues = mentionUtil.getSlotMentionValues(slotMention);
              for (Object slotValue : slotValues) {
                if (mentionUtil.isComplexSlotMention(slotMention)) {
                  SimpleInstance slotValueMention = (SimpleInstance) slotValue;
                  if (mentionUtil.isClassMention(slotValueMention)) {
                    Cls cls = mentionUtil.getMentionCls(slotValueMention);
                    System.out.println("\t\t\tcls=" + cls.getName());
                  }
                  SimpleInstance slotValueAnnotation =
                      mentionUtil.getMentionAnnotation(slotValueMention);
                  System.out.println("\t\t\tannotation=" + slotValueAnnotation.getBrowserText());
                } else {
                  System.out.println("\t\t\tvalue=" + slotValue);
                }
              }
            }
          }
        }
      }
    }
  }
}
