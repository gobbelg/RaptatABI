package src.main.gov.va.vha09.grecc.raptat.gg.analysis.bootstrap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.DataImportReader;

/**
 * ******************************************************
 *
 * @author Shrimalini Jayaramaraja, March 8th 2011 * The AnnotationSet class represents the entire
 *         data * read from the input file. * *
 *         ******************************************************
 */

/*
 * The AnnotationSet class represents the entire data read from the input file.
 */

public class ConceptMapBootstrapSampler {
  public String[] annotation;
  public DataImportReader myReader;
  public int arrayLength = 0;
  public List<String[]> annotationsList = new Vector<>();
  public List<String[]> recordsFromDoc = new Vector<>();
  public List<String[]> recordsFromPatient = new Vector<>();
  public List<String[]> recordsWithGrpCr;
  public HashMap<String, String[]> annotationsMap;
  public HashMap<String, String[]> groupingMap;
  public HashMap<String, String[]> patientsMap;
  public HashMap<String, String[]> documentsMap;
  public HashMap<String, List<String[]>> allDocLists;
  public HashMap<String, List<String[]>> allPatientsLists;
  public HashMap<String, List<String[]>> groupByColumnList;
  // This variable is set to create an annotationSet grouped by colNum
  // (Columns index starts at 0). Default=0 means No grouping.
  int grpColNum = 2; // permanently set to 2. This allows both grouping from

  // Column 2 as well as No grouping data sets to be
  // created.


  /*
   * public AnnotationSet(DataImportReader csvReader). Constructor: Creates the main AnnotationSet
   * object from all the data read from CSV.
   */
  public ConceptMapBootstrapSampler(DataImportReader csvReader) {
    // This value must be the same as the one used in the
    // Bootstrap/crossValidate classes when used in conjunction with them.
    // grpColNum=0;
    // grpColNum =Constants.GROUPBY_COLUMN_NUMBER; //group by column=1;
    // there are currently 2 grouping columns
    // grpColNum =Constants.GROUP_BY_COLUMN_DEFAULT; //No grouping=0

    if (csvReader.isValid()) {
      this.myReader = csvReader;
      this.annotation = this.myReader.getNextData();
      this.arrayLength = this.annotation.length;

      // reading data from CSV file line by line
      while ((this.annotation = this.myReader.getNextData()) != null
          && this.annotation.length != 0) {

        // creates a vector containing all the data
        this.annotationsList.add(this.annotation);
        this.annotation = this.myReader.getNextData();
      }
    }
    if (this.grpColNum > 0) {
      createGroupingMaps();
      createGrpByDataList();
      // createAllDocLists();
      // createAllPatientsLists();
    }
  }


  /*
   * public HashMap <String,String[]> getAllAnnotationsAsMap(). Returns all data AnnotationSet
   * created from CSV as a HashMap.
   */
  public HashMap<String, String[]> getAllAnnotationsAsMap() {
    return this.annotationsMap;
  }


  /*
   * public List<String[]> getAnnotationData(). returns all lines of data read from the CSV file
   */
  public List<String[]> getAnnotationData() {
    return this.annotationsList;
  }


  /*
   * Returns the allDocLists hashMap
   */
  public HashMap<String, List<String[]>> getDocLists() {
    return this.allDocLists;
  }


  /*
   * Returns the allGrouping Col Lists hashMap
   */
  public HashMap<String, List<String[]>> getGroupByColumnList() {
    return this.groupByColumnList;
  }


  /*
   * Returns the allPatientsLists hashMap
   */
  public HashMap<String, List<String[]>> getPatientsLists() {
    return this.allPatientsLists;
  }


  /*
   * public int getSize(). Returns the Size of the AnnotationSet
   */
  public int getSize() {
    return this.annotationsList.size();
  }


