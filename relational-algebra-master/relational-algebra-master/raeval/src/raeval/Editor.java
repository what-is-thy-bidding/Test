/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package raeval;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.io.*;



// helper class sheetPanel
// implements a panel containing a relation as a spreedsheet
class SheetPanel extends JPanel {

    Relation showRelation;
    String [][] sheet;
    int sheetCols;
    int sheetRows;
    int rowFocus;
    int colFocus;
    boolean focusSet;
    Editor owningEditor;
    CustomTable sheetTable;

    // inner class
    // supports sheet cells having highlights to show selection
    class CustomRenderer extends DefaultTableCellRenderer {

        int lastRow = -1;
        int lastCol = -1;
        boolean focusComeBack = true;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
            super.getTableCellRendererComponent(table,value,isSelected,hasFocus,rowIndex,vColIndex);
            if (value != null) setText(value.toString()); else setText("");
            if (hasFocus) {
                CustomTable cTable = (CustomTable)table;
                cTable.getOwner().handleFocus(rowIndex,vColIndex);
            }
            if (hasFocus) {
                lastRow = rowIndex;
                lastCol = vColIndex;
            }
            setOpaque(true);
            if (!table.hasFocus()) {
                focusComeBack = true;
                if (rowIndex == lastRow && vColIndex == lastCol) {
                    setBackground(new Color(255,102,102));
                } else {
                    if (vColIndex == 0 || (rowIndex+1) == table.getRowCount()) {
                        setBackground(new Color(240,240,240));
                    } else {
                        if (rowIndex == 0 || rowIndex == 1) setBackground(new Color(153,204,255)); else setBackground(new Color(204,204,204));
                    }
                }
            } else {
                if (focusComeBack) {
                    focusComeBack = false;
                    CustomTable cTable = (CustomTable)table;
                    if (cTable.owner != null) cTable.owner.forceRepaintSheet();
                }
                if (vColIndex == 0 || (rowIndex+1) == table.getRowCount()) {
                    setBackground(new Color(240,240,240));
                } else {
                    if (hasFocus) {
                        setBackground(new Color(255,102,102));
                    } else {
                        if (rowIndex == 0 || rowIndex == 1) setBackground(new Color(153,204,255)); else setBackground(new Color(204,204,204));
                    }
                }
            }
            if (vColIndex == 0 || (rowIndex+1) == table.getRowCount()) {
                setFont(new Font("Arial",Font.PLAIN,12));
            } else {
                setFont(new Font("Arial",Font.BOLD,14));
            }
            return this;
        }
    }

    // inner class
    class CustomTableModel extends DefaultTableModel {
        public CustomTableModel() {
            super();
        }
        public int getColumnCount() { return super.getColumnCount(); }
        public int getRowCount() { return super.getRowCount(); }
        public Object getValueAt(int row, int col) { return super.getValueAt(row, col); }
        public boolean isCellEditable(int row, int col) {
            if ((row+1) == getRowCount()) return (col==1)?true:false;
            return (col==0)?false:true;
        }
    }

    // inner class
    class CustomTable extends JTable {
        SheetPanel owner;
        public CustomTable() {
            super(new CustomTableModel());
        }
        public SheetPanel getOwner() { return owner; }
    }

    // constructor
    public SheetPanel(Relation editRelation, Editor owner) {
        super(new BorderLayout());
        owningEditor = owner;
        setSize(owningEditor.superPanel.getSize());
        showRelation = editRelation;
        String[] attribs = new String[showRelation.degree()];
        sheetCols = showRelation.degree() + 1;
        sheetRows = showRelation.cardinality() + 3;
        sheet = new String[sheetRows][sheetCols];
        String[] colHeaders = new String[1+showRelation.degree()];
        String[][] data = new String[3+showRelation.cardinality()][1+showRelation.degree()];
        for (int i=-1; i<showRelation.degree(); i++) {
            if (i == -1) {
                colHeaders[0] = "Relation";
                data[0][0] = "Attribute";
                data[1][0] = "Domain";
            } else {
                colHeaders[i+1] = String.valueOf(i+1);
                attribs[i] = showRelation.attributeName(i);
                data[0][i+1] = attribs[i];
                data[1][i+1] = showRelation.domainForAttribute(attribs[i]);
            }

        }
        for (int i=0; i<showRelation.cardinality(); i++) {
            for (int j=-1; j<showRelation.degree(); j++) {
                if (j == -1) {
                    data[2+i][0] = "Tuple " + String.valueOf(i+1);
                } else {
                    data[2+i][j+1] = showRelation.valueForTuple(i, attribs[j]);
                    System.out.println("row " + i + "column " + j + " is " + data[i][j]);
                }
            }
        }
        for (int i=-1; i<showRelation.degree(); i++) {
            if (i == -1) data[2+showRelation.cardinality()][0] = "New Tuple";
            if (i == 0)  data[2+showRelation.cardinality()][1] = "<<<Click>>>";
            if (i > 0)   data[2+showRelation.cardinality()][i+1] = "";
        }
        // set up the table
        sheetTable = new CustomTable();
        CustomTableModel sheetModel = (CustomTableModel) sheetTable.getModel();
        sheetModel.setColumnCount(sheetCols);
        sheetModel.setRowCount(sheetRows);
        for (int i=0; i<sheetRows; i++)
            for (int j=0; j<sheetCols; j++) {
                sheet[i][j] = data[i][j];
                sheetModel.setValueAt(sheet[i][j], i, j);
            }        
        sheetTable.owner = this;
        sheetTable.setRowHeight(24);
        sheetTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        for (int i=0; i<sheetTable.getColumnModel().getColumnCount(); i++) {
            TableColumn col = sheetTable.getColumnModel().getColumn(i);
            CustomRenderer customRender = new CustomRenderer();
            col.setCellRenderer(customRender);
        }
        sheetTable.setAutoCreateColumnsFromModel(false);
        sheetTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JScrollPane scrollPane = new JScrollPane(sheetTable);
        sheetTable.setTableHeader(null);
        scrollPane.setColumnHeaderView(null);
        this.add(scrollPane,BorderLayout.CENTER);
        scrollPane.setSize(owningEditor.superPanel.getSize());
        scrollPane.setSize(600,500);
        scrollPane.setVisible(true);
        focusSet = false;
    }

    public void forceRepaintSheet() {
        if (sheetTable != null) sheetTable.repaint();
    }

    // update for change in table cell getting focus
    public void handleFocus(int row, int col) {
        rowFocus = row;
        colFocus = col;
        focusSet = true;
        if (colFocus == 1 && (rowFocus+1) == sheetTable.getRowCount()) {
            // click for new tuple
            newRow(rowFocus);
        }
        int menuMask = 0;
        if (colFocus == 0 || (rowFocus+1) == sheetTable.getRowCount()) {
            menuMask = 0;
        } else {
            menuMask = menuMask | 1;
            menuMask = menuMask | 2;
            if (rowFocus > 1) menuMask = menuMask | 4;
            if (rowFocus > 0) menuMask = menuMask | 8;
        }
        if (owningEditor != null) owningEditor.setMenuAvailability(menuMask);
        System.out.println("focus: " + col + ", " + row);
    }

    // return the row with current focus
    public int getFocusRow() { return rowFocus; }

    // return the col with current focus
    public int getFocusCol() { return colFocus; }

    // add a new row into the table
    public void newRow(int insertIndex) {
        CustomTableModel sheetModel = (CustomTableModel)sheetTable.getModel();
        Object[] newData = new String[sheetModel.getColumnCount()];
        for (int i=0; i<sheetModel.getColumnCount(); i++) {
            newData[i] = "";
        }
        System.out.println("Insert row: " + insertIndex);
        sheetModel.insertRow(insertIndex, newData);
        int tCount = 0;
        for (int i=0; i<sheetModel.getRowCount(); i++) {
            if (i>1 && (i+1)<sheetModel.getRowCount()) {
                tCount++;
                String tupleStr = "Tuple " + tCount;
                sheetTable.setValueAt(tupleStr, i, 0);
            }
        }
        sheetModel.fireTableRowsInserted(insertIndex, insertIndex);
        sheetTable.changeSelection(insertIndex, 1, false, false);
    }

    // add a new column into the table
    public void newCol(int insertIndex) {
        CustomTableModel sheetModel = (CustomTableModel)sheetTable.getModel();
        Object[] newData = new String[sheetModel.getRowCount()];
        for (int i=0; i<sheetModel.getRowCount(); i++) {
            newData[i] = "";
        }
        TableColumn col = new TableColumn(sheetModel.getColumnCount());
        col.setHeaderValue("");
        col.setCellRenderer(new CustomRenderer());
        sheetTable.addColumn(col);
        sheetModel.addColumn("",newData);
        sheetTable.moveColumn(sheetTable.getColumnCount()-1, insertIndex);
        sheetModel.fireTableStructureChanged();
    }

    // delete a row from the table
    public void delRow(int delIndex) {
        CustomTableModel sheetModel = (CustomTableModel)sheetTable.getModel();
        sheetModel.removeRow(delIndex);
    }

    // delete a column from the table
    public void delCol(int delIndex) {
        CustomTableModel sheetModel = (CustomTableModel)sheetTable.getModel();
        TableColumn col = sheetTable.getColumnModel().getColumn(delIndex);
        sheetTable.removeColumn(col);
        sheetModel.fireTableStructureChanged();
    }

    // check to see if this is a valid relation - if not produce a dialog
    // valid - no blank attributes, no blank domains, no blank values
    // duplicate tuples are accepted but removed later
    public boolean validForAccept() {
        CustomTableModel sheetModel = (CustomTableModel)sheetTable.getModel();
        boolean anyErrors = false;
        boolean errorAttr = false;
        boolean errorAttrRepeat = false;
        boolean errorDomain = false;
        boolean errorValue = false;
        int errRow = -1;
        int errCol = -1;
        for (int i=0; i<sheetModel.getRowCount(); i++) {
            for (int j=1; j<sheetModel.getColumnCount(); j++) {
                String content = (String)sheetModel.getValueAt(i, j);
                if (i == 0) {
                    for (int k=1; k<sheetModel.getColumnCount(); k++) {
                        if (j != k) {
                            String checkContent = (String)sheetModel.getValueAt(i,k);
                            if (content.matches(checkContent)) {
                                anyErrors = true;
                                errorAttrRepeat = true;
                            }
                        }
                    }
                }
                if (content.length() == 0 && (i+1)<sheetModel.getRowCount()) {
                    anyErrors = true;
                    if (i == 0) errorAttr = true;
                    if (i == 1) errorDomain = true;
                    if (i > 1) errorValue = true;
                    if (errRow < 0) {
                        errRow = i;
                        errCol = j;
                    }
                }
            }
        }
        if (anyErrors) {
            String message = "";
            if (errorAttrRepeat) message = message + "Attribute names cannot be repeated\n";
            if (errorAttr) message = message + "Empty attribute name not allowed\n";
            if (errorDomain) message = message + "Empty domain name not allowed\n";
            if (errorValue) message = message + "Empty value not allowed\n";
            JOptionPane.showMessageDialog(sheetTable, message, "Cannot accept", JOptionPane.ERROR_MESSAGE);
        }
        return !anyErrors;
    }

    // create a relation from the current data model including amendments
    public Relation relationFromData() {
        Relation newRel = new Relation();
        CustomTableModel sheetModel = (CustomTableModel)sheetTable.getModel();
        for (int i=0; (i+1)<sheetModel.getRowCount(); i++) {
            RelationTuple tuple = null;
            if (i >= 2) tuple = new RelationTuple(newRel);
            for (int j=1; j<sheetModel.getColumnCount(); j++) {
                if (i == 0) {
                    String attrName = (String)sheetModel.getValueAt(i, j);
                    String domnName = (String)sheetModel.getValueAt(i+1, j);
                    newRel.addAttribute(attrName, domnName);
                }
                if (i >= 2) {
                    String attrName = (String)sheetModel.getValueAt(0,j);
                    String value = (String)sheetModel.getValueAt(i,j);
                    tuple.setValue(attrName, value);
                }
            }
            if (i >= 2) newRel.addTuple(tuple);
        }
        newRel.removeDuplicateTuples();
        return newRel;
    }

    // force a stop to editing - e.g. to commit current edit if save triggered
    public void stopEditing() {
        TableCellEditor editor = sheetTable.getCellEditor();
        if (editor != null) editor.stopCellEditing();
    }
}

