package src.main.gov.va.vha09.grecc.raptat.gg.core.deprecated;

import static src.main.gov.va.vha09.grecc.raptat.gg.core.Constants.TRAIN_WINDOW_MIN_HEIGHT;
import static src.main.gov.va.vha09.grecc.raptat.gg.core.Constants.TRAIN_WINDOW_MIN_WIDTH;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;

import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated.RaptatSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated.TrainingProcessor;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.deprecated.UpdateTrainingProcessor;
import src.main.gov.va.vha09.grecc.raptat.gg.algorithms.probabilistic.sequenceidentification.probabilistic.ProbabilisticTSFinderSolution;
import src.main.gov.va.vha09.grecc.raptat.gg.core.Constants;
import src.main.gov.va.vha09.grecc.raptat.gg.core.dialogs.BaseClassWindows;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.SchemaSettingsOptionWindow;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.AnnotationGroup;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.PhraseIDTrainOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.SchemaConcept;
import src.main.gov.va.vha09.grecc.raptat.gg.datastructures.TokenProcessingOptions;
import src.main.gov.va.vha09.grecc.raptat.gg.helpers.GeneralHelper;
import src.main.gov.va.vha09.grecc.raptat.gg.importer.xml.XMLImporter;
import cc.mallet.fst.CRF;

/**
 * Creates the Update Solution window to update an existing solution or create a
 * new solution.
 *
 * @author Sanjib Saha, created on July 16, 2014
 */
@Deprecated
public class UpdateAnnotatorWindow extends BaseClassWindows
{

	/**
	 * Creates the action listener for cancel button. OVERRIDES PARENT METHOD
	 *
	 * @author Sanjib Saha, created on July 22, 2014
	 */
	private class CancelButtonListener implements ActionListener
	{

