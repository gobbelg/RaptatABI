/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.training;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.phrase.PhraseFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.featurebuilding.labelingfilters.sentence.SentenceFilter;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.AnnotationApp;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.PhraseIDSmoothingMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing.AnnotationGroupPostProcessor;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeConflictManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.annotationcomponents.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.schema.SchemaImporter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.AttributeSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.SentenceContextAttributeAnalyzer;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.tokencontext.TokenContextAnalyzer;

/**
 * Class used to store parameters used in cross-validation as strings and to convert them to the
 * appropriate types before running cross-validation. The class is made serializable so it can be
 * stored as part of the RapTAT solution file after training. It can then be used to determine
 * training parameters at the time of training and to create fields needed for annotation. It is
 * stored as 3 nested classes only to make it easier to distinguish which parameters are used for
 * training and which are used for annotation
 *
 * @author Glenn Gobbel
 */
public final class TSTrainingParameterObject implements Serializable {
  /*
   * Nested class to store objects that are only used for annotation and are provided to the
   * TSTrainingParameterObject when training so that the solution file created will have all the
   * elements needed later for annotation
   */
  public class AnnotationOnlyParameterObject implements Serializable {
    private static final long serialVersionUID = 2667895248443944631L;

    /*
     * Note that the attributeSifter class was introduced to sift out annotation without overt
     * reason no meds in that particular project. It is included here to deal with future
     * modifications, and, as of 2/20/18, is not part of the TSTrainingParameterObject or
     * AnnotationParameterObject constructors, and it is not included in the TSTrainingParser
     */
    public final AttributeSifter attributeSifter = null;

    public final Collection<AttributeTaggerSolution> attributeTaggers;
    public final List<AttributeConflictManager> attributeConflictManagers;
    public final List<AnnotationGroupPostProcessor> annotationGroupPostProcessors;
    public final Set<SentenceContextAttributeAnalyzer> sentenceContextAnalyzers;
    public final List<RaptatPair<String, String>> sentenceMatchers;
    public final TokenContextAnalyzer dictionary;
    public final List<RaptatPair<String, String>> sectionMatchers;
    public final ContextSifter contextSifter;


    private AnnotationOnlyParameterObject(String dictionaryFilePath,
        HashSet<String> acceptedConcepts, String[] attributeTaggerPaths,
        String[] attributeConflictManagersDescriptors, String[] annotationGroupPostProcessors,
        String[] sentenceContextAnalyzerDescriptors, boolean isSentenceContextAnalyzerFilePath,
        String[] sectionMatcherDescriptors, boolean isSectionMatcherFilePath,
        String[] sentenceMatcherDescriptors, boolean isSentenceMatcherFilePath,
        String[] contextSifterDescriptors, boolean isContextSifterFilePath) {

      this.dictionary =
          new TokenContextAnalyzer(dictionaryFilePath.replaceAll("\"", ""), acceptedConcepts);
      this.attributeTaggers = getAttributeTaggers(attributeTaggerPaths);
      this.attributeConflictManagers =
          getAttributeConflictManagers(attributeConflictManagersDescriptors);
      this.annotationGroupPostProcessors =
          getAnnotationGroupPostProcessors(annotationGroupPostProcessors);

      this.sentenceContextAnalyzers = this.getSentenceContextAttributeAnalyzers(
          sentenceContextAnalyzerDescriptors, isSentenceContextAnalyzerFilePath);

      this.sectionMatchers =
          getSectionMatchers(sectionMatcherDescriptors, isSectionMatcherFilePath);
      this.sentenceMatchers =
          getSentenceMatchers(sentenceMatcherDescriptors, isSentenceMatcherFilePath);

      this.contextSifter = this.getContextSifter(contextSifterDescriptors, isContextSifterFilePath);
    }


