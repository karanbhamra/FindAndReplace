//*******************************************************************
//  Karandeep Bhamra
//  October 9, 2018
//  COMP585
//  FindFrame is responsible for displaying the GUI and the actions
//  of the components. It calls the SwingWorker which will call the
//  FileVisitor and walk the folder tree and search for the string.
//  The results are displayed on the table, and words can be replaced.
//*******************************************************************
package com.karanbhamra;

import javax.swing.*;

import org.apache.log4j.Logger;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.prefs.*;


public class FindFrame extends JFrame
{
    // UI variables and objects
    private static int FRAME_WIDTH = 700;
    private static int FRAME_HEIGHT = 700;
    private static String TITLE = "Find & Replace";
    private static String ABOUT = "Project 2\nby\nKaran Bhamra";

    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu aboutMenu;
    private JMenuItem exitMenuItem;
    private JMenuItem aboutMenuItem;

    private JPanel mainPanel;

    private JPanel topPanel;
    private JPanel pathPanel;
    private JLabel pathLabel;
    private JTextField pathTextField;
    private JButton pathBrowseButton;

    private JFileChooser chooser;
    private JPanel searchPanel;
    private JLabel searchLabel;
    private JTextField searchTextField;
    private JButton searchButton;
    private JButton searchAndReplaceButton;

    private JPanel filterPanel;
    private JLabel filterLabel;
    private JTextField filterTextField;
    private JCheckBox matchCaseCheckBox;
    private JCheckBox matchWholeWordCheckBox;

    // Table will hold the results line number, file name and sentence
    private JTable fileTable;
    private String[] tableColumns;
    private JProgressBar progressBar;
    private File selectedFolder;

    // SwingWorker will execute the search code
    private File currentSelectedRowFile;
    private FileVisitor fileVisitor;
    private SwingWorker searchWorker;

    // Preferences will hold the checkbox settings
    private Preferences prefs;
    private final Logger logger;
    private static final String COMMIT_ACTION = "commit";

    // Provides autocompletion of search text field
    private AutoComplete autoComplete;
    private ArrayList<String> keywords;


    // sets up the ui and displays the frame
    public FindFrame(Logger logger)
    {
        this.logger = logger;

        buildFrame();
        buildAndAddMenuBar();
        buildAndAddMainPanel();
        buildAndAddPathPanel();
        addBrowseButtonListener();
        addSearchButtonListener();
        addReplaceButtonListener();
        buildAndSetupTable();
        buildAndProgressBar();
        buildAndAddClickRowOnTable();

        setupSettings();

        initializeAutoComplete();

        displayFrame();

    }

