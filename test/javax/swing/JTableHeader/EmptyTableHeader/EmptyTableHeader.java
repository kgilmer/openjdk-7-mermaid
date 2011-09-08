/*
 * Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 @test
 @summary <rdar://problem/3755801> 
 @summary Empty String causes Table Header height to be miscalculated
 @summary com.apple.junit.javax.swing.JTable.JTableHeader
 @run main EmptyTableHeader
 */

import junit.framework.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class EmptyTableHeader extends TestCase
{

    protected void setUp() {
        // called by JUnit before the actual testing
    }


    JTable fTable;
    JFrame fFrame;
    public void testEmptyHeader()
    {
        fFrame = new JFrame("Test Window");
        

        // Create a panel to hold all other components
        JPanel topPanel = new JPanel();
        topPanel.setLayout( new BorderLayout() );

        // Create a new table instance
        MyTableModel myModel = new MyTableModel();
        fTable = new JTable(myModel);

        // Add the table to a scrolling pane
        JScrollPane scrollPane = new JScrollPane( fTable );
        topPanel.add( scrollPane, BorderLayout.CENTER );
                
                                
        fFrame.getContentPane().setLayout(new BorderLayout());
        fFrame.getContentPane().add(BorderLayout.CENTER, topPanel);


        fFrame.setSize(400,450);
        fFrame.setLocation(20,20);
        fFrame.setVisible(true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        JTableHeader header = fTable.getTableHeader();
        assertTrue("JTableHeader greater than 5 pixels tall with empty string first element.", 
        header.getSize().height > 5);
        fFrame.setVisible(false);
        fFrame.dispose();
    }

    protected void tearDown() {
        fFrame.setVisible(false);
        fFrame.dispose();
    }


    class MyTableModel extends AbstractTableModel {
        final String[] columnNames = {"", 
                                      "Last Name",
                                      "Sport"};
        final Object[][] data = {
            {"Scott", "Adler", 
             "Snowboarding"}
        };

        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }



    }

    //-----------------------------------------------------------------------------
    //                  BOILERPLATE CODE FROM HERE ON DOWN
    //-----------------------------------------------------------------------------
    
    public static Test suite() {
        // This assembles a test suite. 
        // It will trigger all testXXX() calls in the specified class.
        return new TestSuite(EmptyTableHeader.class);
    }

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }
    
}