  /*
   * public String[] getUniqueDocIds(). Returns an array containing the Documents numbers used in
   * the data set
   */
  public String[] getUniqueDocIds() {
    String[] docIds = new String[this.documentsMap.size()];
    Set<String> s = this.documentsMap.keySet();
    Iterator<String> itr = s.iterator();
    int i = 0;
    while (itr.hasNext()) {
      docIds[i] = itr.next();
      i++;
    }
    return docIds;
  }


  /*
   * public int getUniqueDocumentCount(). Returns the number of documents used
   */
  public int getUniqueDocumentCount() {
    return this.documentsMap.size();
  }


  public int getUniqueGpCount() {
    return this.groupingMap.size();
  }


  /*
   * Extracts the unique values in the Grouping column
   *
   * @return String[] array containing the set of grouping column values
   */
  public String[] getUniqueGrpVals() {
    String[] GrpVals = new String[this.groupingMap.size()];
    Set<String> s = this.groupingMap.keySet();
    Iterator<String> itr = s.iterator();
    int i = 0;
    while (itr.hasNext()) {
      GrpVals[i] = itr.next();
      i++;
    }
    return GrpVals;
  }


  /*
   * public int getUniquePatientCount(). Returns the number of Patients whose records are used
   */
  public int getUniquePatientCount() {
    return this.patientsMap.size();
  }


  /*
   * public String[] getUniquePatientIds(). Returns an array containing the patientIDs used in the
   * data set
   */
  public String[] getUniquePatientIds() {
    String[] patientIds = new String[this.patientsMap.size()];
    Set<String> s = this.patientsMap.keySet();
    Iterator<String> itr = s.iterator();
    int i = 0;
    while (itr.hasNext()) {
      patientIds[i] = itr.next();
      i++;
    }
    return patientIds;
  }


  public void printAnnotationSet() {
    String tmpstr = "";
    Iterator<String[]> it = this.annotationsList.iterator();
    while (it.hasNext()) {
      String[] temp = it.next();

      for (int i = 0; i < temp.length; i++) {
        tmpstr = tmpstr + " " + temp[i];
      }
      System.out.println(tmpstr);
      tmpstr = "";
    }
  }


  public void setGrpCol(int colNum) {

    this.grpColNum = colNum;
  }


  /*
   * Creates the hashMaps containing the base set of data
   */
  private void createGroupingMaps() {
    this.annotationsMap = new HashMap<>(this.annotationsList.size());
    this.patientsMap = new HashMap<>(this.annotationsList.size());
    this.documentsMap = new HashMap<>(this.annotationsList.size());
    this.groupingMap = new HashMap<>(this.annotationsList.size());

    Iterator<String[]> it = this.annotationsList.iterator();
    int serialNum = 0;
    while (it.hasNext()) {
      String[] annotation = it.next();
      serialNum++;
      this.patientsMap.put(annotation[0], annotation);
      this.documentsMap.put(annotation[1], annotation);
      this.groupingMap.put(annotation[this.grpColNum - 1], annotation);
      this.annotationsMap.put(String.valueOf(serialNum), annotation);
    }
  }


  // /generalized method - Updated by Glenn Gobbel Aug 19, 2011
  private void createGrpByDataList() {
    String[] curAnnotation;
    List<String[]> curList;

    // Create HashMap to hold groups of data as lists of annotations
    // and set size of the lists to 2 * the average size of the list
    int numOfuniqueColVal = getUniqueGpCount();
    this.groupByColumnList = new HashMap<>(numOfuniqueColVal);
    int grpDataSize = (int) (2 * ((double) this.annotationsList.size() / numOfuniqueColVal));
    String[] colVals = getUniqueGrpVals();
    for (String curVal : colVals) {
      this.groupByColumnList.put(curVal, new Vector<String[]>(grpDataSize));
    }

    Iterator<String[]> annotationIterator = this.annotationsList.iterator();
    while (annotationIterator.hasNext()) {
      curAnnotation = annotationIterator.next();
      curList = this.groupByColumnList.get(curAnnotation[this.grpColNum - 1]);
      curList.add(curAnnotation);
    }
  }
}
