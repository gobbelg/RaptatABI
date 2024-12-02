package src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

/**
 * ******************************************************************************
 *
 * @author Shrimalini Jayaramaraja March 8th 2011 *
 *         <p>
 *         The BootStrap class generates a random * sample from the set of Annotations based on a
 *         group by criteria. The * annotations may be grouped by any grouping column/criteria or
 *         without any * grouping, over the entire data set. * * *
 *         ******************************************************************************
 */
public class ConceptMapBootstrapAnalyzer {
  public int randIndex = 0;
  public int defaultSampleSize = 0;
  public int iterations = 1;

  private List<String[]> result = new Vector<>();
  private HashMap<String, String[]> myMap = new HashMap<>(3000000);
  protected HashMap<String, List<String[]>> allGrpColLists;
  private ConceptMapBootstrapSampler allData;
  private long seed;
  private Random rd;
  private String[] GrpColVals;
  private int numOfPatients;
  private int numOfGrByVals;
  private int numOfDocuments;


  public ConceptMapBootstrapAnalyzer() {}


  /**
   * *********************************************************************** public
   * BootStrap(AnnotationSet dataSet, long randSeed, int sampleSize). Constructor:
   *
   * @param dataSet
   * @param randSeed
   * @param sampleSize
   * @param numOfIterations
   * @return BootStrapper object
   *         ***********************************************************************
   */
  public ConceptMapBootstrapAnalyzer(ConceptMapBootstrapSampler dataSet, long randSeed,
      int sampleSize) {
    setAllData(dataSet);
    this.myMap = dataSet.getAllAnnotationsAsMap();
    this.seed = randSeed;
    this.rd = new Random(this.seed);
    if (sampleSize == 0) {
      this.defaultSampleSize = dataSet.getSize();
    }
    this.GrpColVals = dataSet.getUniqueGrpVals();
    this.numOfDocuments = dataSet.getUniqueDocumentCount();
    this.numOfPatients = dataSet.getUniquePatientCount();
    this.numOfGrByVals = dataSet.getUniqueGpCount();
    this.allGrpColLists = dataSet.getGroupByColumnList();
    System.out.println("numOfPatients= " + this.numOfPatients);
    System.out.println("numOfDocuments= " + this.numOfDocuments);
    System.out.println("numOfGrpColVals= " + this.numOfGrByVals);
  }


  public ConceptMapBootstrapSampler getAllData() {
    return this.allData;
  }

  /*
   * public AnnotationSet getRandomSet(int colNum) Returns a Randomized sample without grouping or
   * grouped by either PatientID or Document Number
   *
   * @param colNum
   */


  /*
   * @return List<String[]>public List<String[]> getAnnotationData.Returns all the data read from
   * the CSV file as a list of Strings.****
   */
  public List<String[]> getAnnotationData() {
    return getAllData().getAnnotationData();
  }


  public List<String[]> getRandomSet(int colNum) {
    int rdIndex = 0;
    try {
      /* Random set from base set without any grouping */
      if (colNum == 0) {
        int length = this.myMap.size();
        int j = 0;
        String str = "";
        String[] keys = new String[length];
        this.result.clear();
        Set<String> s = this.myMap.keySet();
        Iterator<String> itr = s.iterator();
        while (itr.hasNext()) {
          str = itr.next();
          keys[j] = str;
          j++;
        } // end of while
        for (int i = 0; i < length; i++) {
          rdIndex = this.rd.nextInt(length);
          this.result.add(this.myMap.get(keys[rdIndex]));
        }

      } // end of if loop

      /**
       * Random set sampled from base set- grouped by column values starting from 1
       */
      else if (colNum > 0) {

        this.result.clear();
        for (int i = 0; i < this.numOfGrByVals * 1; i++) {
          rdIndex = this.rd.nextInt(this.numOfGrByVals);
          this.result.addAll(this.allGrpColLists.get(this.GrpColVals[rdIndex]));
        }
      }

    } // End of try
    catch (Exception e) {
      e.printStackTrace();
    }
    return this.result;
  }


  /**
   * This print method directly takes in a List object containing data from an AnnotationSet.
   */
  public void printRandomSet(List<String[]> data) {
    String tmpstr = "";
    Iterator<String[]> it = data.iterator();
    while (it.hasNext()) {
      String[] temp = it.next();

      for (int i = 0; i < temp.length; i++) {
        tmpstr = tmpstr + " " + temp[i];
      }
      System.out.println(tmpstr);
      tmpstr = "";
    }
  }


  public void setAllData(ConceptMapBootstrapSampler allData) {
    this.allData = allData;
  }
}
