/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.crossvalidation;

import java.io.File;
import java.io.IOException;
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
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants.TSFinderMethod;
import src.main.gov.va.vha09.grecc.raptat.gg.core.annotation.postprocessing.AnnotationGroupPostProcessor;
import src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers.AttributeConflictManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.ContextSifter;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.preprocessors.contextanalysis.phrasecontext.SentenceContextAttributeAnalyzer;

/**
 * Class used to store parameters used in cross-validation as strings and to convert them to the
 * appropriate types before running cross-validation
 *
 * @author Glenn Gobbel
 */
/** @author VHATVHGOBBEG */
/** @author Glenn Gobbel */
public class CVParameterObject {
  private static final String DESCRIPTOR_SEPARATOR_CHARACTER = ":";

  private static final String SENTENCE_FILTER_SPLITTER_REGEX =
      CVParameterObject.DESCRIPTOR_SEPARATOR_CHARACTER;

  private static final String POST_PROCESSOR_SPLITTER_REGEX = "\\|";

  private static final String CONTEXT_SIFTER_VALUE_SEPARATOR_CHARACTER = ",";

  private static final String CONTEXT_FIELD_NAME_SEPARATOR =
      CVParameterObject.DESCRIPTOR_SEPARATOR_CHARACTER;

  /* Application used to create the annotations */
  final AnnotationApp annotationApp;

  /* Path to the concepts in the schema to be scored by cross-validation */
  final String trainingConceptsFilePath;

  /*
   * Path to the schema file that describes the potentially annotated concepts, attributes, and
   * values
   */
  final String schemaFilePath;

  /*
   * Path to text files containing the regex patterns and the name of the patterns to be matched to
   * identify certain section types in documents
   */
  final String[] sectionMatcherDescriptors;

  /*
   * Paths to text files containing the regex patterns and the name of the patterns to be matched to
   * identify certain section types in documents
   */
  final String[] sentenceMatcherDescriptors;

  final String[] attributeTaggerPaths;

  final String dictionaryFilePath;

  final String[] attributeConflictManagersParameter;

  final String trainingGroupSizeParameter;

  final String[] sentenceFilterParameters;

  final String tsFinderMethodParameter;

  final String trainingDataFilePath;

  final String startIndexParameter;

  final String stopIndexParameter;

  private final String[] annotationGroupPostProcessors;

  private final String[] generalContextAnalyzerDescriptors;

  final boolean isSectionMatcherFilePath;

  final boolean isSentenceMatcherFilePath;

  private final String[] contextSifterDescriptors;

  private final boolean isContextSifterFilePath;

  private final boolean isGeneralContextAnalyzerFilePath;

  private String testingDirectoryFilePath;

  final String scoringConceptsFilePath;


