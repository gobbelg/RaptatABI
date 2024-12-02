package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.Well1024a;

import com.github.jcrfsuite.util.CrfSuiteLoader;
import com.google.common.base.Splitter;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.Stopwatch;
import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.StringList;

/**
 * ********************************************************** Test -
 *
 * @author Glenn Gobbel, Jan 12, 2011
 *         <p>
 *         *********************************************************
 */
public class Test {

	Vector<String> storeVector;

	private void changeVector(Vector<String> inputVector) {
		this.storeVector = inputVector;
		inputVector = new Vector<>(this.storeVector.size());
		inputVector.add("gamma");
		inputVector.add("delta");
	}

	private void testEscapeSequence() {
		BufferedOutputStream os;
		final String testFile = "D:\\CARTCL-IIR\\ActiveLearning\\AKIAssertionTesting\\"
				+ "NegexCRFTrainingGG_\\escapeTest_v01.txt";

		try {
			os = new BufferedOutputStream(
					new FileOutputStream(testFile, false));
			final PrintWriter pw = new PrintWriter(os, true);
			final StringBuilder sb = new StringBuilder(":abcde:abc:");
			pw.println(sb.toString());
			String testString = sb.toString();
			testString = testString.replaceAll(":", "\\\\:");
			pw.println(testString);

			testString = "\\abcde\\abc\\";
			pw.println(testString);
			testString = testString.replaceAll("\\\\", "\\\\\\\\");
			pw.println(testString);

			pw.println("\n---------\nFinished\n---------");
			pw.close();
		}
		catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void testSimple() {
		HashSet<String> mySet = new HashSet<String>();
	}

	private void testSplit() {
		final Stopwatch sw = new Stopwatch();
		final long iterations = 10000000;
		String delimiter;
		String testString;
		Pattern delimiterPattern;
		final StringTokenizer stringTokenizer;

		delimiter = "|";
		{
			/*
			 * Code to test speed of String.split() method using short string as
			 * delimiter
			 */
			{
			}
		}

		final String[] delimiters = new String[] {
				"D_L@M#T", "D_L_M_T"
		};
		for (int m = 0; m < delimiters.length; m++) {
			delimiter = delimiters[m];
			/*
			 * Code to test speed of String.split() method using long string as
			 * delimiter
			 */
			{
				testString = "alphabet" + delimiter + "partOfSpeech" + delimiter
						+ "TargetTag";
				sw.markTime("processing of '" + delimiter
						+ "'via String.split() method");
				for (long i = 0; i < iterations; i++) {
					final String[] testSplit = testString.split(delimiter);
					final int splits = testSplit.length;
				}
				sw.getSecondsSinceMark("processing of '" + delimiter
						+ "' via String.split() method");
			}

			/*
			 * Code to test speed of Guava Splitter on long delimiter
			 */
			{
				testString = "alphabet" + delimiter + "partOfSpeech" + delimiter
						+ "TargetTag";
				final Splitter splitter = Splitter.on(delimiter);
				sw.markTime("processing of '" + delimiter
						+ "'via splitter.split() method");
				final String[] resultString = new String[5];
				for (long i = 0; i < iterations; i++) {
					int j = 0;
					for (final String curString : splitter.split(testString)) {
						resultString[j++] = curString;
					}
					if ( j != 3 ) {
						System.out.println("Incorrect split number");
						System.exit(-1);
					}
				}
				sw.getSecondsSinceMark("processing of '" + delimiter
						+ "'  via splitter.split() method");
			}

			/*
			 * Code to test speed of Apache StringUtils.split() method using
			 * short delimiter
			 */
			{
				testString = "alphabet" + delimiter + "partOfSpeech" + delimiter
						+ "TargetTag";
				sw.markTime("processing of '" + delimiter
						+ "' via StringUtils.splitByWholeSeparator() method");
				for (long i = 0; i < iterations; i++) {
					final String[] testSplit = StringUtils
							.splitByWholeSeparator(testString, delimiter);
					final int splits = testSplit.length;
					if ( splits != 3 ) {
						System.out.println("Incorrect split number");
						System.exit(-1);
					}
				}
				sw.getSecondsSinceMark("processing of '" + delimiter
						+ "' via StringUtils.split() method");
			}

			/*
			 * Code to test speed of Regex Pattern splitter on long delimiter
			 */
			{
				delimiterPattern = Pattern.compile(delimiter);
				testString = "alphabet" + delimiter + "partOfSpeech" + delimiter
						+ "TargetTag";
				sw.markTime("processing of '" + delimiter
						+ "'via Pattern.split() method");
				for (long i = 0; i < iterations; i++) {
					final String[] testSplit = delimiterPattern
							.split(testString);
					final int splits = testSplit.length;
				}
				sw.getSecondsSinceMark("processing of '" + delimiter
						+ "' via Pattern.split() method");
			}

			/*
			 * Code to test speed of String.split() method using short string as
			 * delimiter
			 */
			{
				delimiter = "|";
				testString = "alphabet" + delimiter + "partOfSpeech" + delimiter
						+ "TargetTag";
				sw.markTime("processing of '" + delimiter
						+ "' via String.split() method");
				for (long i = 0; i < iterations; i++) {
					final String[] testSplit = testString.split("\\|");
					final int splits = testSplit.length;
					if ( splits != 3 ) {
						System.out.println("Incorrect split number");
						System.exit(-1);
					}
				}
				sw.getSecondsSinceMark("processing of '" + delimiter
						+ "' via String.split() method");
			}
		}
	}

	protected void fileSplit() {
		final File fileToRead = new File(
				"P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CVFeatures_160615_141634\\CVGroup_4\\CVGroup_TrainingFile_4.txt");

		final String pathToNewFile = fileToRead.getParent() + File.separator
				+ "ShortFile_" + GeneralHelper.getTimeStamp() + ".txt";
		try {
			final BufferedReader br = new BufferedReader(
					new FileReader(fileToRead));
			final BufferedWriter os = new BufferedWriter(
					new FileWriter(new File(pathToNewFile)));

			int linesRead = 0;
			final int maxLines = 200000;
			String readLine = null;
			while ( ((readLine = br.readLine()) != null)
					&& (linesRead++ < maxLines) ) {
				if ( (linesRead % 1000) == 0 ) {
					System.out.println(linesRead + ":" + readLine);
				}
				os.write(readLine + "\n");
				os.flush();
			}
			br.close();
			os.close();
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void findinFile(final String testString) {
		final File fileToRead = new File(
				"P:\\ORD_Garvin_201212024D\\Annotation\\Glenn\\Analysis\\CVFeatures_160615_141634\\CVGroup_1\\CVGroup_TestingFile_1.txt");
		try {
			final BufferedReader br = new BufferedReader(
					new FileReader(fileToRead));

			String readLine = null;
			long linesRead = 0;
			while ( (readLine = br.readLine()) != null ) {
				linesRead++;
				if ( (linesRead % 10000) == 0 ) {
					System.out.println("Lines read:" + linesRead);
				}

				if ( readLine.startsWith(testString) ) {
					System.out.println(linesRead + ":" + readLine);
				}
			}
			br.close();
		}
		catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/** */
	protected void mathText() {
		System.out.println(-10.0 > Double.NEGATIVE_INFINITY);
		final double x = -Double.MAX_VALUE;
		System.out.println("x is:" + Double.toString(x));
	}

	/** */
	protected void testCRFSuiteAttributes() {
		try {
			CrfSuiteLoader.load();
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}

		final List<Attribute> attributeListA = new ArrayList<>();
		final Item itemListAttributes = new Item();

		final Attribute testAttribute = new Attribute("startValue");
		itemListAttributes.add(testAttribute);
		attributeListA.add(
				itemListAttributes.get((int) itemListAttributes.size() - 1));

		String list = "AttributeListA";
		for (final Attribute curAttribute : attributeListA) {
			System.out.println(list + ", Attribute:" + curAttribute.getAttr());
		}

		list = "ItemAttributeList";
		for (int i = 0; i < itemListAttributes.size(); i++) {

			System.out.println(list + ", Attribute:"
					+ itemListAttributes.get(i).getAttr());
		}

		for (final Attribute curAttribute : attributeListA) {
			curAttribute.setAttr("endValue");
		}

		System.out.println("\n\nAfter changing value:\n");
		list = "AttributeListA";
		for (final Attribute curAttribute : attributeListA) {
			System.out.println(list + ", Attribute:" + curAttribute.getAttr());
		}

		list = "ItemAttributeList";
		for (int i = 0; i < itemListAttributes.size(); i++) {
			System.out.println(list + ", Attribute:"
					+ itemListAttributes.get(i).getAttr());
		}
	}

	/** */
	protected void testCRFSuiteStringLists() {
		try {
			CrfSuiteLoader.load();
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}

		final StringList stringListA = new StringList();

		final String testString = "startValue";
		stringListA.add(testString);

		String resultStringBefore = stringListA
				.get((int) stringListA.size() - 1);

		System.out.println("String before:" + resultStringBefore);

		resultStringBefore = "endValue";

		final String resultStringAfter = stringListA
				.get((int) stringListA.size() - 1);

		System.out.println("String after:" + resultStringAfter);
	}

	/** */
	protected void testListSetMethod() {

		final List<Integer> testArray = new ArrayList<>(10);
		// for (int i = 0; i < 10; i++ )
		// {
		// testArray.add( null );
		// }
		Collections.fill(testArray, null);
		testArray.set(0, 2);
		testArray.set(3, 5);
		System.out.println("Index 0:" + testArray.get(0));
		System.out.println("Index 3:" + testArray.get(3));
		System.out.println("Index 5:" + testArray.get(5));
	}

	/** */
	protected void testRandomSampling() {
		final RandomDataGenerator rdg = new RandomDataGenerator(
				new Well1024a());

		final List<Integer> intList = new ArrayList<>();
		final int maxInt = 20;
		for (int i = 1; i <= maxInt; i++) {
			intList.add(i);
		}
		final int sampleSize = 3;
		// Object[] curSample = new Object[sampleSize];
		// while (( curSample = rdg.nextSample( intList, sampleSize ) ).length >
		// 0)
		// {
		// StringBuilder sb = new StringBuilder( (Integer) curSample[0] );
		// for (int i = 1; i < curSample.length; i++ )
		// {
		// sb.append( " " ).append( curSample[i] );
		// }
		// System.out.println( sb.toString() );
		// curSample = new Object[sampleSize];
		// }

		Collections.shuffle(intList, new Random(System.currentTimeMillis()));
		java.util.Iterator<Integer> intListIterator = intList.iterator();
		int curIndex = 0;
		final List<List<Integer>> randomLists = new ArrayList<>();
		List<Integer> innerList = null;
		while ( (curIndex + sampleSize) < intList.size() ) {
			innerList = new ArrayList<>();
			for (int i = 0; i < sampleSize; i++, curIndex++) {
				innerList.add(intListIterator.next());
			}
			randomLists.add(innerList);
		}
		while ( intListIterator.hasNext() ) {
			innerList.add(intListIterator.next());
		}

		for (final List<Integer> curList : randomLists) {
			if ( curList.size() > 0 ) {
				intListIterator = curList.iterator();
				final StringBuilder sb = new StringBuilder(
						intListIterator.next().toString());
				while ( intListIterator.hasNext() ) {
					sb.append(" ").append(intListIterator.next());
				}
				System.out.println(sb.toString());
			}
		}
	}

	/** */
	protected void testSetIteration() {
		final Set<String> testHash = new HashSet<>();

		final Iterator<String> setIterator = testHash.iterator();

		if ( !setIterator.hasNext() ) {
			System.out.println("No next found");
		}
	}

	public static void main(final String[] args) {
		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				testInMemoryFile();
			}

			private void checkLoopLabel() {
				outerLoop: for (int i = 0; i < 10; i++) {
					System.out.println("\ni = " + i + "\n---------------");
					innerLoop: for (int j = 0; j < 20; j++) {
						if ( j == 10 ) {
							break innerLoop;
						}
						System.out.println("j = " + j);
					}
					System.out.println("-------------\ni = " + i);
				}
			}

			private void testInMemoryFile() {
				String testString = "A big dog went across the street.  He met a cat.";

				try (FileSystem inMemoryFileSystem = Jimfs
						.newFileSystem(Configuration.forCurrentPlatform())) {
					Path filePath = inMemoryFileSystem
							.getPath("C:\\example.txt");
					Files.write(filePath,
							testString.getBytes(StandardCharsets.UTF_8));

					// Read the content from the file
					String fileContent = new String(
							Files.readAllBytes(filePath),
							StandardCharsets.UTF_8);
					System.out.println("FileContent:\n\t" + fileContent);
				}
				catch (IOException e) {

					e.printStackTrace();
				}

			}

		});
	}
}