/**
 *
 * @author nick
 */
public class Editor extends JFrame implements ActionListener {

    UserInterface parentUI;
    Relation editRelation;
    String editName;
    String saveFileName;
    JLabel nameLabel;
    JMenuItem ialMenuItem;
    JMenuItem iarMenuItem;
    JMenuItem itaMenuItem;
    JMenuItem itbMenuItem;
    JMenuItem delAttrMenuItem;
    JMenuItem delTupleMenuItem;
    JMenuItem saveMenuItem;
    SheetPanel sheetPanel = null;
    JPanel superPanel;

    // process menu actions
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().startsWith("Cancel") || e.getActionCommand().startsWith("Close")) {
            boolean carryOut = true;
            if (e.getActionCommand().startsWith("Close")) {
                Object[] options = {"Yes", "No"};
                int n = JOptionPane.showOptionDialog(this, "Lose changes?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (n != 0) carryOut = false;
            }
            if (carryOut) {
                parentUI.hideEditor(this, false);
                this.dispose();
            }
        }
        if (e.getActionCommand().startsWith("Accept")) {
            if (sheetPanel.validForAccept()) {
                Relation newReln = sheetPanel.relationFromData();
                if (Relation.definedNameExists(editName)) Relation.definedRemove(editName);
                Relation.assign(editName, newReln);
                parentUI.hideEditor(this,true);
                this.dispose();
            }
        }
        if (e.getActionCommand().startsWith("Insert Tuple Above")) {
            sheetPanel.newRow(sheetPanel.getFocusRow());
        }
        if (e.getActionCommand().startsWith("Insert Tuple Below")) {
            sheetPanel.newRow(sheetPanel.getFocusRow()+1);
        }
        if (e.getActionCommand().startsWith("Insert Attribute Left")) {
            sheetPanel.newCol(sheetPanel.getFocusCol());
        }
        if (e.getActionCommand().startsWith("Insert Attribute Right")) {
            sheetPanel.newCol(sheetPanel.getFocusCol()+1);
        }
        if (e.getActionCommand().startsWith("Delete Attribute")) {
            sheetPanel.delCol(sheetPanel.getFocusCol());
        }
        if (e.getActionCommand().startsWith("Delete Tuple")) {
            sheetPanel.delRow(sheetPanel.getFocusRow());
        }
        if (e.getActionCommand().startsWith("Open File...")) {
            int retVal = UserInterface.fileChooser.showOpenDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = UserInterface.fileChooser.getSelectedFile();
                String filename = "";
                try {
                    filename = file.getCanonicalPath();
                }
                catch (Exception excep) {}
                System.out.println("open " + filename);
                Relation newReln = new Relation();
                newReln = newReln.loadCSV(filename);
                Relation.definedRemove(editName);
                Relation.assign(editName, newReln);
                saveFileName = filename;
                saveMenuItem.setEnabled(true);
                superPanel.remove(sheetPanel);
                sheetPanel = new SheetPanel(newReln,this);
                sheetPanel.owningEditor = this;
                sheetPanel.setVisible(true);
                this.superPanel.add(sheetPanel,BorderLayout.CENTER);
                this.superPanel.revalidate();
            }
        }
        boolean runIntoSaveAs = false;
        if (e.getActionCommand().startsWith("Save") && !e.getActionCommand().startsWith("Save As...")) {
            runIntoSaveAs = true;
            sheetPanel.stopEditing();
        }
        if (e.getActionCommand().startsWith("Save As...") || runIntoSaveAs) {
            sheetPanel.stopEditing();
            String filename = "";
            if (runIntoSaveAs) {
                filename = saveFileName;
            } else {
                int retVal = UserInterface.fileChooser.showSaveDialog(this);
                if (retVal == JFileChooser.APPROVE_OPTION) {
                    File file = UserInterface.fileChooser.getSelectedFile();
                    try {
                        filename = file.getCanonicalPath();
                    }
                    catch (Exception excep) {System.out.println("emsg:"+excep.getMessage());}
                }
            }
            Relation tempRelation = sheetPanel.relationFromData();
            System.out.println("svf: "+filename);
            tempRelation.saveCSV(filename);
            saveFileName = filename;
            saveMenuItem.setEnabled(true);
        }
    }

    // menu option availability depends on position in sheet
    public void setMenuAvailability(int flags) {
        if ((flags & 1) == 1) {
            ialMenuItem.setEnabled(true);
            delAttrMenuItem.setEnabled(true);
        } else {
            ialMenuItem.setEnabled(false);
            delAttrMenuItem.setEnabled(false);
        }
        if ((flags & 2) == 2) iarMenuItem.setEnabled(true); else iarMenuItem.setEnabled(false);
        if ((flags & 4) == 4) {
            itaMenuItem.setEnabled(true);
            delTupleMenuItem.setEnabled(true);
        } else {
            itaMenuItem.setEnabled(false);
            delTupleMenuItem.setEnabled(false);
        }
        if ((flags & 8) == 8) itbMenuItem.setEnabled(true); else itbMenuItem.setEnabled(false);
    }

    // constructor
    public Editor(String relName, UserInterface parent) {
        super("Editing: " + relName);
        parentUI = parent;
        editName = relName;
        saveFileName = "";
        // build the menu
        JMenuBar editorMenuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        editorMenuBar.add(fileMenu);
        JMenuItem openMenuItem = new JMenuItem("Open File...");
        openMenuItem.setMnemonic(KeyEvent.VK_O);
        openMenuItem.addActionListener(this);
        fileMenu.add(openMenuItem);
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setMnemonic(KeyEvent.VK_S);
        saveMenuItem.setEnabled(false);
        saveMenuItem.addActionListener(this);
        fileMenu.add(saveMenuItem);
        JMenuItem saveAsMenuItem = new JMenuItem("Save As...");
        saveAsMenuItem.setMnemonic(KeyEvent.VK_V);
        saveAsMenuItem.addActionListener(this);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        JMenuItem closeMenuItem = new JMenuItem("Close");
        closeMenuItem.addActionListener(this);
        fileMenu.add(closeMenuItem);
        JMenu insertMenu = new JMenu("Insert");
        insertMenu.setMnemonic(KeyEvent.VK_I);
        editorMenuBar.add(insertMenu);
        ialMenuItem = new JMenuItem("Insert Attribute Left");
        ialMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,ActionEvent.CTRL_MASK));
        ialMenuItem.setMnemonic(KeyEvent.VK_L);
        ialMenuItem.addActionListener(this);
        insertMenu.add(ialMenuItem);
        iarMenuItem = new JMenuItem("Insert Attribute Right");
        iarMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
        iarMenuItem.setMnemonic(KeyEvent.VK_R);
        iarMenuItem.addActionListener(this);
        insertMenu.add(iarMenuItem);
        itaMenuItem = new JMenuItem("Insert Tuple Above");
        itaMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,ActionEvent.CTRL_MASK));
        itaMenuItem.setMnemonic(KeyEvent.VK_A);
        itaMenuItem.addActionListener(this);
        insertMenu.add(itaMenuItem);
        itbMenuItem = new JMenuItem("Insert Tuple Below");
        itbMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,ActionEvent.CTRL_MASK));
        itbMenuItem.setMnemonic(KeyEvent.VK_B);
        itbMenuItem.addActionListener(this);
        insertMenu.add(itbMenuItem);
        JMenu deleteMenu = new JMenu("Delete");
        deleteMenu.setMnemonic(KeyEvent.VK_D);
        editorMenuBar.add(deleteMenu);
        delAttrMenuItem = new JMenuItem("Delete Attribute");
        delAttrMenuItem.setMnemonic(KeyEvent.VK_A);
        delAttrMenuItem.addActionListener(this);
        deleteMenu.add(delAttrMenuItem);
        delTupleMenuItem = new JMenuItem("Delete Tuple");
        delTupleMenuItem.setMnemonic(KeyEvent.VK_T);
        delTupleMenuItem.addActionListener(this);
        deleteMenu.add(delTupleMenuItem);
        this.setJMenuBar(editorMenuBar);
        setMenuAvailability(0);
        // build the rest of the gui
        editRelation = Relation.referenceForRelation(relName);
        setSize(600,600);
        setBackground(Color.gray);
        superPanel = new JPanel(new BorderLayout());
        superPanel.setSize(600,600);
        superPanel.setBackground(new Color(240,240,240));
        sheetPanel = new SheetPanel(editRelation,this);
        sheetPanel.owningEditor = this;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setSize(600,80);
        JButton acceptButton = new JButton("Accept");
        JButton cancelButton = new JButton("Cancel");
        acceptButton.addActionListener(this);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);
        buttonPanel.add(acceptButton);
        superPanel.add(sheetPanel,BorderLayout.CENTER);
        superPanel.add(buttonPanel,BorderLayout.SOUTH);
        sheetPanel.setVisible(true);
        buttonPanel.setVisible(true);
        this.getContentPane().add(superPanel,BorderLayout.CENTER);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                triggerHideForWindowClosing();
            }
        });
    }

    // if window close icon is clicked, close gracefully
    public void triggerHideForWindowClosing() {
        parentUI.hideEditor(this, false);
    }

}