    // Adds autocompletion to the search textfield with some initial keywords
    private void initializeAutoComplete()
    {
        // Without this, cursor always leaves text field
        searchTextField.setFocusTraversalKeysEnabled(false);

        // Our words to complete
        keywords = new ArrayList<String>();
        keywords.add("karan");
        keywords.add("Karan");
        keywords.add("Jessica");
        keywords.add("jessica");
        keywords.add("test");
        keywords.add("Test");

        autoComplete = new AutoComplete(searchTextField, keywords);
        searchTextField.getDocument().addDocumentListener(autoComplete);

        // Maps the tab key to the commit action, which finishes the autocomplete
        // when given a suggestion
        searchTextField.getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
        searchTextField.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());
    }

    // Saves the settings of Case Matching and Whole Word Matching to the Preferences
    // Also reads the settings on startup and sets the checkboxes accordingly
    private void setupSettings()
    {
        prefs = Preferences.userNodeForPackage(this.getClass());

        if (prefs.get("MatchCase", "").equals("true"))
        {
            matchCaseCheckBox.setSelected(true);
            logger.info("MatchCase setting enabled.");

        } else
        {
            matchCaseCheckBox.setSelected(false);
            logger.info("MatchCase setting disabled.");

        }

        if (prefs.get("MatchWord", "").equals("true"))
        {
            matchWholeWordCheckBox.setSelected(true);
            logger.info("MatchWord setting enabled.");

        } else
        {
            matchWholeWordCheckBox.setSelected(false);
            logger.info("MatchWord setting disabled.");
        }

        // When the checkbox settings are modified
        matchCaseCheckBox.addChangeListener((e) ->
        {
            if (matchCaseCheckBox.isSelected())
            {
                prefs.put("MatchCase", "true");
            } else
            {
                prefs.put("MatchCase", "false");
            }
        });

        matchWholeWordCheckBox.addChangeListener((e) ->
        {
            if (matchWholeWordCheckBox.isSelected())
            {
                prefs.put("MatchWord", "true");
            } else
            {
                prefs.put("MatchWord", "false");
            }
        });
    }

    private boolean isFolderSelected()
    {
        if (selectedFolder == null)
        {
            return false;
        }

        return true;
    }

    private boolean isRowSelected()
    {
        return (currentSelectedRowFile != null);
    }

    private boolean isSearchTextFieldEmpty()
    {
        return (searchTextField.getText().isEmpty());
    }

    public void searchFieldEmptyMessage()
    {
        JOptionPane.showMessageDialog(this,
                "Text field cannot be empty.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        logger.error("Text field cannot be empty.");
    }

    public void folderNotSelectedMessage()
    {
        JOptionPane.showMessageDialog(this,
                "Please select a folder to search in.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        logger.error("Search folder cannot be empty.");
    }

    // Returns true when the subitem is a whole word in the source string
    public static boolean doesMatchWholeWord(String source, String subItem)
    {
        String pattern = "\\b" + subItem + "\\b";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(source);
        return m.find();
    }

    //  Reads the selected file on table and replaces the matched words with the inputted word
    private void addReplaceButtonListener()
    {
        searchAndReplaceButton.addActionListener((event) ->
        {

            // a row is selected and search field has text then replace the words in there

            if (isRowSelected() && !isSearchTextFieldEmpty())
            {
                String input = JOptionPane.showInputDialog(this, "Enter new word:");

                Path path = currentSelectedRowFile.toPath();
                Charset charset = StandardCharsets.UTF_8;

                String content = null;
                try
                {
                    content = new String(Files.readAllBytes(path), charset);
                    content = content.replaceAll(searchTextField.getText(), input);
                    Files.write(path, content.getBytes(charset));

                    JOptionPane.showMessageDialog(this,
                            "Replaced all occurrences of " + searchTextField.getText() + " with " + input + "."
                            , "Success", JOptionPane.INFORMATION_MESSAGE);

                    logger.info("Replaced all occurrences of " + searchTextField.getText() + " with " + input + ".");

                    currentSelectedRowFile = null;
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            } else if (isFolderSelected() && !isSearchTextFieldEmpty())
            {
                // if folder and text are provided, search and then replace

                startSearchThread(true);

            } else if (isSearchTextFieldEmpty())
            {
                searchFieldEmptyMessage();
            } else if (!isFolderSelected())
            {
                folderNotSelectedMessage();

            }

        });
    }

    // sets the current selected file based on which row is clicked on table
    private void buildAndAddClickRowOnTable()
    {
        fileTable.getSelectionModel().addListSelectionListener((event) ->
        {
            if (fileTable.getSelectedRow() > -1)
            {
                String file = fileTable.getValueAt(fileTable.getSelectedRow(), 1).toString();

                currentSelectedRowFile = new File(file);
            }
        });
    }

    // ProgressBar is indeterminate when wakling the filesystem and then shows progress when checking files
    private void buildAndProgressBar()
    {

        progressBar = new JProgressBar();
        progressBar.setValue(0);
        progressBar.setIndeterminate(false);

        mainPanel.add(progressBar, BorderLayout.SOUTH);
    }

    private void buildAndSetupTable()
    {
        tableColumns = new String[]{"Line #", "File", "Line"};
        fileTable = new JTable(new Object[][]{}, tableColumns);
        mainPanel.add(new JScrollPane(fileTable), BorderLayout.CENTER);
    }

    private void addBrowseButtonListener()
    {
        pathBrowseButton.addActionListener((event) ->
        {
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            {
                selectedFolder = chooser.getSelectedFile();
                pathTextField.setText(selectedFolder.getAbsolutePath());
            } else
            {
            }
        });
    }

    private void addSearchButtonListener()
    {
        searchButton.addActionListener((event) ->
        {

            if (isFolderSelected())
            {

                if (searchTextField.getText().length() > 0)
                {
                    startSearchThread(false);
                } else
                {
                    searchFieldEmptyMessage();
                }
            } else
            {
                folderNotSelectedMessage();
            }
        });
    }

    // The SwingWorker logic is implemented here and autocomplete adds the entered keyword to the list
    private void startSearchThread(boolean replace)
    {
        autoComplete.setNewKeyword(searchTextField.getText());

        // Take the extensions field and split it and retrieve the extension text which will search the files
        String[] splitextensions = filterTextField.getText().split(",");

        String[] filteredExtensions = new String[splitextensions.length];

        for (int i = 0; i < splitextensions.length; i++)
        {
            filteredExtensions[i] = splitextensions[i].substring(1);
        }

        ArrayList<String> extensions = new ArrayList<String>(Arrays.asList(filteredExtensions));


        fileVisitor = new FileVisitor(logger, extensions);


        searchWorker = new SwingWorker()
        {
            //  walk the system and search each line in matched file to the text
            @Override
            protected String doInBackground() throws Exception
            {
                // set progress to be indeterminate while it walks the filesystem
                progressBar.setIndeterminate(true);

                int progressBarValue = 0;
                try
                {
                    Files.walkFileTree(selectedFolder.toPath(), fileVisitor);

                    progressBar.setIndeterminate(false);

                    progressBar.setValue(progressBarValue);

                    ArrayList<String> matchedFiles = fileVisitor.getMatchedFiles();

                    ArrayList<SearchResult> searchResults = new ArrayList<>();

                    int count = 0;
                    for (var file : matchedFiles)
                    {
                        progressBarValue = (int) ((++count / (double) matchedFiles.size()) * 100.0);


                        progressBar.setValue(progressBarValue);

                        // read each of the file and search it
                        Scanner sc = new Scanner(new File(file));
                        int lineCounter = 0;
                        while (sc.hasNextLine())
                        {
                            String currentLine = sc.nextLine();
                            lineCounter++;


                            if (!matchCaseCheckBox.isSelected())    // case insensitive
                            {
                                if (matchWholeWordCheckBox.isSelected())    // if whole word is selected
                                {
                                    boolean wholeWordResult = doesMatchWholeWord(currentLine.toLowerCase(), searchTextField.getText().toLowerCase());

                                    if (wholeWordResult)
                                    {
                                        searchResults.add(new SearchResult(lineCounter, file, currentLine));
                                    }
                                } else        // not whole word
                                {
                                    if (currentLine.toLowerCase().contains(searchTextField.getText().toLowerCase()))
                                    {
                                        searchResults.add(new SearchResult(lineCounter, file, currentLine));
                                    }
                                }

                            } else      // case sensitive
                            {
                                if (matchWholeWordCheckBox.isSelected())
                                {
                                    boolean wholeWordResult = doesMatchWholeWord(currentLine, searchTextField.getText());
                                    if (wholeWordResult)
                                    {
                                        searchResults.add(new SearchResult(lineCounter, file, currentLine));
                                    }
                                } else if (currentLine.contains(searchTextField.getText()))
                                {

                                    searchResults.add(new SearchResult(lineCounter, file, currentLine));
                                }
                            }
                        }
                    }

                    if (matchedFiles.size() > 0)
                    {

                        TableModel tableModel = new DefaultTableModel(tableColumns, 0);

                        for (var searchResult : searchResults)
                        {
                            ((DefaultTableModel) tableModel).addRow(searchResult.toArray());
                        }

                        fileTable.setModel(tableModel);
                    }


                } catch (IOException e)
                {
                    e.printStackTrace();
                }


                String res = "Finished Execution";
                return res;
            }

            // on completion of task, if replacement was an option, do the replacement
            @Override
            protected void done()
            {
                logger.info("Searching complete for " + searchTextField.getText());
                logger.info("Found " + fileVisitor.getMatchedFiles().size() + " files.");

                if (fileTable.getRowCount() < 1)
                {
                    JOptionPane.showMessageDialog(FindFrame.super.rootPane,
                            "No items found.", "Search Results", JOptionPane.INFORMATION_MESSAGE);
                    logger.info("No items found.");
                }


                if (replace && fileTable.getRowCount() >= 1)
                {
                    if (fileVisitor.getMatchedFiles().size() > 0)
                    {
                        String input = JOptionPane.showInputDialog(FindFrame.super.rootPane, "Enter new word:");


                        for (var file : fileVisitor.getMatchedFiles())
                        {

                            Path path = new File(file).toPath();
                            Charset charset = StandardCharsets.UTF_8;

                            String content = null;
                            try
                            {
                                content = new String(Files.readAllBytes(path), charset);
                                content = content.replaceAll(searchTextField.getText(), input);
                                Files.write(path, content.getBytes(charset));


                                currentSelectedRowFile = null;
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        JOptionPane.showMessageDialog(FindFrame.super.rootPane,
                                "Replaced all occurrences of " + searchTextField.getText() + " with " + input + "."
                                , "Success", JOptionPane.INFORMATION_MESSAGE);
                        logger.info("Replaced all occurrences of " + searchTextField.getText() + " with " + input + ".");

                    }
                }
                progressBar.setValue(0);


            }
        };


        searchWorker.execute();

    }


    private void buildAndAddPathPanel()
    {

        topPanel = new JPanel(new BorderLayout(10, 10));

        pathPanel = new JPanel(new BorderLayout(5, 5));
        pathLabel = new JLabel("Find in folder:");
        pathTextField = new JTextField("No folder selected.");
        pathTextField.setHorizontalAlignment(JTextField.CENTER);
        pathTextField.setEditable(false);
        pathBrowseButton = new JButton("Browse");
        pathPanel.add(pathLabel, BorderLayout.WEST);
        pathPanel.add(pathTextField, BorderLayout.CENTER);
        pathPanel.add(pathBrowseButton, BorderLayout.EAST);
        chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a folder:");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


        searchPanel = new JPanel(new BorderLayout(5, 5));
        searchLabel = new JLabel("Find Text:");
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchTextField = new JTextField();
        searchTextField.setHorizontalAlignment(JTextField.CENTER);
        searchPanel.add(searchTextField, BorderLayout.CENTER);
        searchButton = new JButton("Search");
        searchAndReplaceButton = new JButton("Replace");

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));

        buttonPanel.add(searchButton);
        buttonPanel.add(searchAndReplaceButton);

        searchPanel.add(buttonPanel, BorderLayout.EAST);

        filterPanel = new JPanel(new BorderLayout(5, 5));
        filterLabel = new JLabel("File Ext:");
        filterTextField = new JTextField(".java,.txt,.cfg,.html,.css,.js");
        matchWholeWordCheckBox = new JCheckBox("Match Words");
        matchCaseCheckBox = new JCheckBox("Match Case");

        filterPanel.add(filterLabel, BorderLayout.WEST);
        filterPanel.add(filterTextField, BorderLayout.CENTER);

        JPanel checkBoxPanel = new JPanel(new GridLayout(2, 1));
        checkBoxPanel.add(matchWholeWordCheckBox);
        checkBoxPanel.add(matchCaseCheckBox);
        filterPanel.add(checkBoxPanel, BorderLayout.EAST);

        topPanel.add(pathPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
    }

    private void buildAndAddMainPanel()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(mainPanel);
    }

    private void buildAndAddMenuBar()
    {

        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        aboutMenu = new JMenu("About");
        exitMenuItem = new JMenuItem("Exit");
        aboutMenuItem = new JMenuItem("About");

        exitMenuItem.addActionListener((event) -> System.exit(0));

        aboutMenuItem.addActionListener((event) ->
                JOptionPane.showMessageDialog(this, ABOUT, "About", JOptionPane.PLAIN_MESSAGE));
        fileMenu.add(exitMenuItem);
        aboutMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);

        this.setJMenuBar(menuBar);

    }

    private void buildFrame()
    {
        this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle(TITLE);
    }

    private void displayFrame()
    {
        this.setVisible(true);
    }
}
