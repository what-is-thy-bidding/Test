/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package raeval;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.util.*;


/**
 *
 * @author nick
 */

public class UserInterface extends JFrame implements CaretListener, KeyListener, DocumentListener {

    static JFileChooser fileChooser;    // static here to preserve state between invocations

    JTextPane text;
    Color penColor;
    Color userColor;
    Color sysColor;
    Color errorColor;
    int offsetLimit;
    UserInterface userInterface;
    ArrayList<String> lastCommand;
    int historyBrowsing;
    boolean suspended;
    boolean createdForEdit;

    // constructor
    public UserInterface() {
        // build gui for user interface
        super("Relational Algebra Evaluator");
        setLayout(new BorderLayout());
        setBackground(Color.gray);
        setSize(800,800);
        // create a text pane and a scroll pane
        text = new JTextPane();
        JScrollPane pane = new JScrollPane(text);
        this.setLayout(new BorderLayout());
        // format the gui elements
        text.setBorder(new MatteBorder(6,6,6,6,Color.black));
        Font font = new Font("Courier", Font.PLAIN, 14);
        text.setFont(font);
        text.getDocument().addDocumentListener(this);
        text.setEditable(true);
        text.addCaretListener(this);
        text.addKeyListener(this);
        pane.setBorder(null);
        this.add(pane,BorderLayout.CENTER);
        // predefined colours
        sysColor = Color.white;
        userColor = Color.cyan;
        errorColor = Color.red;
        text.setBackground(Color.black);
        text.setForeground(sysColor);
        text.setCaretColor(sysColor);
        // initialise ivars
        lastCommand = new ArrayList<String>();
        historyBrowsing = 0;
        suspended = false;
        offsetLimit = 0;
        fileChooser = new JFileChooser();
    }

    // show the welcome message
    public void showMessage() {
        UserInterface commandPane = this;
        commandPane.setPenColor(Color.white);
        commandPane.printString("Relational Algebra Evaluator");
        commandPane.printString("Version 0.2");
        commandPane.printString("For a list of commands evaluate 'help'");
        commandPane.printString("To see help for a specific keyword evaluate 'help <keyword>'");
        commandPane.printString("To open the editor evaluate 'edit <relation>'");
        commandPane.setPenColor(Color.red);
        commandPane.printString("For newline press <enter>, to evaluate press <ctrl><enter>");
        commandPane.setPenColor(Color.white);
        commandPane.printString("");
        commandPane.printString("Ready");
        commandPane.printMark();
        commandPane.setPenColor(Color.red);
    }

    // reveal the editor window
    // and put interpreter into suspension
    public void showEditor(String editRelnStr) {
        createdForEdit = false;
        if (!Relation.definedNameExists(editRelnStr)) {
            // this is a new relation
            Relation newReln = new Relation();
            newReln.addAttribute("", "");
            Relation.assign(editRelnStr, newReln);
            createdForEdit = true;
        }
        Relation editReln = Relation.referenceForRelation(editRelnStr);
        Editor editorWindow = new Editor(editRelnStr,this);
        editorWindow.setVisible(true);
    }

    // destroy the editor window
    // and awaken interpreter from suspension
    public void hideEditor(Editor editor, boolean accepted) {
        String relName = editor.editName;
        suspended = false;
        text.setEnabled(true);
        text.setEditable(true);
        if (accepted) {
            command(relName);
        } else {
            if (createdForEdit) Relation.definedRemove(relName);
            setPenError();
            printString("");
            printString("Edit cancelled");
            setPenSystem();
            printMark();
        }
    }