    private List<AnnotationGroupPostProcessor> getAnnotationGroupPostProcessors(
        String[] annotationGroupPostProcessors) {
      List<AnnotationGroupPostProcessor> resultList = new ArrayList<>();
      if (annotationGroupPostProcessors == null || annotationGroupPostProcessors.length < 1) {
        return resultList;
      }

      for (String postProcessorDescriptor : annotationGroupPostProcessors) {
        try {
          AnnotationGroupPostProcessor postProcessor;

          postProcessorDescriptor = postProcessorDescriptor.replaceAll("\"", "");
          Class<?> postProcessorClass = Class.forName(postProcessorDescriptor);
          Constructor<?> classConstructor = postProcessorClass.getConstructor();
          postProcessor = (AnnotationGroupPostProcessor) classConstructor.newInstance();
          resultList.add(postProcessor);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
            | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
          System.err.println(e.getLocalizedMessage());
          System.err.println("Unable to instantiate " + postProcessorDescriptor
              + " as instance of AnnotationGroupPostProcessor");
          e.printStackTrace();
          System.exit(-1);
        }
      }

      return resultList;
    }


    /**
     * Oct 17, 2017
     *
     * @return
     */
    private Set<String> getAttributeConflictManagerNames(
        String[] attributeConflictManagersDescriptors) {
      if (attributeConflictManagersDescriptors == null) {
        return new HashSet<>();
      }
      return new HashSet<>(Arrays.asList(attributeConflictManagersDescriptors));
    }


    /**
     * Oct 17, 2017
     *
     * @return
     */
    private List<AttributeConflictManager> getAttributeConflictManagers(
        String[] attributeConflictManagersDescriptors) {
      Set<String> conflictManagerNames =
          getAttributeConflictManagerNames(attributeConflictManagersDescriptors);
      List<AttributeConflictManager> conflictManagers =
          new ArrayList<>(conflictManagerNames.size());

      for (String managerName : conflictManagerNames) {
        try {
          managerName = managerName.replaceAll("\"", "");
          Class<?> managerClass = Class.forName(managerName);
          Constructor<?> classConstructor = managerClass.getConstructor();
          AttributeConflictManager conflictManager =
              (AttributeConflictManager) classConstructor.newInstance();
          conflictManagers.add(conflictManager);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
            | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
          System.err.println(
              "Unable to construct attribute confllict managers " + e.getLocalizedMessage());
          e.printStackTrace();
          Arrays.toString(conflictManagerNames.toArray(new String[] {}));
          System.exit(-1);
        }
      }

      return conflictManagers;
    }


    /**
     * Oct 17, 2017
     *
     * @return
     */
    private Collection<AttributeTaggerSolution> getAttributeTaggers(String[] attributeTaggerPaths) {
      if (attributeTaggerPaths == null) {
        return new ArrayList<>();
      }

      List<AttributeTaggerSolution> attributeTaggers = new ArrayList<>(attributeTaggerPaths.length);

      for (String attributeTaggerPath : attributeTaggerPaths) {
        AttributeTaggerSolution solution =
            AttributeTaggerSolution.loadFromFile(new File(attributeTaggerPath));
        attributeTaggers.add(solution);
      }

      return attributeTaggers;
    }


    private String getContextDescription(Map<String, List<String>> sifterConceptMap,
        String contextKey) {
      List<String> resultList;
      if ((resultList = sifterConceptMap.get(contextKey)) == null) {
        resultList = new ArrayList<>();
      }
      return resultList.get(0);
    }


