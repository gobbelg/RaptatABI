package src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import com.github.jcrfsuite.CrfTagger;
import com.github.jcrfsuite.util.CrfSuiteLoader;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.crf.training.CSAttributeTaggerTrainer;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.AttributeTaggerSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.CrfConceptSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.RaptatConstants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;

/**
 * ExecDemo shows how to execute an external program read its output, and print its exit status.
 */
public class CrfSuiteRunner {
  private enum SuiteRunnerType {
    DUMP, CROSS_VALIDATE, MULT_CROSS_VALIDATE, ESTIMATE_PARAMETERS, SIMPLE_TRAIN,
    ESTIMATE_PARAMETERS_MULTIFILE, ESTIMATE_PERFORMANCE, ESTIMATE_PERFORMANCE_MULTIFILE
  }

  private enum Tagger {
    CONCEPT_TAGGER, CUE_TAGGER, SCOPE_TAGGER
  }

  private static boolean COMPILE_RESULTS = true;


  /*
   * Make processes an array so that a single CrfSuiteRunner can run multiple sequential processes
   * if needed
   */
  private final CrfTagger[] taggers = new CrfTagger[Tagger.values().length];


  public CrfTagger getConceptTagger() {
    return this.taggers[Tagger.CONCEPT_TAGGER.ordinal()];
  }

  public CrfTagger getCueTagger() {
    return this.taggers[Tagger.CUE_TAGGER.ordinal()];
  }


  public CrfTagger getScopeTagger() {
    return this.taggers[Tagger.SCOPE_TAGGER.ordinal()];
  }


  /**
   * @param taggerSolution
   */
  public void initialize(AttributeTaggerSolution taggerSolution) {
    String directoryPath = UserPreferences.INSTANCE.getWorkingDirectoryPath();

    /*
     * These paths are to files where the models will be temporarily stored during running of the
     * CRFSuite library, which requires that the models be files on the file system
     */
    String cueFilePath =
        directoryPath + File.separator + "cueModel_" + GeneralHelper.getTimeStamp() + ".mdl";
    String scopeFilePath =
        directoryPath + File.separator + "scopeModel_" + GeneralHelper.getTimeStamp() + ".mdl";

    taggerSolution.writeCueModel(cueFilePath);
    taggerSolution.writeScopeModel(scopeFilePath);

    this.taggers[Tagger.CUE_TAGGER.ordinal()] = new CrfTagger(cueFilePath);
    this.taggers[Tagger.SCOPE_TAGGER.ordinal()] = new CrfTagger(scopeFilePath);
  }


  /**
   * The CRF models are stored in the Raptat solution files as byte []s. To use them in the CRFSuite
   * code, they must be on disk as individual binary files. So, to allow their use by CRFSuite, we
   * read in the CRF models to byte []s, and then write them out to individual files that *should*
   * be temporary.
   *
   * @param taggerSolution
   */
  public void initialize(CrfConceptSolution taggerSolution) {
    String directoryPath = UserPreferences.INSTANCE.getWorkingDirectoryPath();

    /*
     * These paths are to files where the model will be temporarily stored during running of the
     * CRFSuite library, which requires that the model be files on the file system
     */
    String modelFilePath =
        directoryPath + File.separator + "model_" + GeneralHelper.getTimeStamp() + ".mdl";

    taggerSolution.writeModel(modelFilePath);

    this.taggers[Tagger.CONCEPT_TAGGER.ordinal()] = new CrfTagger(modelFilePath);
  }