    // process a command
    public void command(String commStr) {
        boolean process = true; // will only process rel alg, not interpreter commands
        // quit
        if (commStr.trim().matches("quit")) {
            quit();
            process = false; // interpreter command
        }
        // edit
        if (commStr.trim().startsWith("edit")) {
            // get name of relation to edit
            String editRelation = commStr.substring(4).trim();
            if (editRelation.length() > 0) {
                showEditor(editRelation);
                suspended = true;
                text.setEnabled(false);
                text.setEditable(false);
                process = false; // interpreter command
            } else {
                // no name was provided
                setPenError();
                printString("");
                printString("Format is 'edit <relation>' - must specify which relation to edit or create");
                setPenSystem();
                process = false; // failed interpreter command
            }
        }
        // help
        if (commStr.trim().matches("help")) {
            // help on its own - list commands
            help();
            process = false; // interpreter command
        } else if (commStr.trim().startsWith("help")) {
            // help with key word
            String keyWord = commStr.substring(4).trim();
            help(keyWord);
            process = false; // interpreter command
        }
        // if not an interpreter command then evaluate as rel algebra
        if (process) {
            setPenSystem();
            String result = Relation.evaluate(commStr);
            printString("");
            if (!Relation.error()) {
                // evaluated ok - so display result
                printString(result);
            } else {
                // error during evaluation - display error message
                setPenError();
                printString(Relation.errorMessage());
                setPenSystem();
            }
        }
        // print the next command prompt
        // unless editor has suspended interpreter
        if (!suspended) printMark();
    }

    // display list of commands
    public void help() {
        setPenSystem();
        printString("");
        printString("");
        printString("Relational algebra operators:");
        printString("select <relation> where <condition>");
        printString("project <relation> over <attribute> {, <attribute> ... }");
        printString("<relation> join <relation>");
        printString("divide <relation> by <relation>");
        printString("<relation> rename ( <attribute> as <new-name> {, <attribute> as <new-name> ... } )");
        printString("<relation> union <relation>");
        printString("<relation> intersection <relation>");
        printString("<relation> difference <relation>");
        printString("<relation> times <relation>");
        printString("<relation> alias <relation>");
        printString("");
        printString("Non-relational operators:");
        printString("<relation> := <relation>   (assignment)");
        printString("show <relation>");
        printString("<relation> := load <csv-filename>");
        printString("edit <relation>");
        printString("quit");
        printString("");
        printString("For conditions: ( ) + - * / and or not < > = <= >= <>");
        printString("For more help: help <keyword>");
        printString("Project homepage: http://code.google.com/p/relational-algebra/");
        printString("");
    }

    // recognise keyword for help
    public void help(String keyWord) {
        setPenSystem();
        printString("");
        printString("");
        boolean recognised = false;
        if (keyWord.matches("select")) {
            helpSelect();
            recognised = true;
        }
        if (keyWord.matches("project")) {
            helpProject();
            recognised = true;
        }
        if (keyWord.matches("join")) {
            helpJoin();
            recognised = true;
        }
        if (keyWord.matches("rename")) {
            helpRename();
            recognised = true;
        }
        if (keyWord.matches("divide")) {
            helpDivide();
            recognised = true;
        }
        if (keyWord.matches("union")) {
            helpUnion();
            recognised = true;
        }
        if (keyWord.matches("intersection")) {
            helpIntersection();
            recognised = true;
        }
        if (keyWord.matches("difference")) {
            helpDifference();
            recognised = true;
        }
        if (keyWord.matches("times")) {
            helpTimes();
            recognised = true;
        }
        if (keyWord.matches("alias")) {
            helpAlias();
            recognised = true;
        }
        if (keyWord.matches("load")) {
            helpLoad();
            recognised = true;
        }
        if (keyWord.matches("assign") || keyWord.matches("assignment") || keyWord.matches(":=")) {
            helpAssign();
            recognised = true;
        }
        if (keyWord.matches("show")) {
            helpShow();
            recognised = true;
        }
        if (keyWord.matches("edit")) {
            helpEdit();
            recognised = true;
        }
        // if no words match, show message
        if (!recognised) printString("Keyword not recognised");
    }

    public void helpEdit() {
        printString("edit <relation>");
        printString("Example:");
        printString("    edit myrel");
        printString("Launches the editor to alter attributes, domains or values in the specified");
        printString("relation.  If the relation exists then its current values are loaded for");
        printString("editing.  Otherwise, if it does not exist, then a new relation is created.");
        printString("Use menu options in the editor to make changes.  Choose 'accept' to bring");
        printString("changes back into the interpreted environment.");
    }