    /**
     * The contextSifterDescriptors parameter must not be null when this is call
     *
     * @param contextSifterDescriptors
     * @return
     */
    private ContextSifter getContextSifter(String[] contextSifterDescriptors) {
      ContextSifter contextSifter = new ContextSifter();
      Map<String, Integer> sifterFields =
          this.getNormalizedEnumConstants(RaptatConstants.CONTEXT_SIFTER_FIELDS.class);
      Set<String> sifterFieldNames = sifterFields.keySet();
      for (String sifterDescriptor : contextSifterDescriptors) {
        sifterDescriptor = sifterDescriptor.replaceAll("\"", "");
        Map<String, List<String>> sifterConceptMap =
            getContextSifterFields(sifterDescriptor, sifterFieldNames);
        Set<String> conceptsSet = new HashSet<>(sifterConceptMap
            .get(RaptatConstants.CONTEXT_SIFTER_FIELDS.CONCEPTS.toString().toLowerCase()));
        if (!conceptsSet.isEmpty()) {
          String sifterName = sifterConceptMap
              .get(RaptatConstants.CONTEXT_SIFTER_FIELDS.ORDER.toString().toLowerCase()).get(0);
          String contextDescriptor = getContextDescription(sifterConceptMap,
              RaptatConstants.CONTEXT_SIFTER_FIELDS.CONTEXT.toString().toLowerCase());
          RaptatPair<Integer, Integer> forbiddenDistances = getDistances(sifterConceptMap,
              RaptatConstants.CONTEXT_SIFTER_FIELDS.FORBIDDEN_DISTANCE_REVERSE.toString()
                  .toLowerCase(),
              RaptatConstants.CONTEXT_SIFTER_FIELDS.FORBIDDEN_DISTANCE_FORWARD.toString()
                  .toLowerCase());
          RaptatPair<Integer, Integer> requiredDistances = getDistances(sifterConceptMap,
              RaptatConstants.CONTEXT_SIFTER_FIELDS.REQUIRED_DISTANCE_REVERSE.toString()
                  .toLowerCase(),
              RaptatConstants.CONTEXT_SIFTER_FIELDS.REQUIRED_DISTANCE_FORWARD.toString()
                  .toLowerCase());

          for (String concept : conceptsSet) {
            contextSifter.addContextChecker(sifterName, concept, contextDescriptor,
                forbiddenDistances, requiredDistances);
          }
        }
      }

      return contextSifter;
    }


    private ContextSifter getContextSifter(String[] contextSifterDescriptors,
        boolean isContextSifterFilePath) {
      if (isContextSifterFilePath) {
        contextSifterDescriptors = readDescriptorsFromFilePaths(contextSifterDescriptors);
      }

      ContextSifter contextSifter = null;
      if (contextSifterDescriptors == null || contextSifterDescriptors.length < 1) {
        return new ContextSifter();
      }

      try {
        contextSifter = this.getContextSifter(contextSifterDescriptors);
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
        System.exit(-1);
      }

      return contextSifter;
    }


    private Map<String, List<String>> getContextSifterFields(String contextDescriptor,
        Set<String> fieldNames) {
      Map<String, List<String>> resultMap = new HashMap<>(fieldNames.size());

      /*
       * Note that the returned TreeMap is sorted in reverse order, so the first key is the position
       * of the last field in the contextDescriptor. The code reads the fields from right to left.
       */
      TreeMap<Integer, String> indexToFieldMap = getFieldIndices(contextDescriptor, fieldNames);

      if (indexToFieldMap.size() > 0) {
        List<String> fieldValues;
        Integer fieldValueEnd = contextDescriptor.length();
        Iterator<Integer> indexIterator = indexToFieldMap.keySet().iterator();

        while (indexIterator.hasNext()) {
          Integer startFieldIndex = indexIterator.next();
          String field = indexToFieldMap.get(startFieldIndex);
          int fieldValueStart = startFieldIndex + field.length();
          fieldValues = getFieldDescriptorValues(contextDescriptor, fieldValueStart, fieldValueEnd);
          resultMap.put(field, fieldValues);
          fieldValueEnd = startFieldIndex;
        }
      }
      return resultMap;
    }


    private RaptatPair<Integer, Integer> getDistances(Map<String, List<String>> sifterConceptMap,
        String reverseDistanceString, String forwardDistanceString) {
      String numberString;
      Integer reverseDistance = 0;
      Integer forwardDistance = 0;

      if (sifterConceptMap.containsKey(reverseDistanceString)
          && sifterConceptMap.containsKey(forwardDistanceString)) {
        numberString = sifterConceptMap.get(reverseDistanceString).get(0);
        reverseDistance = Integer.parseInt(numberString);
        numberString = sifterConceptMap.get(forwardDistanceString).get(0);
        forwardDistance = Integer.parseInt(numberString);
      }

      return new RaptatPair<>(reverseDistance, forwardDistance);
    }


    private List<String> getFieldDescriptorValues(String contextDescriptor, Integer startFieldIndex,
        Integer endFieldIndex) {
      String fieldDescriptor = contextDescriptor.substring(startFieldIndex, endFieldIndex).trim();

      /* Remove separator between field name and its values */
      fieldDescriptor = fieldDescriptor.replaceFirst(
          "\\s*" + TSTrainingParameterObject.CONTEXT_FIELD_NAME_SEPARATOR + "\\s*", "");
      String[] fieldValues = fieldDescriptor.split(
          "\\s*" + TSTrainingParameterObject.CONTEXT_SIFTER_VALUE_SEPARATOR_CHARACTER + "\\s*");

      return Arrays.asList(fieldValues);
    }


