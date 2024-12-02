package src.main.gov.va.vha09.grecc.raptat.gg.core.deprecated;

import static src.main.gov.va.vha09.grecc.raptat.gg.core.Constants.ANNOTATE_WINDOW_MIN_HEIGHT;
import static src.main.gov.va.vha09.grecc.raptat.gg.core.Constants.TRAIN_WINDOW_MIN_WIDTH;
import static src.main.gov.va.vha09.grecc.raptat.gg.core.Constants.SEQUENCE_PROBABILITY_THRESHOLD_DEFAULT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.ContextType;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated.RaptatSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.solutions.RaptatAnnotationSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.Constants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.MenuResponseDispatcher;
import src.main.gov.va.vha09.grecc.raptat.gg.core.UserPreferences;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.RaptatPair;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.exporters.XMLExporter;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.WindowHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.csv.CSVReader;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.TextAnalyzer;
import third_party.org.chokkan.crfsuite.Tagger;
import cc.mallet.fst.CRF;

@Deprecated
public class AnnotationFinderWindow extends JDialog
{

	protected class GetSchemaFileListerner implements ActionListener
	{

		/**
		 * Create the Hash Set for the concepts.
		 *
		 * @param e
		 *            Action Event for the listener
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			JFileChooser fileChooser = new JFileChooser( OptionsManager.getInstance().getLastSelectedDir() );
			fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
			fileChooser.setFileFilter( new FileNameExtensionFilter( "Select Dictionary File", "dct" ) );

			if ( fileChooser.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION )
			{
				AnnotationFinderWindow.this.dictionaryFile = fileChooser.getSelectedFile();
				AnnotationFinderWindow.this.dictionaryFilePath.setText( AnnotationFinderWindow.this.dictionaryFile
						.getAbsolutePath() );

				OptionsManager.getInstance().setLastSelectedDir( fileChooser.getCurrentDirectory() );
			}
		}
	}

	private class AddConceptsListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			AnnotationFinderWindow.this.acceptedConceptsSet = new HashSet<String>();
			CSVReader conceptReader = new CSVReader( "Select text file with concepts to be analyzed" );
			if ( conceptReader.isValid() )
			{
				String[] nextDataSet;
				while (( nextDataSet = conceptReader.getNextData() ) != null)
				{
					AnnotationFinderWindow.this.acceptedConceptsSet.add( nextDataSet[0].toLowerCase() );
					AnnotationFinderWindow.this.conceptListModel.addElement( nextDataSet[0] );
				}
			}
		}
	}

	private class AddTextFileListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent ev)
		{
			JFileChooser fileChooser = new JFileChooser( "File Dialog" );
			if ( OptionsManager.getInstance().getLastSelectedDir() != null )
			{
				fileChooser.setCurrentDirectory( OptionsManager.getInstance().getLastSelectedDir() );
			}
			fileChooser.setMultiSelectionEnabled( true );
			fileChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
			FileNameExtensionFilter extFilter = new FileNameExtensionFilter( "Text files only", "txt" );
			fileChooser.setFileFilter( extFilter );
			int returnVal = fileChooser.showOpenDialog( null );
			File[] dirFiles;

			if ( returnVal == JFileChooser.APPROVE_OPTION )
			{
				File[] selectedFiles = fileChooser.getSelectedFiles();
				OptionsManager.getInstance().setLastSelectedDir( fileChooser.getCurrentDirectory() );
				AnnotationFinderWindow.this.docListModel.clear();
				AnnotationFinderWindow.this.textFilesList.clear();

				for (File selectedFile : selectedFiles)
				{
					if ( WindowHelper.validFile( selectedFile ) )
					{
						if ( !AnnotationFinderWindow.this.docListModel.contains( selectedFile.getName() ) )
						{
							AnnotationFinderWindow.this.docListModel.addElement( selectedFile.getName() );
							AnnotationFinderWindow.this.textFilesList.add( selectedFile.getAbsolutePath() );
						}
						else
						{
							String message = "\"File already exists in the list!\n";

							JOptionPane.showMessageDialog( new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE );
						}
					}
					else if ( selectedFile.isDirectory() )
					{
						dirFiles = selectedFile.listFiles();

						for (File dirFile : dirFiles)
						{
							if ( WindowHelper.validFile( dirFile ) )
							{
								if ( !AnnotationFinderWindow.this.docListModel.contains( selectedFile.getName() ) )
								{
									AnnotationFinderWindow.this.docListModel.addElement( dirFile.getName() );
									AnnotationFinderWindow.this.textFilesList.add( dirFile.getAbsolutePath() );
								}
								else
								{
									String message = "\"File already exists in the list!\n";
									JOptionPane.showMessageDialog( new JFrame(), message, "Error",
											JOptionPane.ERROR_MESSAGE );
								}
							}
						}
					}
				}
			}
		}
	}

	private class AnnotateButtonListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			double threshold;
			boolean thresholdInRange = true;

			try
			{
				threshold = Double.parseDouble( AnnotationFinderWindow.this.thresholdField.getText() );
				if ( ( threshold < 0.0 ) || ( threshold > 1.0 ) )
				{
					throw new NumberFormatException();
				}

			}
			catch (NumberFormatException e1)
			{
				thresholdInRange = false;
				System.err.println( e1.getMessage() );
				GeneralHelper.errorWriter( "<html><b>Threshold Input Error:</b><br>"
						+ "Please use a fraction between 0.0 and 1.0<html>" );
				GeneralHelper.errorWriter( "Threshold reset to default value of " + SEQUENCE_PROBABILITY_THRESHOLD );
				AnnotationFinderWindow.this.thresholdField.setText( Double.toString( SEQUENCE_PROBABILITY_THRESHOLD ) );
			}
			if ( thresholdInRange )
			{
				/*
				 * modified by Sanjib Saha on Dec 4, 2014 to include the new
				 * /class RaptatSolution instead of previous
				 * ProbabilisticSolution class
				 */