    public void helpSelect() {
        printString("select <relation> where <condition>");
        printString("Examples:");
        printString("    select painting where price >= 500000");
        printString("    select painting where artist = \"goya\" and price < 250000");
        printString("    select painting where yearpainted < artistbirthyear + 25");
        printString("    select painting where (artist = \"picasso\" or artist = \"goya\") and price > 250000");
        printString("    project (select painting where artist = \"renoir\") over yearpainted");
        printString("Valid conditions may contain: ( ) + - * / and or not < > <= >= <>");
        printString("Note: the 'select' keyword may be omitted, the 'where' keyword is mandatory");
    }

    public void helpProject() {
        printString("project <relation> over <attribute> {, <attribute> ...}");
        printString("Examples:");
        printString("    project solarsystem over planetname, sundistance");
        printString("    project solarsystem over moonname");
        printString("    select (project solarsystem over planetname, sundistance) where sundistance > 1");
        printString("Note: the 'project' keyword may be omitted, the 'over' keyword is mandatory");
    }

    public void helpJoin() {
        printString("<relation> join <relation>");
        printString("Examples:");
        printString("    artist join painting");
        printString("    planet join moon");
        printString("    project (artist join painting) over artistname, paintingname, yearpainted");
        printString("Note: implements the natural join");
    }

    public void helpRename() {
        printString("<relation> rename ( <attribute> as <new-name> {, <attribute> as <new-name> ... } )");
        printString("Examples:");
        printString("    author rename (name as pseudonym)");
        printString("    author rename (name as pseudonym, book as booktitle");
        printString("    project (author rename (name as pseudonym)) over pseudonym");
    }

    public void helpDivide() {
        printString("divide <relation> by <relation>");
        printString("Examples:");
        printString("    divide animal by continent");
    }

    public void helpUnion() {
        printString("<relation> union <relation>");
        printString("Examples:");
        printString("    frenchpresidents union italianpresidents");
        printString("    select (oldestmen union oldestwomen) where age > 110");
        printString("Note: the two relations must be union-compatible - same attributes and domains");
    }

    public void helpIntersection() {
        printString("<relation> intersection <relation>");
        printString("Examples:");
        printString("    filmactor intersection tvactor");
        printString("    project (filmactor intersection tvactor) where firstappeared >= 1960");
        printString("Note: the two relations must be union-compatible - same attributes and domains");
    }

    public void helpDifference() {
        printString("<relation> difference <relation>");
        printString("Examples:");
        printString("   filmactor difference tvactor");
        printString("   project (filmactor difference tvactor) over name");
        printString("Note: the two relations must be union-compatible - same attributes and domains");
    }

    public void helpTimes() {
        printString("<relation> times <relation>");
        printString("Examples:");
        printString("   ship times port");
    }

    public void helpAlias() {
        printString("<relation> alias <relation>");
        printString("Examples:");
        printString("   writer alias author");
        printString("   sculptor alias (select artist where medium = \"marble\")");
    }

    public void helpLoad() {
        printString("<relation> := load \"filename\"");
        printString("Examples");
        printString("    artist := load \"/home/myfiles/Desktop/artistreln.csv\"");
        printString("    artist := load \"c:\\myfiles\\artistreln.csv\"");
        printString("File format is comma-separated value");
        printString("First row must consist of attribute names");
        printString("Second row must consist of domain names");
        printString("Subsequent rows define tuples");
        printString("The use of double quotes (\") to enclose strings is optional");
        printString("Note: load is not part of the relational algebra");
    }

    public void helpAssign() {
        printString("<relation> := <relation>   (assignment)");
        printString("Examples");
        printString("    sculptor := select artist where medium = \"marble\"");
        printString("Note: assignment is not part of the relational algebra");
    }

    public void helpShow() {
        printString("show <relation>");
        printString("Examples");
        printString("   show invention");
        printString("   show project (select painting where yearpainted > 1900) over paintingname");
        printString("Note: show is not part of the relational algebra");
    }

    // quit application on command
    public void quit() {
        this.setVisible(false);
        this.dispose();
    }