    /*
     * Get indices that demarcate the beginning of context sifter field names
     */
    private TreeMap<Integer, String> getFieldIndices(String descriptorField,
        Set<String> fieldNames) {
      TreeMap<Integer, String> indexToFieldMap = new TreeMap<>(Collections.reverseOrder());
      String descriptorLower = descriptorField.toLowerCase();

      for (String field : fieldNames) {
        int fieldIndex = descriptorLower.indexOf(field + ":");
        if (fieldIndex > -1) {
          indexToFieldMap.put(fieldIndex, field);
        }
      }
      return indexToFieldMap;
    }


    private List<RaptatPair<String, String>> getMatchers(String[] descriptors) {
      List<RaptatPair<String, String>> matchers = new ArrayList<>();

      for (String descriptor : descriptors) {
        descriptor = descriptor.replaceAll("\"", "");
        int separatorIndex =
            descriptor.indexOf(TSTrainingParameterObject.DESCRIPTOR_SEPARATOR_CHARACTER);
        if (separatorIndex > 0) {
          String descriptorName = descriptor.substring(0, separatorIndex);
          String descriptorValue = descriptor.substring(separatorIndex + 1);
          matchers.add(new RaptatPair<>(descriptorName, descriptorValue));
        }
      }

      return matchers;
    }


    private <E extends Enum<E>> HashMap<String, Integer> getNormalizedEnumConstants(
        Class<E> enumClass) {
      Enum<E>[] enumConstants = enumClass.getEnumConstants();
      int totalNames = enumConstants.length;
      HashMap<String, Integer> fieldNames = new HashMap<>(totalNames);

      for (int i = 0; i < totalNames; i++) {
        String enumConstantName = enumConstants[i].toString();
        enumConstantName = enumConstantName.replaceAll("_", "").trim().toLowerCase();
        fieldNames.put(enumConstantName, i);
      }
      return fieldNames;
    }


    private List<RaptatPair<String, String>> getSectionMatchers(String[] sectionMatcherDescriptors,
        boolean isSectionMatcherFilePath) {
      if (isSectionMatcherFilePath) {
        sectionMatcherDescriptors = readDescriptorsFromFilePaths(sectionMatcherDescriptors);
      }

      if (sectionMatcherDescriptors == null || sectionMatcherDescriptors.length == 0) {
        return null;
      }
      return getMatchers(sectionMatcherDescriptors);
    }


    /**
     * Helper method to convert array of String instances to constructor values for a
     * GeneralContextAtributeAnalyzer and uses them to return a single instance of the
     * GeneralContextAtributeAnalyzer class.
     *
     * <p>
     * Feb 12, 2018
     *
     * @param analyzerConstructorValues
     * @return
     */
    private SentenceContextAttributeAnalyzer getSentenceContextAttributeAnalyzer(
        String[] analyzerConstructorValues) {
      int fieldIndex;
      fieldIndex =
          RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.RETURN_DEFAULT.ordinal();
      boolean returnDefault = Boolean.parseBoolean(analyzerConstructorValues[fieldIndex]);

      fieldIndex = RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.SENTENCE_CONTEXT_NAME
          .ordinal();
      String sentenceContextName = analyzerConstructorValues[fieldIndex];

      fieldIndex =
          RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.ATTRIBUTE_NAME.ordinal();
      String attributeName = analyzerConstructorValues[fieldIndex];

      fieldIndex =
          RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.DEFAULT_ATTRIBUTE_VALUE
              .ordinal();
      String defaultAttributeValue = analyzerConstructorValues[fieldIndex];

      fieldIndex =
          RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.CONTEXT_ASSIGNED_ATTRIBUTE_VALUE
              .ordinal();
      String contextAssignedAttributeValue = analyzerConstructorValues[fieldIndex];

      SentenceContextAttributeAnalyzer analyzer =
          new SentenceContextAttributeAnalyzer(attributeName, sentenceContextName,
              defaultAttributeValue, contextAssignedAttributeValue, returnDefault);
      return analyzer;
    }


