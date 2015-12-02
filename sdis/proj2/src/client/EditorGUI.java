package client;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.Vector;


@SuppressWarnings("serial")
public class EditorGUI extends JFrame implements ActionListener, KeyListener {

    /*public static void main(String[] args) {
        new EditorGUI();
    }*/

    //============================================
    // FIELDS
    //============================================

    // Menus
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenuItem newFile, openFile, saveFile, saveAsFile, exit;
    private JMenuItem undoEdit, redoEdit, selectAll, copy, paste, cut;


    private Vector<String> loggedChanges;

    // Window
    private JFrame editorWindow;

    // Text Area
    private Border textBorder;
    private JScrollPane scroll;
    public JTextArea textArea;
    private Font textFont;

    // Window
    private JFrame window;


    // Is File Saved/Opened
    private boolean opened = false;
    private boolean saved = false;

    // Record Open File for quick saving
    private File openedFile;

    // Undo manager for managing the storage of the undos
    // so that the can be redone if requested
    private UndoManager undo;

    //============================================
    // CONSTRUCTOR
    //============================================

    public EditorGUI() {
        super("JavaEdit");

        // Create Menus
        fileMenu();
        editMenu();

        // Create Text Area
        createTextArea();

        // Create Undo Manager for managing undo/redo commands
        undoMan();

        // Create Window
        createEditorWindow();
    }
    public EditorGUI(File filename) {
        super("JavaEdit");

        // Create Menus
        fileMenu();
        editMenu();

        // Create Text Area
        createTextArea();

        // Create Undo Manager for managing undo/redo commands
        undoMan();

        // Create Window
        createEditorWindow();
        openingFiles(filename);
    }

    private JFrame createEditorWindow() {
        editorWindow = new JFrame("JavaEdit");
        editorWindow.setVisible(true);
        editorWindow.setExtendedState(Frame.MAXIMIZED_BOTH);
        editorWindow.setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Create Menu Bar
        editorWindow.setJMenuBar(createMenuBar());
        editorWindow.add(scroll, BorderLayout.CENTER);
        editorWindow.pack();
        // Centers application on screen
        editorWindow.setLocationRelativeTo(null);

        return editorWindow;
    }


    private JTextArea createTextArea() {
        textBorder = BorderFactory.createBevelBorder(0, Color.RED, Color.RED);
        textArea = new JTextArea(30, 50);
        textArea.setEditable(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(textBorder, BorderFactory.createEmptyBorder(2, 5, 0, 0)));


        loggedChanges = new Vector<String>();

        textFont = new Font("Verdana", 0, 14);
        textArea.setFont(textFont);
        textArea.addKeyListener(this);

        textArea.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    logChange(e.getType().toString(),e.getOffset(),e.getLength(),textArea.getText(e.getOffset(),e.getLength()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    logChange(e.getType().toString(),e.getOffset(),e.getLength(),textArea.getText(e.getOffset(),e.getLength()));
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                System.out.println(arg0.getOffset());
                System.out.println(arg0.toString());

            }
        });


        scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        return textArea;        
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        return menuBar;
    }

    private UndoManager undoMan() {
        // Listener for undo and redo functions to document
        undo = new UndoManager();
        textArea.getDocument().addUndoableEditListener(new UndoableEditListener() {

            public void undoableEditHappened(UndoableEditEvent e) {
                undo.addEdit(e.getEdit());
            }
        });

        return undo;
    }

    private void fileMenu() {
        // Create File Menu
        fileMenu = new JMenu("File");
        fileMenu.setPreferredSize(new Dimension(40, 20));

        // Add file menu items
        newFile = new JMenuItem("New");
        newFile.addActionListener(this);
        newFile.setPreferredSize(new Dimension(100, 20));
        newFile.setEnabled(true);

        openFile = new JMenuItem("Open...");
        openFile.addActionListener(this);
        openFile.setPreferredSize(new Dimension(100, 20));
        openFile.setEnabled(true);

        saveFile = new JMenuItem("Save");
        saveFile.addActionListener(this);
        saveFile.setPreferredSize(new Dimension(100, 20));
        saveFile.setEnabled(true);

        saveAsFile = new JMenuItem("Save As...");
        saveAsFile.addActionListener(this);
        saveAsFile.setPreferredSize(new Dimension(100, 20));
        saveAsFile.setEnabled(true);


        exit = new JMenuItem("Exit");
        exit.addActionListener(this);
        exit.setPreferredSize(new Dimension(100, 20));
        exit.setEnabled(true);


        // Add items to menu
        fileMenu.add(newFile);
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.add(exit);
    }

    private void editMenu() {
        editMenu = new JMenu("Edit");
        editMenu.setPreferredSize(new Dimension(40, 20));

        // Add file menu items
        undoEdit = new JMenuItem("Undo");
        undoEdit.addActionListener(this);
        undoEdit.setPreferredSize(new Dimension(100, 20));
        undoEdit.setEnabled(true);

        redoEdit = new JMenuItem("Redo");
        redoEdit.addActionListener(this);
        redoEdit.setPreferredSize(new Dimension(100, 20));
        redoEdit.setEnabled(true);

        selectAll = new JMenuItem("Select All");
        selectAll.addActionListener(this);
        selectAll.setPreferredSize(new Dimension(100, 20));
        selectAll.setEnabled(true);

        copy = new JMenuItem("Copy");
        copy.addActionListener(this);
        copy.setPreferredSize(new Dimension(100, 20));
        copy.setEnabled(true);

        paste = new JMenuItem("Paste");
        paste.addActionListener(this);
        paste.setPreferredSize(new Dimension(100, 20));
        paste.setEnabled(true);

        cut = new JMenuItem("Cut");
        cut.addActionListener(this);
        cut.setPreferredSize(new Dimension(100, 20));
        cut.setEnabled(true);

        // Add items to menu
        editMenu.add(undoEdit);
        editMenu.add(redoEdit);
        editMenu.add(selectAll);
        editMenu.add(copy);
        editMenu.add(paste);
        editMenu.add(cut);
    }

    // Method for saving files - Removes duplication of code
    private void saveFile(File filename) throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(textArea.getText());
            writer.close();
            saved = true;
            editorWindow.setTitle("JavaText - " + filename.getName());
        } catch (IOException err) {
            err.printStackTrace();
        }
    }


    // Method for quick saving files
    private void quickSave(File filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(textArea.getText());
            writer.close();
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    // Method for opening files
    private void openingFiles(File filename) {
        try {
            openedFile = filename;
            FileReader reader = new FileReader(filename);
            textArea.read(reader, null);
            textArea.getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    try {
                        logChange(e.getType().toString(),e.getOffset(),e.getLength(),textArea.getText(e.getOffset(),e.getLength()));
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    logChange(e.getType().toString(),e.getOffset(),e.getLength(),"");
                }

                @Override
                public void changedUpdate(DocumentEvent arg0) {
                    System.out.println(arg0.getOffset());
                    System.out.println(arg0.toString());
                }
            });
            opened = true;
            editorWindow.setTitle("JavaEdit - " + filename.getName());
        } catch (IOException err) {
            err.printStackTrace();
        }
    }

    private void logChange(String s, int offset, int length, String text) {
        //java.util.Date date= new java.util.Date();
    	if(text.equals("\n"))
    		text = "\\n";
        String change = new StringBuilder().append(s).append("/").append(length).append("/").append(text).append("/").append(offset).toString();
        loggedChanges.add(change);
        //System.out.println(change);

    }

    public void actionPerformed(ActionEvent event) {
        if(event.getSource() == newFile) {
            new EditorGUI();
        } else if(event.getSource() == openFile) {
            JFileChooser open = new JFileChooser();
            open.showOpenDialog(null);
            File file = open.getSelectedFile();                
            openingFiles(file);
        } else if(event.getSource() == saveFile) {
            try {
                saveFile(openedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JFileChooser save = new JFileChooser();
            File filename = save.getSelectedFile();
            if(opened == false && saved == false) {
                save.showSaveDialog(null);
                int confirmationResult;
                if(filename.exists()) {
                    confirmationResult = JOptionPane.showConfirmDialog(saveFile, "Replace existing file?");
                    if(confirmationResult == JOptionPane.YES_OPTION) {
                        try {
                            saveFile(filename);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        saveFile(filename);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                quickSave(openedFile);
            }
        } else if(event.getSource() == saveAsFile) {
            JFileChooser saveAs = new JFileChooser();
            saveAs.showSaveDialog(null);
            File filename = saveAs.getSelectedFile();
            int confirmationResult;
            if(filename.exists()) {
                confirmationResult = JOptionPane.showConfirmDialog(saveAsFile, "Replace existing file?");
                if(confirmationResult == JOptionPane.YES_OPTION) {
                    try {
                        saveFile(filename);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    saveFile(filename);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if(event.getSource() == exit) {
            System.exit(0);
        } else if(event.getSource() == undoEdit) {
            try {
                undo.undo();
            } catch(CannotUndoException cu) {
                cu.printStackTrace();
            }
        } else if(event.getSource() == redoEdit) {
            try {
                undo.redo();
            } catch(CannotUndoException cur) {
                cur.printStackTrace();
            }
        } else if(event.getSource() == selectAll) {
            textArea.selectAll();
        }  else if(event.getSource() == copy) {
            textArea.copy();
        } else if(event.getSource() == paste) {
            textArea.paste();
        } else if(event.getSource() == cut) {
            textArea.cut();
        }
    }

    //============================================
    // GETTERS AND SETTERS
    //============================================

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(JTextArea text) {
        textArea = text;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        quickSave(openedFile);
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public Vector<String> getLoggedChanges() {
        return loggedChanges;
    }

    public void setLoggedChanges(Vector<String> loggedChanges) {
        this.loggedChanges = loggedChanges;
    }

    public void clearLog()
    {
        loggedChanges.removeAllElements();
    }
    public void applyChange(String change)
    {
        String[] splitChange = change.split("/");
        if(splitChange[0].equals("INSERT"))
        {
            textArea.insert(splitChange[2], Integer.parseInt(splitChange[3]));
            loggedChanges.remove(loggedChanges.size() - 1);
            //quickSave(openedFile);
        }
        else if(splitChange[0].equals("REMOVE"))
        {
            //textArea.remove(Integer.parseInt(splitChange[3]));
            textArea.replaceRange("",Integer.parseInt(splitChange[3]),Integer.parseInt(splitChange[3])+Integer.parseInt(splitChange[1]));
            loggedChanges.remove(loggedChanges.size() - 1);
            //quickSave(openedFile);
        }
    }
}