    // print a string in the text pane
    public void printString(String str) {
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, penColor);
        try {
            text.getDocument().insertString(text.getDocument().getLength(),str.concat("\n"),sas);
        } catch (Exception e) {}
        text.setCaretPosition(text.getDocument().getLength());
    }

    // print a prompt for next command
    public void printMark() {
        SimpleAttributeSet sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, sysColor);
        try {
            text.getDocument().insertString(text.getDocument().getLength(),"\n>",sas);
        } catch (Exception e) {}
        sas = new SimpleAttributeSet();
        StyleConstants.setForeground(sas, userColor);
        try {
            text.getDocument().insertString(text.getDocument().getLength()," ",sas);
        } catch (Exception e) {}
        text.setCaretPosition(text.getDocument().getLength());
        text.setForeground(userColor);
        text.setCaretColor(userColor);
        // set the start limit for this command
        offsetLimit = text.getDocument().getLength();
    }

    // change pen colour
    public void setPenColor(Color col) {
        penColor = col;
    }

    // system messages pen color
    public void setPenSystem() {
        penColor = sysColor;
    }

    // user text pen color
    public void setPenUser() {
        penColor = userColor;
    }

    // error messages pen color
    public void setPenError() {
        penColor = errorColor;
    }

    // required for interface
    public void changedUpdate(DocumentEvent e) {}

    // cannot remove beyond the limit of the start of the prompt
    public void removeUpdate(DocumentEvent e) {
        int len = text.getDocument().getLength();
        if (len < offsetLimit) {
            // cannot insertString in an event handler so...
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    StyleConstants.setForeground(sas, userColor);
                    try {
                        int posn = text.getDocument().getLength();
                        text.getDocument().insertString(posn," ",sas);
                    } catch (Exception ex) {System.out.println("exceprem: " + ex.getMessage());}
                }
            });
        }
    }

    // required for interface
    public void insertUpdate(DocumentEvent e) {}

    // do not allow caret to move before start of prompt
    public void caretUpdate(CaretEvent e) {
        int mark = e.getMark();
        if (mark < offsetLimit && offsetLimit <= text.getDocument().getLength())
            text.setCaretPosition(offsetLimit);
    }

    // react to a typed key, ctrl enter is call to process command
    public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() == KeyEvent.VK_ENTER || (int)e.getKeyChar() == 13) {
            int mods = e.getModifiers();
            if (mods == KeyEvent.CTRL_MASK || mods == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
                // ctrl enter
                try {
                    String newCommand = text.getDocument().getText(offsetLimit,text.getDocument().getLength()-offsetLimit);
                    lastCommand.add(newCommand);
                    historyBrowsing = lastCommand.size();
                    command(newCommand);
                }
                catch (Exception excep) {}
            }
        }
    }

    // react to a released key, ctrl up or down arrow browses command history
    public void keyReleased(KeyEvent e) {
        int mods = e.getModifiers();
        if (mods == KeyEvent.CTRL_MASK || mods == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) {
            // only react to ctrl combination (or cmd on macs)
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                // keypad up
                try {
                    text.getDocument().remove(offsetLimit,text.getDocument().getLength()-offsetLimit);
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    StyleConstants.setForeground(sas, userColor);
                    if (historyBrowsing > 0) historyBrowsing = historyBrowsing - 1;
                    String histCommand = lastCommand.get(historyBrowsing);
                    text.getDocument().insertString(offsetLimit, histCommand, sas);
                }
                catch (Exception excep) {}
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                // keypad down
                try {
                    text.getDocument().remove(offsetLimit,text.getDocument().getLength()-offsetLimit);
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    StyleConstants.setForeground(sas, userColor);
                    if (historyBrowsing < lastCommand.size()) historyBrowsing = historyBrowsing + 1;
                    String histCommand = lastCommand.get(historyBrowsing);
                    text.getDocument().insertString(offsetLimit, histCommand, sas);
                }
                catch (Exception excep) {}
            }
        }
    }

    // required for interface
    public void keyPressed(KeyEvent e) {}

}