    /**
     * Feb 12, 2018
     *
     * @param analyzerDescriptors
     * @return
     * @throws IllegalArgumentException
     */
    private Set<SentenceContextAttributeAnalyzer> getSentenceContextAttributeAnalyzers(
        String[] analyzerDescriptors) throws IllegalArgumentException {

      Set<SentenceContextAttributeAnalyzer> analyzerSet = new HashSet<>(analyzerDescriptors.length);
      HashMap<String, Integer> analyzerFields = this.getNormalizedEnumConstants(
          RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.class);

      for (String analyzerDescriptor : analyzerDescriptors) {
        analyzerDescriptor = analyzerDescriptor.replaceAll("\"", "");
        String[] concatenatedKeysAndValues = analyzerDescriptor.split(",");
        String[] analyzerConstructorValues = new String[concatenatedKeysAndValues.length];

        for (String concatenatedKeyAndValue : concatenatedKeysAndValues) {
          String[] keyAndValue = concatenatedKeyAndValue
              .split(TSTrainingParameterObject.DESCRIPTOR_SEPARATOR_CHARACTER);
          String key = keyAndValue[0].trim().toLowerCase();
          String value = keyAndValue[1].trim().toLowerCase();

          /* Validate field names provided */
          if (!analyzerFields.containsKey(key)) {
            GeneralHelper.errorWriter(
                "SentenceContextAnalyzer instance improperly specified in properties file");
            throw new IllegalArgumentException(
                "SentenceContextAnalyzer instance improperly specified in properties file");
          }
          int analyzerFieldIndex = analyzerFields.get(key);
          analyzerConstructorValues[analyzerFieldIndex] = value;
        }

        SentenceContextAttributeAnalyzer analyzer =
            getSentenceContextAttributeAnalyzer(analyzerConstructorValues);

        analyzerSet.add(analyzer);
      }

      return analyzerSet;
    }


    private Set<SentenceContextAttributeAnalyzer> getSentenceContextAttributeAnalyzers(
        String[] sentenceContextAnalyzerDescriptors, boolean isSentenceContextAnalyzerFilePath) {
      if (isSentenceContextAnalyzerFilePath) {
        sentenceContextAnalyzerDescriptors =
            readDescriptorsFromFilePaths(sentenceContextAnalyzerDescriptors);
      }

      Set<SentenceContextAttributeAnalyzer> analyzers = new HashSet<>();
      if (sentenceContextAnalyzerDescriptors == null
          || sentenceContextAnalyzerDescriptors.length < 1) {
        return analyzers;
      }

      try {
        analyzers = this.getSentenceContextAttributeAnalyzers(sentenceContextAnalyzerDescriptors);
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
        System.exit(-1);
      }

      return analyzers;
    }


    private List<RaptatPair<String, String>> getSentenceMatchers(
        String[] sentenceMatcherDescriptors, boolean isSentenceMatcherFilePath) {
      if (isSentenceMatcherFilePath) {
        sentenceMatcherDescriptors = readDescriptorsFromFilePaths(sentenceMatcherDescriptors);
      }

      if (sentenceMatcherDescriptors == null || sentenceMatcherDescriptors.length == 0) {
        return null;
      }
      return getMatchers(sentenceMatcherDescriptors);
    }
  }

  /*
   * Nested class to store objects that are use needed for both training and annotation.
   */
  public class TrainingAndAnnotationParameterObject implements Serializable {
    private static final long serialVersionUID = 2489368833204447484L;
    public final boolean invertTokenSequence;
    public final boolean usePOS;
    public final boolean useStems;
    public final boolean removeStopWords;
    public final int trainingTokenNumber;

    /*
     * Path to the concepts in the schema to be scored by cross-validation
     */
    public final HashSet<String> acceptedConcepts;

    /*
     * Path to the schema file that describes the potentially annotated concepts, attributes, and
     * values
     */
    public final List<SchemaConcept> schemaConcepts;


