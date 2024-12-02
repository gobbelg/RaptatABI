/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package src.main.gov.va.vha09.grecc.raptat.gg.uima.cpe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceConfigurationException;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import src.main.gov.va.vha09.grecc.raptat.gg.uima.annotation_types.SourceDocumentInformation;

/**
 * A simple collection reader that reads documents from a directory in the filesystem. It can be
 * configured with the following parameters:
 *
 * <ul>
 * <li><code>InputDirectory</code> - path to directory containing files
 * <li><code>Encoding</code> (optional) - character encoding of the input files
 * <li><code>Language</code> (optional) - language of the input documents
 * </ul>
 */
public class FileSystemCollectionReader extends CollectionReader_ImplBase {
  /**
   * Name of configuration parameter that must be set to the path of a directory containing input
   * files.
   */
  public static final String PARAM_INPUTDIR = "InputDirectory";

  /**
   * Name of configuration parameter that contains the character encoding used by the input files.
   * If not specified, the default system encoding will be used.
   */
  public static final String PARAM_ENCODING = "Encoding";

  /**
   * Name of optional configuration parameter that contains the language of the documents in the
   * input directory. If specified this information will be added to the CAS.
   */
  public static final String PARAM_LANGUAGE = "Language";

  /**
   * Name of optional configuration parameter that indicates including the subdirectories
   * (recursively) of the current input directory.
   */
  public static final String PARAM_SUBDIR = "BrowseSubdirectories";

  private ArrayList<File> mFiles;

  private String mEncoding;

  private String mLanguage;

  private Boolean mRecursive;

  private int mCurrentIndex;


  /** @see org.apache.uima.collection.base_cpm.BaseCollectionReader#close() */
  @Override
  public void close() throws IOException {}


  /**
   * @see org.apache.uima.collection.CollectionReader#getNext(org.apache.uima.cas.CAS)
   */
  @Override
  public void getNext(CAS aCAS) throws IOException, CollectionException {
    JCas jcas;
    try {
      jcas = aCAS.getJCas();
    } catch (CASException e) {
      throw new CollectionException(e);
    }

    // open input stream to file
    File file = this.mFiles.get(this.mCurrentIndex++);
    String text = FileUtils.file2String(file, this.mEncoding);
    // put document in CAS
    jcas.setDocumentText(text);

    // set language if it was explicitly specified as a configuration
    // parameter
    if (this.mLanguage != null) {
      ((DocumentAnnotation) jcas.getDocumentAnnotationFs()).setLanguage(this.mLanguage);
    }

    // Also store location of source document in CAS. This information is
    // critical
    // if CAS Consumers will need to know where the original document
    // contents are located.
    // For example, the Semantic Search CAS Indexer writes this information
    // into the
    // search index that it creates, which allows applications that use the
    // search index to
    // locate the documents that satisfy their semantic queries.
    SourceDocumentInformation ocInfo = new SourceDocumentInformation(jcas);
    ocInfo.setUri(file.getAbsoluteFile().toURL().toString());
    ocInfo.setOffsetInSource(0);
    ocInfo.setDocumentSize((int) file.length());
    ocInfo.setLastSegment(this.mCurrentIndex == this.mFiles.size());
    ocInfo.addToIndexes();
  }


  /**
   * Gets the total number of documents that will be returned by this collection reader. This is not
   * part of the general collection reader interface.
   *
   * @return the number of documents in the collection
   */
  public int getNumberOfDocuments() {
    return this.mFiles.size();
  }


  /**
   * @see org.apache.uima.collection.base_cpm.BaseCollectionReader#getProgress()
   */
  @Override
  public Progress[] getProgress() {
    return new Progress[] {
        new ProgressImpl(this.mCurrentIndex, this.mFiles.size(), Progress.ENTITIES)};
  }


  /** @see org.apache.uima.collection.CollectionReader#hasNext() */
  @Override
  public boolean hasNext() {
    return this.mCurrentIndex < this.mFiles.size();
  }


  /** @see org.apache.uima.collection.CollectionReader_ImplBase#initialize() */
  @Override
  public void initialize() throws ResourceInitializationException {
    File directory = new File(((String) this.getConfigParameterValue(PARAM_INPUTDIR)).trim());
    this.mEncoding = (String) this.getConfigParameterValue(PARAM_ENCODING);
    this.mLanguage = (String) this.getConfigParameterValue(PARAM_LANGUAGE);
    this.mRecursive = (Boolean) this.getConfigParameterValue(PARAM_SUBDIR);
    if (null == this.mRecursive) { // could be null if not set, it is optional
      this.mRecursive = Boolean.FALSE;
    }
    this.mCurrentIndex = 0;

    // if input directory does not exist or is not a directory, throw
    // exception
    if (!directory.exists() || !directory.isDirectory()) {
      throw new ResourceInitializationException(ResourceConfigurationException.DIRECTORY_NOT_FOUND,
          new Object[] {PARAM_INPUTDIR, getMetaData().getName(), directory.getPath()});
    }

    // get list of files in the specified directory, and subdirectories if
    // the
    // parameter PARAM_SUBDIR is set to True
    this.mFiles = new ArrayList<>();
    addFilesFromDir(directory);
  }


  /**
   * This method adds files in the directory passed in as a parameter to mFiles. If mRecursive is
   * true, it will include all files in all subdirectories (recursively), as well.
   *
   * @param dir
   */
  private void addFilesFromDir(File dir) {
    File[] files = dir.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (!files[i].isDirectory()) {
        this.mFiles.add(files[i]);
      } else if (this.mRecursive) {
        addFilesFromDir(files[i]);
      }
    }
  }
}