				RaptatPair<RaptatAnnotationSolution, String> solutionAndDirectory = new MenuResponseDispatcher()
						.loadSolution();

				File exportLocation;
				if ( ( solutionAndDirectory != null )
						&& ( ( exportLocation = GeneralHelper.getDirectory( "Choose directory"
								+ " for exporting results to XML" ) ) != null ) )
				{

					RaptatSolution theSolution = solutionAndDirectory.left;

					/* The solution file contains only probabilistic solution */
					if ( theSolution.getSolutionType() == Constants.solutionType.PROBABILISTIC )
					{
						String wrieAnnotationsDirectory = solutionAndDirectory.right;
						this.annotateProbabilisticModel( theSolution, wrieAnnotationsDirectory, exportLocation );
					}

					/* The solution file contains only CRF solution */
					else if ( theSolution.getSolutionType() == Constants.solutionType.CRF )
					{
						this.annotateCRFModel( theSolution, exportLocation );
					}

					setVisible( false );
					dispose();
				}
			}
		}


		/**
		 * Private method that takes a list of text and xml paths and does the
		 * processing necessary to conform the xml and text and generate a list
		 * of annotation training groups where each group corresponds to a
		 * document and the annotations associated with that document
		 *
		 * @param textPaths
		 * @param xmlPaths
		 * @param acceptedConcepts
		 * @param saveUnlabeledToDisk
		 * @return List<AnnotationTrainingGroup>
		 *
		 * @author Glenn Gobbel - Oct 3, 2012
		 */
		public List<AnnotationGroup> buildAnnotationGroupList(List<String> textPaths, TokenProcessingOptions theOptions)
		{
			TextAnalyzer theAnalyzer = new TextAnalyzer();
			theAnalyzer.setProcessingParameters( theOptions );
			List<AnnotationGroup> trainingGroups = new ArrayList<AnnotationGroup>( textPaths.size() );
			int docNumber = 1;
			int corpusSize = textPaths.size();
			for (String curTextPath : textPaths)
			{
				System.out.println( "Processing document " + docNumber++ + " of " + corpusSize );
				RaptatDocument theDocument = theAnalyzer.processDocument( curTextPath );
				trainingGroups.add( new AnnotationGroup( theDocument, null ) );
			}
			// theAnalyzer.destroy();

			return trainingGroups;
		}


		/**
		 * Annotate using CRF model
		 *
		 * @param theSolution
		 * @param solutionAndDirectory
		 * @param exportLocation
		 */
		private void annotateCRFModel(RaptatSolution theSolution, File exportLocation)
		{
			CRF crf = theSolution.getCrfSolution();
			Tagger tagger;
			tagger = new Tagger( crf );
			TextAnalyzer ta = new TextAnalyzer();

			try
			{
				setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
				// Stopwatch overallTimer = new Stopwatch();
				// overallTimer.tic("Starting annotation process");
				RaptatDocument raptatDocument;
				List<RaptatToken> docTokens;
				List<AnnotatedPhrase> ap;
				AnnotationGroup ag;

				for (String file : AnnotationFinderWindow.this.textFilesList)
				{
					// overallTimer.markTime("Starting document annotation");

					raptatDocument = ta.processDocument( file );
					docTokens = raptatDocument.regenerateProcessedTokens( false, false, true, false, null );
					ap = tagger.tagUsingRaptatTokens( docTokens );

					ag = new AnnotationGroup( raptatDocument, ap );
					ag.setRaptatAnnotations( ap );

					exportAnnotationsToXML( ag, exportLocation );
				}
			}
			catch (Exception e2)
			{
				System.out.println( "Unable to complete annotation: " + e2.getMessage() );
				GeneralHelper.errorWriter( "Error - unable to complete annotation" );
			}
			finally
			{
				setCursor( Cursor.getDefaultCursor() );
			}
		}


		private void annotateProbabilisticModel(RaptatSolution theSolution, String writeAnnotationsDirectory,
				File exportAnnotationsFile)
		{
			ProbabilisticTSFinderSolution theProbabilisticSolution = theSolution.getProbabilisticSolution();
			AnnotationFinder annotator = new AnnotationFinder( theProbabilisticSolution, writeAnnotationsDirectory );
			annotator.setSequenceThreshold( AnnotationFinderWindow.this.threshold );
			TokenProcessingOptions tokenProcessingOptions = theProbabilisticSolution.getTokenTrainingOptions();

			setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

			for (int i = 0; i < AnnotationFinderWindow.this.textFilesList.size(); i++ )
			{
				List<AnnotationGroup> groupsForAnnotation = buildAnnotationGroupList(
						AnnotationFinderWindow.this.textFilesList.subList( i, i + 1 ), tokenProcessingOptions );
				annotator.identifyPhrasesAndAnnotations( groupsForAnnotation,
						AnnotationFinderWindow.this.acceptedConceptsSet, false );
				exportAnnotationsToXML( groupsForAnnotation, exportAnnotationsFile );
			}
		}


		private void exportAnnotationsToXML(AnnotationGroup annotationGroups, File exportLocation)
		{

			exportLocation.setWritable( true );

			if ( ( annotationGroups.raptatAnnotations != null ) && ( annotationGroups.raptatAnnotations.size() > 0 ) )
			{
				XMLExporter.exportAnnotationGroup( annotationGroups, exportLocation );
			}
			else
			{
				System.out.println( "No annotations for export" );
			}

		}


		/**
		 * @param annotationGroups
		 * @param exportLocation
		 */
		private void exportAnnotationsToXML(List<AnnotationGroup> annotationGroups, File exportLocation)
		{

			exportLocation.setWritable( true );
			System.out.println( "Exporting Raptat Annotations To:" + exportLocation.getAbsolutePath() );

			for (AnnotationGroup curGroup : annotationGroups)
			{
				if ( ( curGroup.raptatAnnotations != null ) && ( curGroup.raptatAnnotations.size() > 0 ) )
				{
					XMLExporter.exportAnnotationGroup( curGroup, exportLocation );
				}
				else
				{
					System.out.println( "No annotations for export" );
				}
			}
		}
	}

	private class RemoveConceptsListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (int i = AnnotationFinderWindow.this.selectedConceptIndices.length - 1; i >= 0; i-- )
			{
				AnnotationFinderWindow.this.acceptedConceptsSet.remove( AnnotationFinderWindow.this.conceptListModel
						.elementAt( AnnotationFinderWindow.this.selectedConceptIndices[i] ).toString() );
				AnnotationFinderWindow.this.conceptListModel
						.removeElementAt( AnnotationFinderWindow.this.selectedConceptIndices[i] );
			}
		}
	}

	private class RemoveTextFileListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			for (int i = AnnotationFinderWindow.this.textSrcselections.length - 1; i >= 0; i-- )
			{
				AnnotationFinderWindow.this.textFilesList.remove( AnnotationFinderWindow.this.textSrcselections[i] );
				AnnotationFinderWindow.this.docListModel
						.removeElementAt( AnnotationFinderWindow.this.textSrcselections[i] );
			}
		}
	}

	private static final long serialVersionUID = 35601560859006230L;

	public static AnnotationFinderWindow annotationFinderWindowInstance = null;

	private final DefaultListModel<String> docListModel;
	private final List<String> textFilesList = new Vector<String>();
	private final DefaultListModel<String> conceptListModel;
	private JList<String> textList;
	private JList<String> conceptList;

	private JList<?> textSrclist;

	private int textSrcselections[], selectedConceptIndices[];

	private HashSet<String> acceptedConceptsSet;

	private final JTextField thresholdField = new JTextField( 10 );

	private final double threshold = SEQUENCE_PROBABILITY_THRESHOLD;

	private File dictionaryFile = null;

	private JTextField dictionaryFilePath;


	public AnnotationFinderWindow()
	{
		this.docListModel = new DefaultListModel<String>();
		this.conceptListModel = new DefaultListModel<String>();
	}


	/**
	 * ********************************************
	 *
	 * @param thePosition
	 * @param theDimension
	 *
	 * @author Glenn Gobbel - Jun 12, 2012
	 *********************************************
	 */
	public AnnotationFinderWindow(Point thePosition, Dimension theDimension)
	{
		this.docListModel = new DefaultListModel<String>();
		this.conceptListModel = new DefaultListModel<String>();
		initialize( thePosition, theDimension );
	}


	/**
	 * Annotate using Probabilistic model. This is to test merging more than one
	 * analysis.. Specially detect negative Annotated Phrases.. Need to merge
	 * the solution with the main solution
	 *
	 * @param theSolution
	 * @param solutionAndDirectory
	 * @param exportLocation
	 */
	public void annotateUsingMultipleSolutions(RaptatSolution theSolution,
			RaptatPair<RaptatSolution, String> solutionAndDirectory, File exportLocation)
	{

		AnnotateButtonListener a = new AnnotateButtonListener();

		ProbabilisticTSFinderSolution theProbabilisticSolution = theSolution.getProbabilisticSolution();
		CRF theCRFSolution = theSolution.getCrfSolution();

		AnnotationFinder annotator = new AnnotationFinder( theProbabilisticSolution, solutionAndDirectory.right );

		Tagger tagger;
		tagger = new Tagger( theCRFSolution );

		List<RaptatToken> docTokens;

		annotator.setSequenceThreshold( this.threshold );
		TokenProcessingOptions theOptions = theProbabilisticSolution.getTokenTrainingOptions();

		String contextValue;

		try
		{
			setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

			for (int i = 0; i < this.textFilesList.size(); i++ )
			{
				List<AnnotationGroup> groupsForAnnotation = a.buildAnnotationGroupList(
						this.textFilesList.subList( i, i + 1 ), theOptions );
				annotator.identifyPhrasesAndAnnotations( groupsForAnnotation, this.acceptedConceptsSet, false );

				// a.exportAnnotationsToXML(groupsForAnnotation, new File
				// ("C:\\Sanjib"));
				for (AnnotationGroup agroup : groupsForAnnotation)
				{
					List<AnnotatedPhrase> raptat = agroup.raptatAnnotations;

					for (AnnotatedPhrase ra : raptat)
					{
						List<RaptatToken> tokens = ra.getProcessedTokens();

						docTokens = tokens.get( 0 ).getSentenceOfOrigin().getProcessedTokens();
						tagger.tagUsingRaptatTokens( docTokens );

						for (RaptatToken t : tokens)
						{
							// Checking the context value - Negative only
							contextValue = t.getContextValue( ContextType.NEGATION );

							if ( !contextValue.equalsIgnoreCase( "null" ) )
							{
								if ( contextValue.equals( "negative" ) )
								{
									System.out.println( "Negated concept: " + ra.getConceptName() );
									System.out.println( "Phrase: " + t.getTokenStringPreprocessed() );
									break;
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e2)
		{
			System.out.println( "Unable to complete annotation: " + e2.getMessage() );
			GeneralHelper.errorWriter( "Error - unable to complete annotation" );
		}
		finally
		{
			setCursor( Cursor.getDefaultCursor() );
		}
	}


	private JPanel buildConceptListPanel(Dimension panelSize)
	{
		JScrollPane scrollPane = new JScrollPane();

		this.conceptList = new JList<String>( this.conceptListModel );
		scrollPane.setViewportView( this.conceptList );
		this.conceptList.setBorder( new TitledBorder( new EtchedBorder( EtchedBorder.RAISED, null, null ),
				"Concepts for Annotation", TitledBorder.LEADING, TitledBorder.TOP, null, new Color( 0, 0, 0 ) ) );

		ListSelectionListener conceptSelectionListener = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent listSelectionEvent)
			{
				if ( !listSelectionEvent.getValueIsAdjusting() )
				{
					JList<?> conceptListSource = (JList<?>) listSelectionEvent.getSource();
					AnnotationFinderWindow.this.selectedConceptIndices = conceptListSource.getSelectedIndices();
				}
			}

		};

		this.conceptList.addListSelectionListener( conceptSelectionListener );
		scrollPane.setBorder( BorderFactory.createLoweredBevelBorder() );
		JPanel thePanel = new JPanel();
		scrollPane.setPreferredSize( panelSize );
		thePanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		thePanel.add( scrollPane );
		return thePanel;
	}


	private JPanel buildTextListPanel(Dimension panelSize)
	{
		JScrollPane scrollPane = new JScrollPane();

		this.textList = new JList<String>( this.docListModel );
		this.textList.setBorder( new TitledBorder( null, "Text Files", TitledBorder.LEADING, TitledBorder.TOP, null,
				null ) );
		scrollPane.setViewportView( this.textList );

		ListSelectionListener ListSelLis = new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent listSelectionEvent)
			{
				if ( !listSelectionEvent.getValueIsAdjusting() )
				{
					AnnotationFinderWindow.this.textSrclist = (JList<?>) listSelectionEvent.getSource();
					AnnotationFinderWindow.this.textSrcselections = AnnotationFinderWindow.this.textSrclist
							.getSelectedIndices();
				}
			}

		};
		this.textList.addListSelectionListener( ListSelLis );
		scrollPane.setBorder( BorderFactory.createLoweredBevelBorder() );
		scrollPane.setPreferredSize( panelSize );
		JPanel thePanel = new JPanel();
		thePanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		thePanel.add( scrollPane );
		return thePanel;
	}


	/**
	 * ***********************************************************
	 *
	 * @param addTextFileListener
	 * @param removeTextFileListener
	 * @return
	 *
	 * @author Glenn Gobbel - Jun 12, 2012 modified by Sanjib Saha - July 24,
	 *         2014
	 * @param chooseButtonString
	 ************************************************************
	 */
	private Component getChooseRemoveButtonPanel(ActionListener addFileListener, ActionListener removeFileListener,
			Dimension panelSize)
	{
		// JButton chooseButton = new JButton( "Choose " + chooseButtonString );
		JButton chooseButton = new JButton( "Choose " );
		chooseButton.addActionListener( addFileListener );

		// JButton removeButton = new JButton( "Remove " + chooseButtonString);
		JButton removeButton = new JButton( "Remove " );
		removeButton.addActionListener( removeFileListener );
		// removeButton.setPreferredSize( chooseButton.getPreferredSize() );

		// JPanel theButtonPanel = new JPanel( new FlowLayout() );
		// theButtonPanel.add( chooseButton );
		// theButtonPanel.add( removeButton );
		// Buttons are aligned with the border
		JPanel theButtonPanel = new JPanel( new GridBagLayout() );
		theButtonPanel.setPreferredSize( panelSize );

		GridBagConstraints c = new GridBagConstraints();

		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		theButtonPanel.add( chooseButton, c );

		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_END;
		theButtonPanel.add( removeButton, c );

		return theButtonPanel;
	}


	/**
	 *
	 * @return
	 *
	 * @author Glenn Gobbel - Jun 12, 2012
	 */
	private Component getRunPanel()
	{
		Box runPanel = Box.createVerticalBox();
		runPanel.add( Box.createRigidArea( new Dimension( 0, 10 ) ) );
		JPanel buttonPanel = new JPanel( new FlowLayout() );
		JButton runButton = new JButton( "Annotate" );
		runButton.addActionListener( new AnnotateButtonListener() );
		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				setVisible( false );
				dispose();
			}
		} );
		cancelButton.setPreferredSize( runButton.getPreferredSize() );
		buttonPanel.add( runButton );
		buttonPanel.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
		buttonPanel.add( cancelButton );
		runPanel.add( buttonPanel );
		return runPanel;
	}


	private JPanel getSchemaPanel()
	{
		JPanel getSchemaPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 20, 5 ) );
		JButton acceptableConceptsButton = new JButton( "<html><center>Select<br>Dictionary File</center></html>" );
		acceptableConceptsButton.addActionListener( new GetSchemaFileListerner() );
		getSchemaPanel.add( acceptableConceptsButton );

		this.dictionaryFilePath = new JTextField( 40 );
		this.dictionaryFilePath.setEditable( false );
		this.dictionaryFilePath.setText( "Dictionary file path . . . " );
		this.dictionaryFilePath.setHorizontalAlignment( SwingConstants.LEFT );
		getSchemaPanel.add( this.dictionaryFilePath );

		return getSchemaPanel;
	}


	private void initialize(Point thePosition, Dimension theDimension)
	{
		int windowWidth = (int) ( theDimension.width * 1.0 );
		int windowHeight = (int) ( theDimension.height * 0.5 );
		windowWidth = ( windowWidth < TRAIN_WINDOW_MIN_WIDTH ) ? TRAIN_WINDOW_MIN_WIDTH : windowWidth;
		windowHeight = ( windowHeight < ANNOTATE_WINDOW_MIN_HEIGHT ) ? ANNOTATE_WINDOW_MIN_HEIGHT : windowHeight;

		setSize( windowWidth, windowHeight );
		setTitle( "Annotate Documents Window" );
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		setResizable( false );
		thePosition.translate( 40, 40 ); // Move option window slightly down and
		setLocation( thePosition );

		Box listPanel = Box.createHorizontalBox();
		listPanel.setPreferredSize( new Dimension( windowWidth, (int) ( ( windowHeight * 5.0 ) / 8 ) ) );

		JPanel generalButtonPanel = new JPanel( new BorderLayout() );
		JPanel listButtonPanel = new JPanel( new GridLayout( 1, 2 ) );
		JPanel optionsRunPanel = new JPanel( new GridLayout( 1, 2 ) );

		Container mainWindowContainer = getContentPane();
		mainWindowContainer.setLayout( new BorderLayout() );
		mainWindowContainer.add( listPanel, BorderLayout.NORTH );

		listPanel.add( buildTextListPanel( new Dimension( (int) ( 0.45 * listPanel.getPreferredSize().width ),
				listPanel.getPreferredSize().height ) ) );
		listPanel.add( buildConceptListPanel( new Dimension( (int) ( 0.45 * listPanel.getPreferredSize().width ),
				listPanel.getPreferredSize().height ) ) );

		mainWindowContainer.add( generalButtonPanel, BorderLayout.SOUTH );
		generalButtonPanel.add( listButtonPanel, BorderLayout.NORTH );

		// Modified by Sanjib Saha - July 24, 2014
		// Buttons are aligned with the text area
		Dimension panelSize = new Dimension( (int) ( 0.45 * listPanel.getPreferredSize().width ),
				(int) ( 0.10 * listPanel.getPreferredSize().height ) );

		JPanel textButtonsPanel = new JPanel( new FlowLayout() );
		textButtonsPanel.add( getChooseRemoveButtonPanel( new AddTextFileListener(), new RemoveTextFileListener(),
				panelSize ) );
		listButtonPanel.add( textButtonsPanel );

		JPanel xmlButtonsPanel = new JPanel( new FlowLayout() );
		xmlButtonsPanel.add( getChooseRemoveButtonPanel( new AddConceptsListener(), new RemoveConceptsListener(),
				panelSize ) );
		listButtonPanel.add( xmlButtonsPanel );

		JPanel thresholdFieldPanel = new JPanel();
		Border b3 = BorderFactory.createTitledBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ),
				"Phase Identification Threshold (0.0 to 1.0)", TitledBorder.CENTER, TitledBorder.BELOW_TOP );
		thresholdFieldPanel.setBorder( b3 );
		this.thresholdField.setText( Double.toString( this.threshold ) );
		this.thresholdField.setEditable( true );
		thresholdFieldPanel.add( this.thresholdField );
		optionsRunPanel.add( thresholdFieldPanel, BorderLayout.CENTER );

		optionsRunPanel.add( getRunPanel() );
		optionsRunPanel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ),
				BorderFactory.createLineBorder( Color.GRAY ) ) );
		generalButtonPanel.add( Box.createRigidArea( new Dimension( 0, 40 ) ) );
		generalButtonPanel.add( getSchemaPanel(), BorderLayout.CENTER );
		generalButtonPanel.add( optionsRunPanel, BorderLayout.SOUTH );

		setVisible( true );
		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		pack();
	}


	public static synchronized AnnotationFinderWindow getInstance(Point thePoint, Dimension theDimension)
	{
		if ( annotationFinderWindowInstance == null )
		{
			annotationFinderWindowInstance = new AnnotationFinderWindow( thePoint, theDimension );
		}

		return annotationFinderWindowInstance;
	}


	public static void main(String[] args)
	{
		UserPreferences.INSTANCE.initializeLVGLocation();

		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				AnnotationFinderWindow.getInstance( new Point( 0, 0 ), new Dimension( 100, 500 ) );
			}
		} );
	}
}