		/**
		 * Disposes the current window instance.
		 *
		 * @param arg0
		 *            Action Event for the listener
		 */
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			dispose();
			updateSolutionWindowInstance = null;
		}
	}

	/**
	 * Creates the action listener for Concept Mapping button. OVERRIDES PARENT
	 * METHOD
	 *
	 * @author Sanjib Saha, created on July 22, 2014
	 */
	private class ConceptMapOptionsListener implements ActionListener
	{

		/**
		 * Set concept map training options.
		 *
		 * @param e
		 *            Action Event for the listener
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			OptionsManager.getInstance().setConceptMapTrainOptions();
		}
	}

	/**
	 * Creates the action listener for Schema Path Settings button. OVERRIDES
	 * PARENT METHOD
	 *
	 * @author Sanjib Saha, created on March 12, 2015
	 */
	private class SchemaPathSettingsListener implements ActionListener
	{

		/**
		 * Set annotation training options.
		 *
		 * @param e
		 *            Action Event for the listener
		 */
		@Override
		@SuppressWarnings("ResultOfObjectAllocationIgnored")
		public void actionPerformed(ActionEvent e)
		{
			if ( UpdateAnnotatorWindow.this.schemaFilePath.getText().equals( "" ) )
			{
				System.err.println( "No file selected" );
			}

			new SchemaSettingsOptionWindow( new Point( 100, 100 ), new Dimension( 600, 800 ),
					UpdateAnnotatorWindow.this.schemaFilePath.getText(), UpdateAnnotatorWindow.this.strTable );
		}
	}

	/**
	 * Creates the action listener for Schema Path Settings button. OVERRIDES
	 * PARENT METHOD
	 *
	 * @author Sanjib Saha, created on March 12, 2015
	 */
	private class SchemaProcessListener implements ActionListener
	{

		/**
		 * Set annotation training options.
		 *
		 * @param e
		 *            Action Event for the listener
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{

			printTable();

		}
	}

	/**
	 * Creates the action listener for Sequence ID button. OVERRIDES PARENT
	 * METHOD
	 *
	 * @author Sanjib Saha, created on July 22, 2014
	 */
	private class SequenceIDOptionsListener implements ActionListener
	{

		/**
		 * Set annotation training options.
		 *
		 * @param e
		 *            Action Event for the listener
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			OptionsManager.getInstance().setAnnotationTrainOptions();
		}
	}

	/**
	 * Creates the action listener for Train RapTAT button. OVERRIDES PARENT
	 * METHOD
	 *
	 * @author Sanjib Saha, created on July 22, 2014
	 */
	private class TrainRaptatListener implements ActionListener
	{

		/**
		 * Train RapTAT with the supplied Reference XML and text corpus and
		 * creates a solution file .
		 *
		 * @param e
		 *            Action Event for the listener
		 */
		@Override
		public void actionPerformed(ActionEvent e)
		{
			List<SchemaConcept> schemaConceptList = XMLImporter.getSchemaConcepts( UpdateAnnotatorWindow.this,
					UpdateAnnotatorWindow.this.schemaFilePath.getText() );

			OptionsManager theOptions = OptionsManager.getInstance();
			JFileChooser saveLocChooser = new JFileChooser();
			saveLocChooser.setDialogTitle( "Enter name for solution file" );

			if ( theOptions.getLastSelectedDir() != null )
			{
				saveLocChooser.setCurrentDirectory( theOptions.getLastSelectedDir() );
			}

			int returnVal = saveLocChooser.showSaveDialog( null );

			if ( returnVal == JFileChooser.APPROVE_OPTION )
			{
				CRF crf;
				ProbabilisticTSFinderSolution probSoln;
				File solutionFile = saveLocChooser.getSelectedFile().getAbsoluteFile();
				solutionFile = correctFileName( solutionFile );
				RaptatSolution solution = new RaptatSolution();

				if ( solutionFile != null )
				{
					theOptions.setLastSelectedDir( saveLocChooser.getCurrentDirectory() );

					try
					{
						if ( solutionFile.createNewFile() )
						{
							logger.info( "Solution file created:" + solutionFile.getAbsolutePath() );
							solutionFile.setWritable( true );

							// CRF is selected for training method
							if ( UpdateAnnotatorWindow.this.crfRadioButton.isSelected() )
							{
								String tempTrainingFile = "data\\crfTrainingFile.dll";
								Trainer trainer = new Trainer();

								setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

								try
								{
									trainer.addTrainingData( UpdateAnnotatorWindow.this.textFilesPathList,
											UpdateAnnotatorWindow.this.conceptXMLFilesPathList, tempTrainingFile, true );
									crf = trainer.stochasticGradientTrainingFromBeginning( tempTrainingFile );
									solution.setCrfSolution( crf );
									solution.setSolutionType( Constants.solutionType.CRF );
								}
								catch (IOException ex)
								{
									System.out.println( ex );
								}
							}

							// Probabilistic method is selected for training
							else if ( UpdateAnnotatorWindow.this.probabilisticRadioButton.isSelected() )
							{
								TokenProcessingOptions tokenProcessingOptions = new TokenProcessingOptions( theOptions );
								PhraseIDTrainOptions phraseIDOptions = new PhraseIDTrainOptions( theOptions );
								setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

								/*
								 * Note that the textAnalyzer instance within
								 * trainProc has its processing options set by
								 * the TrainingProcessor constructor call
								 */
								TrainingProcessor trainProc = new TrainingProcessor( tokenProcessingOptions,
										phraseIDOptions, UpdateAnnotatorWindow.this.acceptedConceptsSet, solutionFile );

								/*
								 * This will process each document in the
								 * training list using the settings within
								 * tokenProcessingOptions for a TextAnalyzer
								 * instance and also import the annotations in
								 * the XML files.
								 */
								List<AnnotationGroup> trainingGroupList = trainProc.buildAnnotationTrainingList(
										UpdateAnnotatorWindow.this.textFilesPathList,
										UpdateAnnotatorWindow.this.conceptXMLFilesPathList, schemaConceptList );

								probSoln = trainProc.trainUsingProbabilisticMethod( trainingGroupList, true,
										schemaConceptList );
								solution.setProbabilisticSolution( probSoln );
								solution.setSolutionType( Constants.solutionType.PROBABILISTIC );
							}

							String message = "The Raptat Trained solution file has been saved to file";
							JOptionPane.showMessageDialog( new JFrame(), message, "Training Completed",
									JOptionPane.INFORMATION_MESSAGE );
						}
						else
						{
							String message1 = "A file with this name already exists in this folder. Please enter another name.";

							JOptionPane.showMessageDialog( new JFrame(), message1, "Error", JOptionPane.ERROR_MESSAGE );
						}
					}
					catch (Exception e2)
					{
						logger.info( "Unable to complete training: " + e2.getMessage() );
						GeneralHelper.errorWriter( "Error - unable to complete training" );
						e2.printStackTrace();
					}
				}
				else
				{
					GeneralHelper.errorWriter( "Solution file should have the form XXX.soln" );
				}

				// Writing the solution file
				try
				{
					ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream(
							solutionFile ), Constants.DEFAULT_BUFFERED_STREAM_SIZE ) );
					oos.writeObject( solution );
					oos.close();
				}
				catch (IOException ex)
				{
					System.err.println( "Exception writing file " + solutionFile + ": " + ex );
				}
			}

			setCursor( Cursor.getDefaultCursor() );

			updateSolutionWindowInstance.dispose();
			updateSolutionWindowInstance = null;
		}
	}

	/**
	 * Creates the action listener for update button. OVERRIDES PARENT METHOD
	 *
	 * @author Sanjib Saha, created on July 17, 2014
	 */
	private class UpdateSolutionListener implements ActionListener
	{

		/**
		 * Updates the selected solution file with the supplied Reference XML
		 * and text corpus.
		 *
		 * @param e
		 *            Action Event for the listener
		 */
		@Override
		@SuppressWarnings(
		{
				"CallToPrintStackTrace", "null", "ConvertToTryWithResources"
		})
		public void actionPerformed(ActionEvent e)
		{
			OptionsManager theOptions = OptionsManager.getInstance();

			JFileChooser solutionChooser = new JFileChooser();

			if ( OptionsManager.getInstance().getLastSelectedDir() != null )
			{
				solutionChooser.setCurrentDirectory( theOptions.getLastSelectedDir() );
			}

			solutionChooser.setMultiSelectionEnabled( false );
			solutionChooser.setFileSelectionMode( JFileChooser.FILES_AND_DIRECTORIES );
			solutionChooser.setFileFilter( new FileNameExtensionFilter( "Solution Files Only", "soln" ) );

			int returnValue = solutionChooser.showDialog( null, "Select" );

			if ( returnValue == JFileChooser.APPROVE_OPTION )
			{
				File solutionFileToUpdate = solutionChooser.getSelectedFile();
				theOptions.setLastSelectedDir( solutionChooser.getCurrentDirectory() );

				if ( solutionFileToUpdate.isDirectory() )
				{
					String message = "You have selected a directory.\n" + "Select a valid RapTAT solution file";
					JOptionPane.showMessageDialog( new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE );

					return;
				}
				// Given solution file name does not exist
				else if ( !solutionFileToUpdate.exists() )
				{
					String message = "No Raptat solution file exists with this name.";
					JOptionPane.showMessageDialog( new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE );

					return;
				}

				// If solutionFileToUpdate is null, this will create a new file
				// in the last directory
				File newSolutionFile = getNewSolutionFile( solutionFileToUpdate );

				if ( newSolutionFile != null )
				{
					newSolutionFile.setWritable( true );
					List<AnnotationGroup> trainingList;
					TrainingProcessor solutionUpdater;

					RaptatSolution solution = RaptatSolution.loadFromFile( solutionFileToUpdate );

					if ( solution.getSolutionType() == Constants.solutionType.PROBABILISTIC )
					{

						ProbabilisticTSFinderSolution solutionToUpdate = solution.getProbabilisticSolution();

						// Invalid solution file
						if ( solutionToUpdate == null )
						{
							String message = "The is not a valid Raptat solution file.";
							JOptionPane.showMessageDialog( new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE );

							newSolutionFile.delete();
							return;
						}

						solutionUpdater = new UpdateTrainingProcessor( newSolutionFile, solutionToUpdate,
								solutionFileToUpdate );

						trainingList = solutionUpdater.buildAnnotationTrainingList(
								UpdateAnnotatorWindow.this.textFilesPathList,
								UpdateAnnotatorWindow.this.conceptXMLFilesPathList );
						solutionToUpdate = solutionUpdater.updateTrainingWithProbabilisticMethod( trainingList,
								solutionToUpdate.getSchemaConceptList() );

						solution.setProbabilisticSolution( solutionToUpdate );
					}
					else if ( solution.getSolutionType() == Constants.solutionType.CRF )
					{

						CRF solutionToUpdate = solution.getCrfSolution();

						// Invalid solution file
						if ( solutionToUpdate == null )
						{
							String message = "The is not a valid Raptat solution file.";
							JOptionPane.showMessageDialog( new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE );

							newSolutionFile.delete();
							return;
						}

						String tempTrainingFile = "data\\crfTrainingFile.dll";
						Trainer trainer = new Trainer();

						setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

						try
						{
							trainer.addTrainingData( UpdateAnnotatorWindow.this.textFilesPathList,
									UpdateAnnotatorWindow.this.conceptXMLFilesPathList, tempTrainingFile, true );
							solutionToUpdate = trainer.stochasticGradientTrainingWithExistingModel( tempTrainingFile,
									solutionToUpdate );
							solution.setCrfSolution( solutionToUpdate );
							solution.setSolutionType( Constants.solutionType.CRF );
						}
						catch (IOException ex)
						{
							System.out.println( ex );
						}
						// solutionUpdater = new
						// SolutionUpdateProcessor(newSolutionFile,
						// solutionToUpdate, solutionFileToUpdate);

						// trainingList =
						// solutionUpdater.buildAnnotationTrainingList(textFilesPathList,
						// referenceXMLFilesPathList);
						// solutionToUpdate =
						// solutionUpdater.updateTrainingWithProbabilisticMethod(trainingList);

						solution.setCrfSolution( solutionToUpdate );
					}

					setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );

					// Writing the solution file
					try
					{
						ObjectOutputStream oos = new ObjectOutputStream( new BufferedOutputStream(
								new FileOutputStream( newSolutionFile ), Constants.DEFAULT_BUFFERED_STREAM_SIZE ) );
						oos.writeObject( solution );
						oos.close();
					}
					catch (IOException ex)
					{
						System.err.println( "Exception writing file " + newSolutionFile + ": " + ex );
					}

					String message = "The Raptat solution file has been updated";
					JOptionPane.showMessageDialog( new JFrame(), message, "Update process Completed",
							JOptionPane.INFORMATION_MESSAGE );
				}
			}
		}
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 3126112280239214848L;

	/**
	 * Instance of UpdateSolutionWindow Class
	 */
	public static UpdateAnnotatorWindow updateSolutionWindowInstance = null;

	/**
	 * The logger
	 */
	private static final Logger logger = Logger.getLogger( UpdateAnnotatorWindow.class );

	private final JRadioButton crfRadioButton = new JRadioButton( "CRF" );

	private final JRadioButton probabilisticRadioButton = new JRadioButton( "Probabilistic" );

	private final List<List<String>> strTable = new ArrayList<List<String>>();


	/**
	 * Constructor of UpdateSolutionWindow Class.
	 *
	 * @param thePoint
	 *            Upper-left coordinate of the window
	 * @param theDimension
	 *            Dimension of the window
	 *
	 * @author Sanjib Saha, July 17, 2014
	 */
	private UpdateAnnotatorWindow(Point thePoint, Dimension theDimension, String action)
	{
		this.conceptXMLFilesPathList = new ArrayList<String>();
		this.textFilesPathList = new ArrayList<String>();

		this.conceptXMLListModel = new DefaultListModel<String>();
		this.textFileListModel = new DefaultListModel<String>();

		initialize( thePoint, theDimension, action );
	}


	/**
	 * Includes timestamp with the user given file name. Removes any "_UPDATE_"
	 * string from the user given file name to avoid conflict during the update
	 * process file naming.
	 *
	 * @param candidateFile
	 *            The file given by the user
	 *
	 * @return File with timestamp included in it's name
	 *
	 * @author Sanjib Saha, July 22, 2014
	 */
	private File correctFileName(File candidateFile)
	{
		// Removing the file extension to include the timestamp in the file name
		String fileName = candidateFile.getName().replaceAll( "\\.soln", "" );

		// Removing any "_UPDATE_" string from file name to avoid confusion
		// in the updateSolution
		fileName = fileName.replaceAll( "_UPDATE_", "" );

		int dotIndex = fileName.indexOf( "." );

		if ( dotIndex > -1 )
		{
			fileName = fileName.substring( 0, dotIndex );
		}

		if ( fileName.length() < 1 )
		{
			return null;
		}

		String directoryName = candidateFile.getParentFile().getAbsolutePath();
		String timeStamp = new SimpleDateFormat( "yyyyMMddHHmmss" ).format( new Date() );

		return new File( directoryName + File.separator + fileName + "_" + timeStamp + ".soln" );
	}


	/**
	 * Creates a new solution file to update the existing file.
	 *
	 * @param solutionFileToUpdate
	 *            The existing solution file
	 *
	 * @return Temporary solution file
	 *
	 * @author Sanjib Saha, July 17, 2014
	 */
	private File getNewSolutionFile(File solutionFileToUpdate)
	{
		File updatedFileDirectory;
		String updatedFileName;

		if ( solutionFileToUpdate == null )
		{
			String message = "The is not a valid Raptat solution file.";
			JOptionPane.showMessageDialog( new JFrame(), message, "Error", JOptionPane.ERROR_MESSAGE );

			return null;
		}
		else
		{
			updatedFileDirectory = solutionFileToUpdate.getParentFile();
			updatedFileName = solutionFileToUpdate.getName();

			int updateNumber = 1; // Default update number
			int indexOfUpdateString = updatedFileName.indexOf( "_UPDATE_" );

			// File name already contains "_UPDATE_"
			if ( indexOfUpdateString > -1 )
			{
				int indexOfDot = updatedFileName.indexOf( "." );

				// Substring containing update number starts after "_UPDATE_"
				// (length 8)
				updateNumber = Integer.parseInt( updatedFileName.substring( indexOfUpdateString + 8, indexOfDot ) ) + 1;

				// updatedFileName retains only the part before the update
				// string and sequence no
				updatedFileName = updatedFileName.substring( 0, indexOfUpdateString );
			}

			updatedFileName = updatedFileName.replaceAll( "\\.soln", "" );
			NumberFormat formatter = new DecimalFormat( "0000" );

			updatedFileName += "_UPDATE_" + formatter.format( updateNumber ) + ".soln";
		}

		return new File( updatedFileDirectory, updatedFileName );
	}


	/**
	 * Creates the components for the option panel of the training window.
	 *
	 * @return A JPanel with the components for the option panel.
	 *
	 * @author Sanjib Saha, July 22, 2014
	 */
	private Component getOptionsPanel()
	{
		JPanel optionsPanel = new JPanel( new FlowLayout( FlowLayout.CENTER, 20, 5 ) );

		JButton conceptMapOptionsButton = new JButton( "<html><center>Concept<br>Mapping</center></html>" );
		optionsPanel.add( conceptMapOptionsButton );
		conceptMapOptionsButton.addActionListener( new ConceptMapOptionsListener() );

		JButton sequenceIDOptions = new JButton( "<html><center>Phrase<br>Identification</center></html>" );
		optionsPanel.add( sequenceIDOptions );
		sequenceIDOptions.addActionListener( new SequenceIDOptionsListener() );

		JButton acceptableConceptsButton = new JButton( "<html><center>Acceptable<br>Concepts</center></html>" );
		acceptableConceptsButton.setHorizontalTextPosition( SwingConstants.CENTER );
		optionsPanel.add( acceptableConceptsButton );
		acceptableConceptsButton.addActionListener( new AcceptableConceptsButtonListener() );

		Font borderFont = new Font( optionsPanel.getFont().getFontName(), Font.ITALIC, optionsPanel.getFont().getSize() );
		Border b3 = BorderFactory.createTitledBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ),
				"Option Settings", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, borderFont );
		optionsPanel.setBorder( b3 );

		return optionsPanel;
	}


	/**
	 * Creates the components for adding Schema on the training window.
	 *
	 * @return A JPanel with the components for the Schema addition panel.
	 *
	 * @author Sanjib Saha, March 12, 2015
	 */
	private JPanel getSchemaPanel()
	{
		JPanel getSchemaPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 20, 5 ) );

		JButton acceptableConceptsButton = new JButton( "<html><center>Select<br>Schema File</center></html>" );
		acceptableConceptsButton.addActionListener( new GetSchemaFileListerner() );
		getSchemaPanel.add( acceptableConceptsButton );

		this.schemaFilePath = new JTextField( 30 );
		this.schemaFilePath.setEditable( false );
		this.schemaFilePath.setText( "Schema file path." );
		this.schemaFilePath.setHorizontalAlignment( SwingConstants.LEFT );
		getSchemaPanel.add( this.schemaFilePath );

		JButton settingsButton = new JButton( "<html><center>Settings</center></html>" );
		settingsButton.addActionListener( new SchemaPathSettingsListener() );
		getSchemaPanel.add( settingsButton );

		JButton processButton = new JButton( "<html><center>Process</center></html>" );
		processButton.addActionListener( new SchemaProcessListener() );
		getSchemaPanel.add( processButton );

		return getSchemaPanel;
	}


	private JPanel getTrainingOptionPanel()
	{
		JPanel trainingPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 20, 5 ) );

		JLabel trainingMethod = new JLabel( "<html><center>Select<br>Training Method</center></html>" );
		trainingPanel.add( trainingMethod );

		ButtonGroup trainingSelection = new ButtonGroup();
		// JRadioButton crfRadioButton = new JRadioButton("CRF");
		// JRadioButton probabilisticRadioButton = new
		// JRadioButton("Probabilistic");
		trainingSelection.add( this.crfRadioButton );
		trainingSelection.add( this.probabilisticRadioButton );
		this.crfRadioButton.setSelected( true );

		trainingPanel.add( this.crfRadioButton );
		trainingPanel.add( this.probabilisticRadioButton );

		return trainingPanel;
	}


	/**
	 * Creates the components for the training panel.
	 *
	 * @return A JPanel with the components for the training panel.
	 *
	 * @author Sanjib Saha, July 22, 2014
	 */
	private Component getTrainingPanel()
	{
		Box runPanel = Box.createVerticalBox();
		runPanel.add( Box.createRigidArea( new Dimension( 0, 10 ) ) );

		JPanel buttonPanel = new JPanel( new FlowLayout() );
		JButton trainButton = new JButton( "Train RapTAT" );
		trainButton.addActionListener( new TrainRaptatListener() );

		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( new CancelButtonListener() );
		cancelButton.setPreferredSize( trainButton.getPreferredSize() );

		buttonPanel.add( trainButton );
		buttonPanel.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
		buttonPanel.add( cancelButton );

		runPanel.add( buttonPanel );

		return runPanel;
	}


	/**
	 * Creates the components for the update panel.
	 *
	 * @return A JPanel with the components for the update panel.
	 *
	 * @author Sanjib Saha, July 16, 2014
	 */
	private Component getUpdatePanel()
	{
		Box runPanel = Box.createVerticalBox();
		runPanel.add( Box.createRigidArea( new Dimension( 0, 10 ) ) );

		JPanel buttonPanel = new JPanel( new FlowLayout() );
		JButton updateButton = new JButton( "Update Solution" );
		updateButton.addActionListener( new UpdateSolutionListener() );

		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( new CancelButtonListener() );

		buttonPanel.add( updateButton );
		buttonPanel.add( cancelButton );
		buttonPanel.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );

		runPanel.add( buttonPanel );

		return runPanel;
	}


	/**
	 * Initializes the update window components and assigns corresponding action
	 * listeners to the components.
	 *
	 * @param thePoint
	 *            Upper-left coordinate of the window
	 * @param theDimension
	 *            Dimension of the window
	 * @param action
	 *            Action selected from the menu (Train/Update solution)
	 *
	 * @author Sanjib Saha, July 22, 2014
	 */
	private void initialize(Point thePosition, Dimension theDimension, String action)
	{
		int windowWidth = (int) ( theDimension.width * 1.0 );
		int windowHeight = (int) ( theDimension.height * 0.8 );

		windowWidth = ( windowWidth < TRAIN_WINDOW_MIN_WIDTH ) ? TRAIN_WINDOW_MIN_WIDTH : windowWidth;
		windowHeight = ( windowHeight < TRAIN_WINDOW_MIN_HEIGHT ) ? TRAIN_WINDOW_MIN_HEIGHT : windowHeight;

		setSize( windowWidth, windowHeight );
		setResizable( false );
		thePosition.translate( 10, 10 );

		setLocation( thePosition );
		setDefaultCloseOperation( DISPOSE_ON_CLOSE );

		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				updateSolutionWindowInstance.dispose();
				updateSolutionWindowInstance = null;
			}
		} );

		// Creates the Box
		Box listPanel = Box.createHorizontalBox();

		JPanel listButtonPanel = new JPanel( new GridLayout( 1, 2 ) );
		JPanel generalButtonPanel = new JPanel( new BorderLayout() );
		JPanel optionsRunPanel = new JPanel( new GridLayout( 1, 2 ) );

		listPanel.setPreferredSize( new Dimension( windowWidth, (int) ( ( windowHeight * 5.0 ) / 8 ) ) );

		// Adding the box for file lists
		setLayout( new BorderLayout() );
		add( listPanel, BorderLayout.NORTH );

		Dimension panelSize = new Dimension( (int) ( 0.30 * listPanel.getPreferredSize().width ),
				listPanel.getPreferredSize().height );

		listPanel.add( buildConceptXMLListPanel( panelSize ) );
		listPanel.add( buildTextListPanel( panelSize ) );

		// Adding the add and remove file buttons
		add( generalButtonPanel, BorderLayout.SOUTH );
		generalButtonPanel.add( listButtonPanel, BorderLayout.NORTH );

		panelSize = new Dimension( (int) ( 0.30 * listPanel.getPreferredSize().width ),
				(int) ( 0.10 * listPanel.getPreferredSize().height ) );

		JPanel refXMLButtonsPanel = new JPanel( new FlowLayout() );
		refXMLButtonsPanel.add( getAddRemoveButtonPanel( new AddReferenceConceptXMLFileListener(),
				new RemoveReferenceConceptXMLFileListener(), "Reference XML", panelSize ) );
		listButtonPanel.add( refXMLButtonsPanel );

		JPanel textButtonsPanel = new JPanel( new FlowLayout() );
		textButtonsPanel.add( getAddRemoveButtonPanel( new AddTextFileListener(), new RemoveTextFileListener(), "Text",
				panelSize ) );
		listButtonPanel.add( textButtonsPanel );

		// Adding score button
		if ( action.equals( "update" ) )
		{
			setTitle( "Update Existing Solution" );

			optionsRunPanel.add( getUpdatePanel() );
		}
		else
		{
			setTitle( "RapTAT Training Window" );

			optionsRunPanel.add( getOptionsPanel() );
			optionsRunPanel.add( getTrainingPanel() );
			generalButtonPanel.add( getTrainingOptionPanel(), BorderLayout.CENTER );
		}

		optionsRunPanel.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ),
				BorderFactory.createLineBorder( Color.GRAY ) ) );

		generalButtonPanel.add( Box.createRigidArea( new Dimension( 0, 40 ) ) );

		JPanel buttonPanel = new JPanel( new BorderLayout() );
		buttonPanel.add( getSchemaPanel(), BorderLayout.NORTH );
		buttonPanel.add( optionsRunPanel, BorderLayout.SOUTH );
		generalButtonPanel.add( buttonPanel, BorderLayout.SOUTH );
		setVisible( true );
		pack();
	}


	private void printTable()
	{
		for (List<String> list : this.strTable)
		{
			for (String str : list)
			{
				System.out.print( str + "\t" );
			}
			System.out.println( "" );
		}
	}


	/**
	 * Returns an instance of the UpdateSolutionWindow class.
	 *
	 * @param thePoint
	 *            Upper-left coordinate of the window
	 * @param theDimension
	 *            Dimension of the window
	 * @param action
	 *            Action selected from the menu (Train/Update solution)
	 *
	 * @return An instance of the UpdateSolutionWindow class
	 *
	 * @author Sanjib Saha, July 22, 2014
	 */
	public static synchronized UpdateAnnotatorWindow getInstance(Point thePoint, Dimension theDimension, String action)
	{
		if ( updateSolutionWindowInstance == null )
		{
			updateSolutionWindowInstance = new UpdateAnnotatorWindow( thePoint, theDimension, action );
		}

		return updateSolutionWindowInstance;
	}


	/**
	 * Tests the UpdateSolutionWindow Class
	 *
	 * @param args
	 */
	public static void main(String[] args)
	{
		UpdateAnnotatorWindow.getInstance( new Point( 100, 100 ), new Dimension( 600, 800 ), "training" );
	}
}