  /**
   * @param trainingDataFilePath
   * @param schemaFilePath
   * @param trainingGroupSizeParameter
   * @param annotationApp
   * @param trainingConceptsFilePath
   * @param dictionaryFilePath
   * @param sentenceFilterDescriptors
   * @param startIndexParameter
   * @param stopIndexParameter
   * @param attributeTaggers
   * @param attributeConflictManagers
   * @param tsFinderMethod
   * @param sectionMatcherDescriptor
   * @param isSectionMatcherFilePath
   * @param sentenceMatcherDescriptor
   * @param isSentenceMatcherFilePath
   * @param contextSifterDescriptor
   * @param isConceptSifterFilePath
   */
  public CVParameterObject(String trainingDataFilePath, String testingDirectoryFilePath,
      String schemaFilePath, String trainingGroupSizeParameter, AnnotationApp annotationApp,
      String trainingConceptsFilePath, String scoringConceptsFilePath, String dictionaryFilePath,
      String[] sentenceFilterParameters, String startIndexParameter, String stopIndexParameter,
      String[] attributeTaggerPaths, String[] attributeConflictManagersParameter,
      String tsFinderMethodParameter, String[] annotationGroupPostProcessors,
      String[] generalContextAnalyzerDescriptors, boolean isGeneralContextAnalyzerFilePath,
      String[] sectionMatcherDescriptors, boolean isSectionMatcherFilePath,
      String[] sentenceMatcherDescriptors, boolean isSentenceMatcherFilePath,
      String[] contextSifterDescriptors, boolean isContextSifterFilePath) {
    super();
    this.trainingDataFilePath = trainingDataFilePath;
    this.testingDirectoryFilePath = testingDirectoryFilePath;
    this.schemaFilePath = schemaFilePath;
    this.trainingGroupSizeParameter = trainingGroupSizeParameter;
    this.annotationApp = annotationApp;
    this.trainingConceptsFilePath = trainingConceptsFilePath;
    this.scoringConceptsFilePath = scoringConceptsFilePath;
    this.dictionaryFilePath = dictionaryFilePath;
    this.sentenceFilterParameters = sentenceFilterParameters;
    this.startIndexParameter = startIndexParameter;
    this.stopIndexParameter = stopIndexParameter;
    this.attributeTaggerPaths = attributeTaggerPaths;
    this.attributeConflictManagersParameter = attributeConflictManagersParameter;
    this.tsFinderMethodParameter = tsFinderMethodParameter;
    this.annotationGroupPostProcessors = annotationGroupPostProcessors;
    this.generalContextAnalyzerDescriptors = generalContextAnalyzerDescriptors;
    this.isGeneralContextAnalyzerFilePath = isGeneralContextAnalyzerFilePath;
    this.sectionMatcherDescriptors = sectionMatcherDescriptors;
    this.isSectionMatcherFilePath = isSectionMatcherFilePath;
    this.sentenceMatcherDescriptors = sentenceMatcherDescriptors;
    this.isSentenceMatcherFilePath = isSentenceMatcherFilePath;
    this.contextSifterDescriptors = contextSifterDescriptors;
    this.isContextSifterFilePath = isContextSifterFilePath;
  }


  public AnnotationApp getAnnotationApp() {
    return this.annotationApp;
  }


  /**
   * Oct 17, 2017
   *
   * @return
   */
  public List<AttributeConflictManager> getAttributeConflictManagers() {
    Set<String> conflictManagerNames = getAttributeConflictManagerNames();
    List<AttributeConflictManager> conflictManagers = new ArrayList<>(conflictManagerNames.size());

    for (String managerName : conflictManagerNames) {
      try {
        Class<?> managerClass = Class.forName(managerName);
        Constructor<?> classConstructor = managerClass.getConstructor();
        AttributeConflictManager conflictManager =
            (AttributeConflictManager) classConstructor.newInstance();
        conflictManagers.add(conflictManager);
      } catch (ClassNotFoundException | NoSuchMethodException | SecurityException
          | InstantiationException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e) {
        System.err
            .println("Unable to construct attribute confllict managers " + e.getLocalizedMessage());
        e.printStackTrace();
        Arrays.toString(conflictManagerNames.toArray(new String[] {}));
        System.exit(-1);
      }
    }

    return conflictManagers;
  }


  /** @return the attributeTaggers */
  public String[] getAttributeTaggerPaths() {
    return this.attributeTaggerPaths;
  }


  /**
   * Oct 17, 2017
   *
   * @return
   */
  public Collection<AttributeTaggerSolution> getAttributeTaggers() {
    if (this.attributeTaggerPaths == null) {
      return new ArrayList<>();
    }

    List<AttributeTaggerSolution> attributeTaggers =
        new ArrayList<>(this.attributeTaggerPaths.length);

    for (String attributeTaggerPath : this.attributeTaggerPaths) {
      attributeTaggerPath = attributeTaggerPath.replaceAll("\"", "").trim();
      AttributeTaggerSolution solution =
          AttributeTaggerSolution.loadFromFile(new File(attributeTaggerPath));
      attributeTaggers.add(solution);
    }

    return attributeTaggers;
  }