    private TrainingAndAnnotationParameterObject(String schemaFilePath, String conceptsFilePath,
        boolean invertTokenSequence, boolean useStems, boolean usePOS, boolean removeStopWords,
        int trainingTokenNumber) {
      this.schemaConcepts = getSchemaConcepts(schemaFilePath.replaceAll("\"", ""));
      this.acceptedConcepts = getAcceptedConcepts(conceptsFilePath.replaceAll("\"", ""));
      this.invertTokenSequence = invertTokenSequence;
      this.useStems = useStems;
      this.usePOS = usePOS;
      this.removeStopWords = removeStopWords;
      this.trainingTokenNumber = trainingTokenNumber;
    }


    /**
     * Oct 17, 2017
     *
     * @return
     */
    public HashSet<String> getAcceptedConcepts(String conceptsFilePath) {
      HashSet<String> acceptedConcepts = null;
      if (conceptsFilePath != null) {
        acceptedConcepts = GeneralHelper.retrieveLinesAsSet(conceptsFilePath);
      }
      return acceptedConcepts;
    }


    private List<SchemaConcept> getSchemaConcepts(String schemaFilePath) {
      schemaFilePath = schemaFilePath.replaceAll("\"", "");
      return SchemaImporter.importSchemaConcepts(schemaFilePath);
    }
  }

  public class TrainingOnlyParameterObject implements Serializable {
    private static final long serialVersionUID = -8348813672599710800L;
    final String trainingDataFilePath;
    final AnnotationApp annotationApp;
    final Collection<SentenceFilter> sentenceFilters;
    final TSFinderMethod tsFinderMethod;

    /* Smoothing method for token sequences */
    final PhraseIDSmoothingMethod tsSmoothingMethod;

    /*
     * This allows a user to keep the unlabeledHashTree instances on disk during training, which may
     * be necessary due to memory issues when using on large variable text. As of 2/16/18, it cannot
     * be set via a TSTrainingParser and is included here for later updating so that it can be set
     * via the parser
     */
    final boolean keepUnlabeledOnDisk = OptionsManager.getInstance().getKeepUnlabeledOnDisk();

    /*
     * Allows user to save unlabeledHashTree instances to disk for updating the solution after
     * training
     */
    final boolean saveUnlabeledToDisk = OptionsManager.getInstance().getSaveUnlabeledToDisk();


    private TrainingOnlyParameterObject(String trainingDataFilePath, AnnotationApp annotationApp,
        String[] sentenceFilterDescriptors, String tsFinderMethodDescriptor,
        String tsSmoothingMethod) {
      this.trainingDataFilePath = trainingDataFilePath.replaceAll("\"", "");
      this.annotationApp = annotationApp;
      this.sentenceFilters = getSentenceFilters(sentenceFilterDescriptors);
      this.tsFinderMethod =
          TSFinderMethod.valueOf(tsFinderMethodDescriptor.replaceAll("\"", "").toUpperCase());
      this.tsSmoothingMethod =
          PhraseIDSmoothingMethod.valueOf(tsSmoothingMethod.replaceAll("\"", "").toUpperCase());
    }


    /**
     * Oct 18, 2017
     *
     * @param phraseFilterParameters
     * @param left
     * @return
     */
    private PhraseFilter getPhraseFilter(String filterName,
        Collection<String> phraseFilterParameters) {
      PhraseFilter phraseFilter = null;

      if (filterName != null && !filterName.isEmpty()) {
        try {
          Class<?> phraseFilterClass = Class.forName(filterName);
          if (phraseFilterParameters == null || phraseFilterParameters.isEmpty()) {
            Constructor<?> classConstructor = phraseFilterClass.getConstructor();
            phraseFilter = (PhraseFilter) classConstructor.newInstance();
          } else {
            Constructor<?> classConstructor = phraseFilterClass.getConstructor(Collection.class);
            phraseFilter = (PhraseFilter) classConstructor.newInstance(phraseFilterParameters);
          }

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
            | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
          System.err.println("Unable to construct PhraseFilter from command line argument, "
              + filterName + ", " + e.getLocalizedMessage());
          e.printStackTrace();

          System.exit(-1);
        }
      }

      return phraseFilter;
    }


