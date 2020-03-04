/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package raeval;

import javax.swing.*;

/**
 *
 * @author nickeveritt
 */
public class Raeval {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // create the user interface
        Relation.referencesInit();
        UserInterface frame = new UserInterface();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.showMessage();
    }
}