  public static void main(String[] args) {
    UserPreferences.INSTANCE.initializeLVGLocation();

    SwingUtilities.invokeLater(new Runnable() {
      String generalFileDirectory = "D:\\CARTCL-IIR\\Glenn\\AKI_NegationTraining_160714"; // \\Rd01TrainingFeatures\\Rd02TrainingFeatures";


      @Override
      public void run() {
        String featureFileName = "ReasonNoMedsModelTraining__filter01_160617_015843.txt";
        String modelFileName = "CueModel_161107_154802.mdl";
        String trainingFileName =
            "CueFeatureTraining_Rd00_CSTrainingFeatures_UnprocessedTokens_v01_WO_4_WNG_4_PO_4_PNG_4_160714_123827.txt";
        String trainingFileListFileName = "MultiCrossVal_160614.txt";

        OptionsManager options = OptionsManager.getInstance();
        TokenProcessingOptions tokenProcessingOptions = new TokenProcessingOptions(options);

        int samples;

        SuiteRunnerType runType = SuiteRunnerType.DUMP;
        try {
          switch (runType) {
            case CROSS_VALIDATE:
              runCrossValidation(trainingFileName);
              break;

            case DUMP:
              dumpModelFile(modelFileName);
              break;

            case MULT_CROSS_VALIDATE:
              multiFileCrossValidation(trainingFileListFileName);
              break;

            case ESTIMATE_PARAMETERS_MULTIFILE:
              samples = 8;
              String trainingFilesDirectoryPath =
                  "D:\\CARTCL-IIR\\Glenn\\AKI_NegationTraining_160711\\Rd01TrainingFeatures";
              multiFileParameterEstimation(trainingFilesDirectoryPath, samples,
                  tokenProcessingOptions);
              break;

            case ESTIMATE_PARAMETERS:
              samples = 10;
              this.runParameterEstimation(trainingFileName, samples, tokenProcessingOptions);
              break;

            case SIMPLE_TRAIN:
              trainFromFeatureFile(featureFileName);
              break;

            /*
             * This case is used to test an existing CRFSuite model file on a feature file with
             * regard to precision, recall, etc. Generally, for valid estimations, the model file
             * will have been trained on data independent from that in the feature data used for
             * testing.
             */
            case ESTIMATE_PERFORMANCE: {
              String pathToModelFile =
                  "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CV10FoldSolution_Filtered_Training_OvertRnmSelectedSent_160627_154351\\Group_1\\CrfSolution_Group_1.mdl";
              String pathToTestingFeatures =
                  "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CVFeatures_Unfiltered_Testing_RmnAndBbSent_160623_231231\\CVGroup_1\\CVGroup_TestingFile_1.txt";
              estimatePerformance(pathToModelFile, pathToTestingFeatures);
            }
              break;

            case ESTIMATE_PERFORMANCE_MULTIFILE: {
              String pathToModelDirectory =
                  "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CV10FoldSolution_Filtered_Training_OvertRnmSelectedSent_160627_154351";
              String pathToTestingDirectory =
                  "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CVFeatures_Filtered_Testing_RmnAndBbSent_160623_231231";
              for (int group = 1; group <= 2; group++) {
                System.out.println("\n\n-------------------------\n" + "Testing group:" + group
                    + "\n-------------------------\n");
                String pathToModelFile = pathToModelDirectory + File.separator + "Group_" + group
                    + File.separator + "CrfSolution_Group_" + group + ".mdl";
                String pathToTestingFeatures = pathToTestingDirectory + File.separator + "CVGroup_"
                    + group + File.separator + "CVGroup_TestingFile_" + group + ".txt";
                estimatePerformance(pathToModelFile, pathToTestingFeatures);
              }
            }
              break;
          }
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }


      /**
       * Method to determine optimal parameters for running the crfsuite tool with respect to
       * various learning metaparameters, such as trainign method. It uses an existing file,
       * trainingFileName, with features and labels in crfsuite format, for running the
       * cross-validation. The method returns the results as a map where the tag of a labeled token
       * is mapped to performance measures given as a single, tab-delimited string indicating true
       * positive, false positives, etc
       *
       * @param trainingFileName
       * @return
       * @throws IOException
       */
      public Map<String, String> runCrossValidation(String trainingFileName) throws IOException {
        String trainingFilePath = this.generalFileDirectory + File.separator + trainingFileName;
        System.out.println("Running cross-validation on file:" + trainingFilePath);
        Process p;

        // Process tracks one external native process
        ProcessBuilder builder =
            new ProcessBuilder(RaptatConstants.CRFSUITE_PATH, "learn", "-g10", "-x", "-");

        // ProcessBuilder builder = new ProcessBuilder(
        // Constants.CRFSUITE_PATH,
        // "learn", "-g10", "-x", "-a", "lbfgs", "-p", "c1=0.01", "-p",
        // "c2=0", "-" );

        // ProcessBuilder builder = new ProcessBuilder(
        // Constants.CRFSUITE_PATH,
        // "learn", "-g10", "-x", "-a", "lbfgs", "-p", "c1=0", "-p",
        // "c2=0.1", "-" );

        // ProcessBuilder builder = new ProcessBuilder(
        // Constants.CRFSUITE_PATH,
        // "learn", "-g10", "-x", "-a", "l2sgd", "-p", "c2=0.1", "-" );

        // ProcessBuilder builder = new ProcessBuilder(
        // Constants.CRFSUITE_PATH,
        // "learn", "-g10", "-x", "-a", "arow", "-p",
        // "max_iterations=200", "-" );

        builder.redirectErrorStream(true);
        p = builder.start();

        String line;
        String on = "";
        String bn = "";
        String in = "";
        List<String> ons = new ArrayList<>();
        List<String> bns = new ArrayList<>();
        List<String> ins = new ArrayList<>();
        Map<String, String> resultHashMap = new HashMap<>();

        System.out.println("In Main after exec");

        /*
         * Now create streams to read in from the file and write to the process
         */
        File inFile = new File(trainingFilePath);

        FileReader fr = new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);
        OutputStream processOutstream = p.getOutputStream();
        PrintWriter pw =
            new PrintWriter(new BufferedWriter(new OutputStreamWriter(processOutstream)));

        /*
         * 'line=br.readLine()' reads in the data from a file, while 'pw.print(line+\n)' writes that
         * line so that the process, 'p', can read it
         */
        while ((line = br.readLine()) != null) {
          pw.print(line + "\n");
        }

        pw.close();
        br.close();

        System.err.println("File reading complete");

        // getInputStream gives an Input stream connected to
        // the process p's standard output. Use it to make
        // a BufferedReader to readLine() what the program writes out.
        InputStream instreamFromProcess = p.getInputStream();
        BufferedReader is = new BufferedReader(new InputStreamReader(instreamFromProcess));

        while ((line = is.readLine()) != null) {
          String trimmedLine = line.trim();
          // System.out.println( "\n" + line );
          if (trimmedLine.startsWith("===== Cross validation")) {
            System.out.println("\n" + line);
          }
          if (trimmedLine.startsWith("O:")) {
            on = trimmedLine;
          }
          if (trimmedLine.startsWith("B")) {
            bn = trimmedLine;
          }
          if (!trimmedLine.startsWith("It") && !trimmedLine.startsWith("Ins")
              && trimmedLine.startsWith("I")) {
            in = trimmedLine;
          }
          if (trimmedLine.contains("Total seconds")) {
            ons.add(on);
            bns.add(bn);
            ins.add(in);

            System.out.println(on);
            System.out.println(bn);
            System.out.println(in);
          }
        }

        System.out.println("In Main after EOF");
        System.out.flush();
        try {
          p.waitFor(); // wait for process to complete
        } catch (InterruptedException e) {
          System.err.println(e); // "Can'tHappen"
          return null;
        } finally {
          if (is != null) {
            is.close();
            p.destroy();
          }
        }

        if (CrfSuiteRunner.COMPILE_RESULTS && !bns.isEmpty() && !ins.isEmpty() && !ons.isEmpty()) {
          String resultTag = "BEGIN";
          resultHashMap.put(resultTag, printResults(resultTag, bns));

          resultTag = "INSIDE";
          resultHashMap.put(resultTag, printResults(resultTag, ins));

          resultTag = "OUTSIDE";
          resultHashMap.put(resultTag, printResults(resultTag, ons));
        }

        System.err.println("Process done, exit status was " + p.exitValue());

        return resultHashMap;
      }


      /**
       * Takes a model in the file modelFileName, supplied as a parameter to the method, and dumps
       * the features and their weights to a file in the same directory as the model file.
       *
       * @param modelFileName
       */
      private void dumpModelFile(String modelFileName) {
        try {
          CrfSuiteLoader.load();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

        Process p; // Process tracks one external native process
        String pathToFiles = "D:\\ORD_Matheny_201312053D\\Glenn\\AKI_Analysis_160427";
        String pathToModelFile = pathToFiles + File.separator + modelFileName;
        ProcessBuilder builder =
            new ProcessBuilder(RaptatConstants.CRFSUITE_PATH, "dump", pathToModelFile);
        try {
          p = builder.start();

          // getInputStream gives an Input stream connected to
          // the process p's standard output. Just use it to make
          // a BufferedReader to readLine() what the program writes
          // out.
          InputStream processInstream = p.getInputStream();
          BufferedReader is = new BufferedReader(new InputStreamReader(processInstream));

          String parameterFileName =
              GeneralHelper.removeFileExtension(modelFileName, '.') + "_Params.txt";
          String pathToParameterFile = pathToFiles + File.separator + parameterFileName;
          File parameterFile = new File(pathToParameterFile);
          if (parameterFile.exists()) {
            FileUtils.forceDelete(parameterFile);
          }
          PrintWriter pw = new PrintWriter(parameterFile);

          String line;
          while ((line = is.readLine()) != null) {
            System.out.println(line);
            pw.println(line);
          }

          pw.flush();
          pw.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }


      private void estimatePerformance(String pathToTrainingFeatures,
          String pathToTestingFeatures) {

        Process p; // Process tracks one external native process

        ProcessBuilder builder = new ProcessBuilder(RaptatConstants.CRFSUITE_PATH, "tag", "-m",
            pathToTrainingFeatures, "-qt", pathToTestingFeatures);
        try {
          p = builder.start();
          // getInputStream gives an Input stream connected to
          // the process p's standard output. Just use it to make
          // a BufferedReader to readLine() what the program writes
          // out.
          InputStream processInstream = p.getInputStream();
          BufferedReader is = new BufferedReader(new InputStreamReader(processInstream));

          String line;
          while ((line = is.readLine()) != null) {
            System.out.println(line);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }


      private int findStartLabelPosition(String curFeature) {
        int startLabelPosition = 0;
        if (curFeature.contains(RaptatConstants.BEGIN_CONCEPT_LABEL)) {
          startLabelPosition = curFeature.indexOf("_" + RaptatConstants.BEGIN_CONCEPT_LABEL);
        } else if (curFeature.contains(RaptatConstants.INSIDE_CONCEPT_LABEL)) {
          startLabelPosition = curFeature.indexOf("_" + RaptatConstants.INSIDE_CONCEPT_LABEL);
        } else {
          startLabelPosition = curFeature.indexOf("_" + RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING);
        }
        return startLabelPosition;
      }


      private List<RaptatPair<ItemSequence, StringList>> getAllSentenceFeaturesAndLabels(
          String trainingFilePath) {
        StringList tokenLabels = new StringList();
        Item token = new Item();
        ItemSequence sentenceTokenFeatures = new ItemSequence();
        String curLine;
        List<RaptatPair<ItemSequence, StringList>> resultList = new ArrayList<>(100000);

        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new FileReader(trainingFilePath));
          while ((curLine = reader.readLine()) != null) {
            if (curLine.trim().length() == 0) {
              if (tokenLabels.size() > 0) {
                resultList.add(new RaptatPair<>(sentenceTokenFeatures, tokenLabels));
                tokenLabels = new StringList();
                token = new Item();
                sentenceTokenFeatures = new ItemSequence();
              }
            } else {
              String[] lineStrings = curLine.split("\t");
              tokenLabels.add(lineStrings[0]);
              for (int i = 1; i < lineStrings.length; i++) {
                token.add(new Attribute(lineStrings[i]));
              }
              sentenceTokenFeatures.add(token);
              token = new Item();
            }
          }

          reader.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }

        return resultList;
      }


      private List<String> getFileList(String fileListName) {
        List<String> filePathList = new ArrayList<>();

        try {
          BufferedReader reader = null;
          reader = new BufferedReader(
              new FileReader(this.generalFileDirectory + File.separator + fileListName));
          String curFileName;
          while ((curFileName = reader.readLine()) != null) {
            filePathList.add(curFileName);
          }
          reader.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }

        return filePathList;
      }


      private Map<String, List<Double>> getParameters(List<String> modelFiles) {
        Process p; // Process tracks one external native process
        ProcessBuilder builder;
        Map<String, List<Double>> resultMap = new HashMap<>(100000);

        try {
          String crfSuitePath = RaptatConstants.CRFSUITE_PATH;

          for (String curModelPath : modelFiles) {
            System.out.println("Processing parameters in file:" + new File(curModelPath).getName());
            builder = new ProcessBuilder(crfSuitePath, "dump", curModelPath);
            p = builder.start();

            InputStream processInstream = p.getInputStream();
            BufferedReader is = new BufferedReader(new InputStreamReader(processInstream));
            String inputLine;
            while (!(inputLine = is.readLine()).startsWith("STATE_FEATURES = {")) {
              System.out.println(inputLine);
            }

            RaptatPair<String, Double> feature;
            while ((inputLine = is.readLine()) != null
                && (feature = processLine(inputLine)) != null) {
              List<Double> curFeatureValues;
              if ((curFeatureValues = resultMap.get(feature.left)) == null) {
                curFeatureValues = new ArrayList<>();
                resultMap.put(feature.left, curFeatureValues);
              }
              curFeatureValues.add(feature.right);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

        return resultMap;
      }


      /**
       * Method similar to runCrossValidation that takes a list of file names as a parameter and
       * runs cross-validation via a call to runCrossValidation on each file in the list. This
       * method is generally used to test performance across a set of different feature files to
       * determine how different features affect performance.
       *
       * @param fileListName
       * @throws IOException
       */
      private void multiFileCrossValidation(String fileListName) throws IOException {
        List<String> filePathList = getFileList(fileListName);

        String resultFileName = "multiCrossValResults_" + GeneralHelper.getTimeStamp() + ".txt";
        File resultFile = new File(this.generalFileDirectory + File.separator + resultFileName);
        PrintWriter pw = new PrintWriter(resultFile);

        for (String curFileName : filePathList) {
          StringBuilder sb = new StringBuilder(curFileName);
          Map<String, String> crossValResults = runCrossValidation(curFileName);
          List<String> keys = new ArrayList<>(crossValResults.keySet());
          Collections.sort(keys);
          for (String curKey : keys) {
            sb.append("\t").append(curKey).append("\t").append(crossValResults.get(curKey));
          }
          pw.println(sb);
          pw.flush();
          System.out.println(sb);
        }
      }


      private void multiFileParameterEstimation(String trainingDirectory, int samples,
          TokenProcessingOptions tokenProcessingOptions) {
        Collection<File> fileList = FileUtils.listFiles(new File(trainingDirectory),
            new RegexFileFilter(".*txt"), TrueFileFilter.INSTANCE);
        for (File file : fileList) {
          this.runParameterEstimation(file.getAbsolutePath(), samples, tokenProcessingOptions,
              file.getParent());
        }
      }


      private void processAndPrintParameters(Map<String, List<Double>> parameters,
          String modelFileDirectory) {
        String modelParametersFile = "parameterStats_" + GeneralHelper.getTimeStamp() + ".txt";
        String modelParametersFilePath = modelFileDirectory + File.separator + modelParametersFile;
        Map<String, double[]> featureSummaryStats = new HashMap<>();
        try {
          PrintWriter pw = new PrintWriter(new File(modelParametersFilePath));
          List<String> parameterKeySet = new ArrayList<>(parameters.keySet());
          Collections.sort(parameterKeySet);
          for (String curFeature : parameters.keySet()) {
            int startLabel = findStartLabelPosition(curFeature);

            String featureName = curFeature.substring(0, startLabel);
            String featureLabel = curFeature.substring(startLabel + 1, curFeature.length());

            StringBuilder sb = new StringBuilder(featureName);
            sb.append("\t").append(featureLabel);
            SummaryStatistics stats = new SummaryStatistics();
            List<Double> curValues = parameters.get(curFeature);
            StringBuilder valuesOnly = new StringBuilder();
            for (Double curValue : curValues) {
              stats.addValue(curValue);
              valuesOnly.append("\t").append(curValue);
            }
            double mean = stats.getMean();
            double sd = stats.getStandardDeviation();
            long n = stats.getN();
            double sem = sd / Math.sqrt(n);
            double significance = sem > 0 ? Math.abs(mean) / sem : 0.0;
            double isSignificant = significance > 2.0 ? 1.0 : 0.0;

            sb.append("\t").append(mean);
            sb.append("\t").append(sd);
            sb.append("\t").append(n);
            sb.append("\t").append(sem);
            sb.append("\t").append(significance);
            sb.append("\t").append(isSignificant);

            sb.append(valuesOnly);
            pw.println(sb.toString());

            double[] sumStats;
            if ((sumStats = featureSummaryStats.get(featureName)) == null) {
              sumStats = new double[] {0.0, 0.0};
              featureSummaryStats.put(featureName, sumStats);
            }
            if (isSignificant > 0.0) {
              sumStats[0] += significance;
              sumStats[1] += isSignificant;
            }
          }

          pw.flush();
          pw.close();

          modelParametersFile = "parameterStatsSumary_" + GeneralHelper.getTimeStamp() + ".txt";
          modelParametersFilePath = modelFileDirectory + File.separator + modelParametersFile;

          pw = new PrintWriter(new File(modelParametersFilePath));
          List<String> featureSummaryKeys = new ArrayList<>(featureSummaryStats.keySet());
          Collections.sort(featureSummaryKeys);

          for (String curKey : featureSummaryKeys) {
            StringBuilder sb = new StringBuilder(curKey);
            double[] curStats = featureSummaryStats.get(curKey);
            sb.append("\t").append(Double.toString(curStats[0]));
            sb.append("\t").append(Double.toString(curStats[1]));
            pw.println(sb.toString());
          }
          pw.flush();
          pw.close();
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      }


      private RaptatPair<String, Double> processLine(String inputLine) {
        String[] parameters = inputLine.split(" ");
        if (parameters.length == 7) {
          String featureNameTag;
          if (parameters[5].toLowerCase().startsWith("b")) {
            featureNameTag = RaptatConstants.BEGIN_CONCEPT_LABEL;
          } else if (parameters[5].toLowerCase().startsWith("i")) {
            featureNameTag = RaptatConstants.INSIDE_CONCEPT_LABEL;
          } else {
            featureNameTag = RaptatConstants.DEFAULT_CRF_OUTSIDE_STRING;
          }
          String featureName = parameters[3] + "_" + featureNameTag;
          Double featureValue = new Double(parameters[6]);
          return new RaptatPair<>(featureName, featureValue);
        }
        return null;
      }


      /**
       * Takes a feature file found at trainingFilePath that is in Crfsuite format and divides it
       * into 'samples' number of random samples of the sentences in the feature file. The samples
       * are used to train a CRF model 'samples' times, and the features associated with each model
       * are printed out to a file so that the variation in model parameters can be assessed.
       *
       * @param trainingFileName
       * @param samples
       * @param tokenProcessingOptions
       */
      private void runParameterEstimation(String trainingFilePath, int samples,
          TokenProcessingOptions tokenProcessingOptions) {
        this.runParameterEstimation(trainingFilePath, samples, tokenProcessingOptions,
            this.generalFileDirectory);
      }


      /**
       * Takes a feature file, trainingFileName, in crfsuite format and divides it into 'samples'
       * number of random samples of the sentences in the feature file. The samples are used to
       * train a CRF model 'samples' times, and the features associated with each model are printed
       * out to a file so that the variation in model parameters can be assessed.
       *
       * @param trainingFileName
       * @param samples
       * @param tokenProcessingOptions
       */
      private void runParameterEstimation(String trainingFilePath, int samples,
          TokenProcessingOptions tokenProcessingOptions, String resultDirectory) {
        try {
          CrfSuiteLoader.load();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

        System.out.println("File:" + new File(trainingFilePath).getName());
        System.out.println("Getting all sentences and feature labels");
        List<RaptatPair<ItemSequence, StringList>> sentences =
            getAllSentenceFeaturesAndLabels(trainingFilePath);

        System.out.println("Randomizing samples");
        List<List<RaptatPair<ItemSequence, StringList>>> dataSamples =
            GeneralHelper.createRandomSamples(sentences, samples);
        CSAttributeTaggerTrainer trainer = new CSAttributeTaggerTrainer(tokenProcessingOptions);
        List<String> modelFiles = new ArrayList<>(samples);

        int i = 1;
        for (List<RaptatPair<ItemSequence, StringList>> curSample : dataSamples) {
          List<ItemSequence> itemSequenceList = new ArrayList<>(curSample.size());
          List<StringList> labelList = new ArrayList<>(curSample.size());
          for (RaptatPair<ItemSequence, StringList> curPair : curSample) {
            itemSequenceList.add(curPair.left);
            labelList.add(curPair.right);
          }

          String version = String.format("v%01d_", i++);
          String modelFilePath = resultDirectory + File.separator + "modelFile_" + version
              + GeneralHelper.getTimeStamp() + ".mdl";
          modelFiles.add(modelFilePath);
          System.out.println("Training Model:" + modelFilePath);
          trainer.train(itemSequenceList, labelList, modelFilePath);
        }

        Map<String, List<Double>> parameters = getParameters(modelFiles);
        processAndPrintParameters(parameters, resultDirectory);
      }


      /**
       * Method takes the name of a feature file as a parameter and generates a CRF model based on
       * that file. Unlike the trainNegation() method in the CSAttributeTaggerTrainer class, this
       * method does generic CRF model generation.
       *
       * @param featureFileName
       */
      private void trainFromFeatureFile(String featureFileName) {
        Process p; // Process tracks one external native process
        String pathToFiles =
            "P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CVFeatures_160615_141634\\CVGroup_4";
        String pathToFeatureFile = pathToFiles + File.separator + featureFileName;
        String modelName = GeneralHelper.removeFileExtension(featureFileName, '.') + "_CRFModel_"
            + GeneralHelper.getTimeStamp() + ".mdl";
        String pathToModelFile = pathToFiles + File.separator + modelName;

        ProcessBuilder builder = new ProcessBuilder(RaptatConstants.CRFSUITE_PATH, "learn", "-m",
            pathToModelFile, pathToFeatureFile);

        try {
          p = builder.start();
          // getInputStream gives an Input stream connected to
          // the process p's standard output. Just use it to make
          // a BufferedReader to readLine() what the program writes
          // out.
          InputStream processInstream = p.getInputStream();
          BufferedReader is = new BufferedReader(new InputStreamReader(processInstream));

          String line;
          while ((line = is.readLine()) != null) {
            System.out.println(line);
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
  }


  private static String printResults(String variableName, List<String> results) {
    StringBuilder result = new StringBuilder(variableName);
    int match = 0;
    int model = 0;
    int ref = 0;
    int tp, fp, fn;

    for (String curString : results) {
      String[] performanceStrings = StringUtils.split(curString, "(,)");
      System.out.println(curString);
      match += Integer.parseInt(performanceStrings[1].trim());
      model += Integer.parseInt(performanceStrings[2].trim());
      ref += Integer.parseInt(performanceStrings[3].trim());
    }
    tp = match;
    fp = model - match;
    fn = ref - match;

    double precision = GeneralHelper.calcPrecision(tp, fp);
    double recall = GeneralHelper.calcRecall(tp, fn);
    double f = GeneralHelper.calcF_Measure(tp, fp, fn);

    System.out.println("------------------------------------------");
    System.out.println("TP:" + tp + ", FP:" + fp + ", FN:" + fn);
    System.out.println("Precision:" + String.format("%1$.3f", precision) + ", Recall:"
        + String.format("%1$.3f", recall) + ", F:" + String.format("%1$.3f", f));
    System.out.println("------------------------------------------");

    result.append("\t").append(tp);
    result.append("\t").append(fp);
    result.append("\t").append(fn);
    result.append("\t").append(precision);
    result.append("\t").append(recall);
    result.append("\t").append(f);
    return result.toString();
  }
}