    /**
     * Oct 17, 2017
     *
     * @return
     */
    private Collection<RaptatPair<RaptatPair<String, String>, Collection<String>>> getSentenceAndPhraseFilterDescriptors(
        String[] sentenceFilterDescriptors) {
      List<RaptatPair<RaptatPair<String, String>, Collection<String>>> resultList =
          new ArrayList<>();

      if (sentenceFilterDescriptors != null) {
        for (String filtersAndParameters : sentenceFilterDescriptors) {
          filtersAndParameters = filtersAndParameters.replaceAll("\"", "");
          String[] filtersAndParametersArray =
              filtersAndParameters.split(TSTrainingParameterObject.SENTENCE_FILTER_SPLITTER_REGEX);

          RaptatPair<String, String> filterAndPhraseNames =
              new RaptatPair<>(filtersAndParametersArray[0], filtersAndParametersArray[1]);

          Collection<String> phraseParameters = new HashSet<>(filtersAndParametersArray.length - 2);

          for (int i = 2; i < filtersAndParametersArray.length; i++) {
            phraseParameters.add(filtersAndParametersArray[i]);
          }

          resultList.add(new RaptatPair<>(filterAndPhraseNames, phraseParameters));
        }
      }
      return resultList;
    }


    /**
     * Oct 18, 2017
     *
     * @param sentenceFilterName
     * @param phraseFilter
     * @return
     */
    private SentenceFilter getSentenceFilter(String sentenceFilterName, PhraseFilter phraseFilter) {
      SentenceFilter sentenceFilter = null;

      if (sentenceFilterName != null && !sentenceFilterName.isEmpty()) {
        try {
          Class<?> managerClass = Class.forName(sentenceFilterName);
          Constructor<?> classConstructor = managerClass.getConstructor(PhraseFilter.class);
          sentenceFilter = (SentenceFilter) classConstructor.newInstance(phraseFilter);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
            | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
          System.err
              .println("Unable to construct SentenceFilter instance from command line argument, "
                  + sentenceFilterName + ", " + e.getLocalizedMessage());
          e.printStackTrace();

          System.exit(-1);
        }
      }

      return sentenceFilter;
    }


    /**
     * Oct 17, 2017
     *
     * @param sentenceFilterDescriptors2
     * @return
     */
    private Collection<SentenceFilter> getSentenceFilters(String[] sentenceFilterDescriptors) {
      Collection<RaptatPair<RaptatPair<String, String>, Collection<String>>> sentenceAndPhraseFilters =
          getSentenceAndPhraseFilterDescriptors(sentenceFilterDescriptors);
      List<SentenceFilter> sentenceFilters = new ArrayList<>(sentenceAndPhraseFilters.size());

      for (RaptatPair<RaptatPair<String, String>, Collection<String>> filterPair : sentenceAndPhraseFilters) {
        String phraseFilterName = filterPair.left.right;
        Collection<String> phraseFilterParameters = filterPair.right;
        PhraseFilter phraseFilter = getPhraseFilter(phraseFilterName, phraseFilterParameters);

        String sentenceFilterName = filterPair.left.left;
        SentenceFilter sentenceFilter = getSentenceFilter(sentenceFilterName, phraseFilter);
        sentenceFilters.add(sentenceFilter);
      }
      return sentenceFilters;
    }
  }

  private static final long serialVersionUID = 2380345354281381797L;

  private static final String DESCRIPTOR_SEPARATOR_CHARACTER = ":";

  private static final String SENTENCE_FILTER_SPLITTER_REGEX = ":";
  private static final String CONTEXT_SIFTER_VALUE_SEPARATOR_CHARACTER = ",";
  private static final String CONTEXT_FIELD_NAME_SEPARATOR = ":";
  public final AnnotationOnlyParameterObject annotationOnlyParameters;

  public final TrainingOnlyParameterObject trainingOnlyParameters;

  public final TrainingAndAnnotationParameterObject trainingAndAnnotationParameters;

  public final String timeDateStamp = String.valueOf(System.currentTimeMillis());