  public ContextSifter getContextSifter() {
    ContextSifter contextSifter = new ContextSifter();

    if (this.contextSifterDescriptors != null && this.contextSifterDescriptors.length > 0) {
      Map<String, Integer> sifterFields =
          this.getNormalizedEnumConstants(RaptatConstants.CONTEXT_SIFTER_FIELDS.class);
      Set<String> sifterFieldNames = sifterFields.keySet();
      for (String sifterDescriptor : this.contextSifterDescriptors) {
        sifterDescriptor = sifterDescriptor.replaceAll("\"", "").trim();
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
    }

    return contextSifter;
  }


  /** @return the dictionaryFilePath */
  public String getDictionaryFilePath() {
    if (this.dictionaryFilePath == null) {
      return null;
    }
    return this.dictionaryFilePath.replaceAll("\"", "").trim();
  }


  public Set<SentenceContextAttributeAnalyzer> getGeneralContextAttributeAnalyzers() {
    if (this.isGeneralContextAnalyzerFilePath) {
      return getSentenceContextAttributeAnalyzersFromFilePath();
    }

    Set<SentenceContextAttributeAnalyzer> analyzers = new HashSet<>();
    if (this.generalContextAnalyzerDescriptors == null
        || this.generalContextAnalyzerDescriptors.length < 1) {
      return analyzers;
    }

    try {
      analyzers = getSentenceContextAttributeAnalyzers(this.generalContextAnalyzerDescriptors);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      System.exit(-1);
    }

    return analyzers;
  }


  public List<AnnotationGroupPostProcessor> getPostProcessors() {
    List<AnnotationGroupPostProcessor> resultList = new ArrayList<>();
    if (this.annotationGroupPostProcessors == null
        || this.annotationGroupPostProcessors.length < 1) {
      return resultList;
    }

    for (String postProcessorDescriptor : this.annotationGroupPostProcessors) {
      try {
        AnnotationGroupPostProcessor postProcessor = null;

        postProcessorDescriptor = postProcessorDescriptor.replaceAll("\"", "").trim();
        String[] classAndParametersArray =
            postProcessorDescriptor.split(CVParameterObject.POST_PROCESSOR_SPLITTER_REGEX);
        Class<?> postProcessorClass = Class.forName(classAndParametersArray[0]);

        List<String> parametersList = null;
        if (classAndParametersArray.length > 1) {
          parametersList = Arrays.asList(GeneralHelper.getSubArray(classAndParametersArray, 1));
        }

        if (parametersList == null) {
          Constructor<?> classConstructor = postProcessorClass.getConstructor();
          postProcessor = (AnnotationGroupPostProcessor) classConstructor.newInstance();
        } else {
          Constructor<?> classConstructor = postProcessorClass.getConstructor(List.class);
          postProcessor =
              (AnnotationGroupPostProcessor) classConstructor.newInstance(parametersList);
        }
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


  /** @return the schemaFilePath */
  public String getSchemaFilePath() {
    return this.schemaFilePath.replaceAll("\"", "").trim();
  }


  /**
   * Oct 17, 2017
   *
   * @return
   */
  public HashSet<String> getScoringConcepts() {
    HashSet<String> scoringConcepts = null;
    if (this.scoringConceptsFilePath != null) {
      scoringConcepts = GeneralHelper.retrieveLinesAsSet(this.scoringConceptsFilePath);
    }

    return scoringConcepts;
  }


  /** @return the sectionMatcherPaths */
  public String[] getSectionMatcherDescriptors() {
    return this.sectionMatcherDescriptors;
  }


  public List<RaptatPair<String, String>> getSectionMatchers() {
    if (this.sectionMatcherDescriptors == null || this.sectionMatcherDescriptors.length == 0) {
      return null;
    }
    return getMatchers(this.sectionMatcherDescriptors);
  }


  /** @return the sentenceFilter */
  public String[] getSentenceFilterParameter() {
    return this.sentenceFilterParameters;
  }


  /**
   * Oct 17, 2017
   *
   * @return
   */
  public Collection<SentenceFilter> getSentenceFilters() {
    Collection<RaptatPair<RaptatPair<String, String>, Collection<String>>> sentenceAndPhraseFilters =
        getSentenceAndPhraseFilterDescriptors();
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


  /** @return the sentenceMatcherPaths */
  public String[] getSentenceMatcherPaths() {
    return this.sentenceMatcherDescriptors;
  }


  public List<RaptatPair<String, String>> getSentenceMatchers() {
    if (this.sentenceMatcherDescriptors == null || this.sentenceMatcherDescriptors.length == 0) {
      return null;
    }
    return getMatchers(this.sentenceMatcherDescriptors);
  }


  /**
   * Oct 18, 2017
   *
   * @return
   */
  public int getStartIndex() {
    return Integer.parseInt(this.startIndexParameter.replaceAll("\"", "").trim());
  }


  /** @return the startIndexParameter */
  public String getStartIndexParameter() {
    return this.startIndexParameter;
  }


  /**
   * Oct 18, 2017
   *
   * @return
   */
  public int getStopIndex() {
    return Integer.parseInt(this.stopIndexParameter.replaceAll("\"", "").trim());
  }


  /** @return the stopIndexParameter */
  public String getStopIndexParameter() {
    return this.stopIndexParameter;
  }


  public String getTestingDirectoryFilePath() {
    if (this.testingDirectoryFilePath != null) {
      return this.testingDirectoryFilePath.replaceAll("\"", "").trim();
    }
    return "";
  }


  /**
   * Oct 17, 2017
   *
   * @return
   */
  public HashSet<String> getTrainingConcepts() {
    HashSet<String> trainingConcepts = null;
    if (this.trainingConceptsFilePath != null) {
      trainingConcepts = GeneralHelper.retrieveLinesAsSet(this.trainingConceptsFilePath);
    }
    return trainingConcepts;
  }


  /** @return the trainingDataFile */
  public String getTrainingDataFile() {
    return this.trainingDataFilePath.replaceAll("\"", "").trim();
  }


  /** @return the trainingDataFilePath */
  public String getTrainingDataFilePath() {
    return this.trainingDataFilePath.replaceAll("\"", "").trim();
  }


  /**
   * Oct 17, 2017
   *
   * @return
   */
  public int getTrainingGroupSize() {
    return Integer.parseInt(this.trainingGroupSizeParameter.replaceAll("\"", "").trim());
  }


  /** @return the trainingGroupSizeParameter */
  public String getTrainingGroupSizeParameter() {
    return this.trainingGroupSizeParameter;
  }


  /**
   * Oct 17, 2017
   *
   * @return
   */
  public TSFinderMethod getTSFinderMethod() {
    return TSFinderMethod
        .valueOf(this.tsFinderMethodParameter.replaceAll("\"", "").trim().toUpperCase());
  }


  /** @return the tsFinderMethod */
  public String getTSFinderMethodParameter() {
    return this.tsFinderMethodParameter;
  }


  /** @return the isSectionMatchFile */
  public boolean isSectionMatchFile() {
    return this.isSectionMatcherFilePath;
  }


  /** @return the isSentenceMatchFile */
  public boolean isSentenceMatchFile() {
    return this.isSentenceMatcherFilePath;
  }


  /**
   * Oct 17, 2017
   *
   * @return
   */
  private Set<String> getAttributeConflictManagerNames() {
    if (this.attributeConflictManagersParameter == null) {
      return new HashSet<>();
    }
    return new HashSet<>(Arrays.asList(this.attributeConflictManagersParameter));
  }


  private String getContextDescription(Map<String, List<String>> sifterConceptMap,
      String contextKey) {
    List<String> resultList;
    if ((resultList = sifterConceptMap.get(contextKey)) == null) {
      resultList = new ArrayList<>();
    }
    return resultList.get(0);
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


  private String getDescriptorFromFilePath(String descriptorPath) {
    String descriptor = null;
    try {
      descriptorPath = descriptorPath.replaceAll("\"", "").trim();
      descriptor = FileUtils.readFileToString(new File(descriptorPath), "UTF-8");
    } catch (IOException e) {
      System.err.println(
          "Unable to read from descriptor file " + descriptorPath + " " + e.getLocalizedMessage());
      e.printStackTrace();
      System.exit(-1);
    } catch (NullPointerException e) {
      System.err.println(
          "Descriptor file path for reading in configuration is null\n" + e.getLocalizedMessage());
      e.printStackTrace();
      System.exit(-1);
    }

    return descriptor;
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
    fieldDescriptor = fieldDescriptor
        .replaceFirst("\\s*" + CVParameterObject.CONTEXT_FIELD_NAME_SEPARATOR + "\\s*", "");
    String[] fieldValues = fieldDescriptor
        .split("\\s*" + CVParameterObject.CONTEXT_SIFTER_VALUE_SEPARATOR_CHARACTER + "\\s*");

    return Arrays.asList(fieldValues);
  }


  /*
   * Get indices that demarcate the beginning of context sifter field names
   */
  private TreeMap<Integer, String> getFieldIndices(String descriptorField, Set<String> fieldNames) {
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
      descriptor = descriptor.replaceAll("\"", "").trim();
      int separatorIndex = descriptor.indexOf(CVParameterObject.DESCRIPTOR_SEPARATOR_CHARACTER);
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
    Enum[] enumConstants = enumClass.getEnumConstants();
    int totalNames = enumConstants.length;
    HashMap<String, Integer> fieldNames = new HashMap<>(totalNames);

    for (int i = 0; i < totalNames; i++) {
      String enumConstantName = enumConstants[i].toString();
      enumConstantName = enumConstantName.replaceAll("_", "");
      fieldNames.put(enumConstants[i].toString().toLowerCase(), i);
    }
    return fieldNames;
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
  private Collection<RaptatPair<RaptatPair<String, String>, Collection<String>>> getSentenceAndPhraseFilterDescriptors() {
    List<RaptatPair<RaptatPair<String, String>, Collection<String>>> resultList = new ArrayList<>();

    if (this.sentenceFilterParameters != null) {
      for (String filtersAndParameters : this.sentenceFilterParameters) {
        filtersAndParameters = filtersAndParameters.replaceAll("\"", "").trim();
        String[] filtersAndParametersArray =
            filtersAndParameters.split(CVParameterObject.SENTENCE_FILTER_SPLITTER_REGEX);

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

    fieldIndex =
        RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.SENTENCE_CONTEXT_NAME.ordinal();
    String sentenceContextName = analyzerConstructorValues[fieldIndex];

    fieldIndex =
        RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.ATTRIBUTE_NAME.ordinal();
    String attributeName = analyzerConstructorValues[fieldIndex];

    fieldIndex = RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.DEFAULT_ATTRIBUTE_VALUE
        .ordinal();
    String defaultAttributeValue = analyzerConstructorValues[fieldIndex];

    fieldIndex =
        RaptatConstants.SENTENCE_CONTEXT_ATTRIBUTE_ANALYZER_FIELDS.CONTEXT_ASSIGNED_ATTRIBUTE_VALUE
            .ordinal();
    String contextAssignedAttributeValue = analyzerConstructorValues[fieldIndex];

    SentenceContextAttributeAnalyzer analyzer = new SentenceContextAttributeAnalyzer(attributeName,
        sentenceContextName, defaultAttributeValue, contextAssignedAttributeValue, returnDefault);
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
      analyzerDescriptor = analyzerDescriptor.replaceAll("\"", "").trim();
      String[] analyzerKeyValues = analyzerDescriptor.split(",");
      String[] analyzerConstructorValues = new String[analyzerKeyValues.length];

      for (String keyValue : analyzerKeyValues) {
        String[] keyAndValue = keyValue.split(CVParameterObject.DESCRIPTOR_SEPARATOR_CHARACTER);

        /* Validate field names provided */
        if (!analyzerFields.containsKey(keyAndValue[0])) {
          GeneralHelper.errorWriter(
              "SentenceContextAnalyzer instance improperly specified in properties file");
          throw new IllegalArgumentException(
              "SentenceContextAnalyzer instance improperly specified in properties file");
        }
        int analyzerFieldIndex = analyzerFields.get(keyAndValue[0]);
        analyzerConstructorValues[analyzerFieldIndex] = keyAndValue[1];
      }

      SentenceContextAttributeAnalyzer analyzer =
          getSentenceContextAttributeAnalyzer(analyzerConstructorValues);

      analyzerSet.add(analyzer);
    }

    return analyzerSet;
  }


  private Set<SentenceContextAttributeAnalyzer> getSentenceContextAttributeAnalyzersFromFilePath() {
    String[] analyzerDescriptors = readDescriptorsFromFiles(this.generalContextAnalyzerDescriptors);
    return getSentenceContextAttributeAnalyzers(analyzerDescriptors);
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
   * Feb 12, 2018
   *
   * @param generalContextAnalyzerDescriptors2
   * @return
   */
  private String[] readDescriptorsFromFiles(String[] generalContextAnalyzerDescriptors) {
    // TODO Auto-generated method stub
    return new String[0];
  }
}