  /*
   * Used to track when the parameter object was created for use in training - will also serve as
   * timestamp for solution file generated during training final String creationTimeStamp =
   * Long.toString( System.currentTimeMillis() );
   *
   *
   *
   * @param trainingDataFilePath
   *
   * @param schemaFilePath
   *
   * @param trainingGroupSizeParameter
   *
   * @param annotationApp
   *
   * @param conceptsFilePath
   *
   * @param dictionaryFilePath
   *
   * @param sentenceFilterDescriptors
   *
   * @param startIndexParameter
   *
   * @param stopIndexParameter
   *
   * @param attributeTaggers
   *
   * @param attributeConflictManagers
   *
   * @param tsFinderMethod
   *
   * @param sectionMatcherDescriptor
   *
   * @param isSectionMatcherFilePath
   *
   * @param sentenceMatcherDescriptor
   *
   * @param isSentenceMatcherFilePath
   *
   * @param contextSifterDescriptor
   *
   * @param isConceptSifterFilePath
   */
  public TSTrainingParameterObject(String trainingDataFilePath, String schemaFilePath,
      AnnotationApp annotationApp, String conceptsFilePath, String dictionaryFilePath,
      String[] sentenceFilterDescriptors, String[] attributeTaggerPaths,
      String[] attributeConflictManagersDescriptor, String tsFinderMethodDescriptor,
      String[] annotationGroupPostProcessors, boolean invertTokenSequence, boolean useStems,
      boolean usePOS, boolean removeStopWords, int trainingTokenNumber, String tsSmoothingMethod,
      String[] sentenceContextAnalyzerDescriptors, boolean isSentenceContextAnalyzerFilePath,
      String[] sectionMatcherDescriptors, boolean isSectionMatcherFilePath,
      String[] sentenceMatcherDescriptors, boolean isSentenceMatcherFilePath,
      String[] contextSifterDescriptors, boolean isContextSifterFilePath) {
    super();

    this.trainingOnlyParameters = new TrainingOnlyParameterObject(trainingDataFilePath,
        annotationApp, sentenceFilterDescriptors, tsFinderMethodDescriptor, tsSmoothingMethod);
    this.trainingAndAnnotationParameters =
        new TrainingAndAnnotationParameterObject(schemaFilePath, conceptsFilePath,
            invertTokenSequence, useStems, usePOS, removeStopWords, trainingTokenNumber);

    /*
     * Note that trainingAndAnnotationParameters.acceptedConcepts is generated by the call to the
     * TrainingAndAnnotationParameterObject() constructor via the conceptsFilePath parameter.
     */
    this.annotationOnlyParameters = new AnnotationOnlyParameterObject(dictionaryFilePath,
        this.trainingAndAnnotationParameters.acceptedConcepts, attributeTaggerPaths,
        attributeConflictManagersDescriptor, annotationGroupPostProcessors,
        sentenceContextAnalyzerDescriptors, isSentenceContextAnalyzerFilePath,
        sectionMatcherDescriptors, isSectionMatcherFilePath, sentenceMatcherDescriptors,
        isSentenceMatcherFilePath, contextSifterDescriptors, isContextSifterFilePath);
  }


  @SuppressWarnings("unused")
  private List<String> readDescriptorsFromFilePath(String descriptorPath) {
    descriptorPath = descriptorPath.replaceAll("\"", "").trim();
    List<String> descriptor = null;
    try {
      descriptor = FileUtils.readLines(new File(descriptorPath), "UTF-8");
    } catch (IOException e) {
      System.err.println(
          "Unable to read from descriptor file " + descriptorPath + " " + e.getLocalizedMessage());
      e.printStackTrace();
    }

    ArrayList<String> descriptorClean = new ArrayList<>(descriptor.size());
    Iterator<String> descriptorIterator = descriptor.iterator();
    for (int i = 0; i < descriptor.size(); i++) {
      String nextDescriptor = descriptorIterator.next();
      nextDescriptor = nextDescriptor.replaceAll("\"", "").trim();
      if (nextDescriptor.length() > 0) {
        descriptorClean.add(nextDescriptor);
      }
    }

    return descriptorClean;
  }


  private String[] readDescriptorsFromFilePaths(String[] paths) {
    if (paths == null || paths.length < 1) {
      return new String[0];
    }
    List<String> resultList = new ArrayList<>();
    for (String path : paths) {
      path = path.replaceAll("\"", "");
      resultList.addAll(readDescriptorsFromFilePath(path));
    }
    return resultList.toArray(new String[resultList.size()]);
  }
}
