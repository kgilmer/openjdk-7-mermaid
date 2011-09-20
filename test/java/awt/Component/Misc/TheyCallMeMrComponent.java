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

/**
 @test
 @summary A simple stress test
 @summary com.apple.junit.java.awt;
 @run main TheyCallMeMrComponent
 */

// classes necessary for this test

import junit.framework.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TheyCallMeMrComponent extends TestCase {
    
    public void testMrComponent() throws Exception {
        MrComponent a = null;
        try {
            a = new MrComponent();
            a.init();
            a.setSize(200, 200);
            a.setVisible(true);
            
            Thread.sleep(1000);
            
            final MrComponent c = a;
            
            SwingUtilities.invokeLater(new Runnable() { public void run() { c.toggleStart(); } });
            
            Thread.sleep(5000);
            
            SwingUtilities.invokeLater(new Runnable() { public void run() { c.toggleStart(); } });
            
            Thread.sleep(1000);
            
            SwingUtilities.invokeLater(new Runnable() { public void run() { c.stopThreads(); } });

            Thread.sleep(1000);

        } finally {
            if (a != null) {
                a.setVisible(false);
                a.dispose();
                a = null;
            }
        }
    }
    
    public static Test suite() {
        return new TestSuite( TheyCallMeMrComponent.class);
    }
    
    public static void main( String[] args ) {
        junit.textui.TestRunner.run( suite() );
    }
}

class MrComponent extends Frame {
    
    static final Object fLock = new Object();
    Color    fColors[] = {Color.blue, Color.red, Color.green};
    int        fColorIndex = 0;
    
    Panel    fPanel;
    
    Button    fToggleStart;
    volatile boolean fRunning = false;
    volatile boolean fShouldStop = false;
    static final String sStartString= "Start";
    static final String sStopString = "Stop";
    
    public void init()
    {
        this.setLayout(new BorderLayout());
        
        fToggleStart = new Button(sStartString);
        this.add(BorderLayout.NORTH, fToggleStart);
        
        fToggleStart.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e)
        {
                toggleStart();
        }
        });
        
        fPanel = new Panel();
        this.add(BorderLayout.CENTER, fPanel);
        
        Painter p = new Painter();
        ComponentMucker m = new ComponentMucker();
        p.start();
        m.start();
    }
    
    
    public void paint (Graphics g) {
        synchronized (fLock)
    {
            g.setColor(fColors[fColorIndex]);
            fColorIndex++;
            if (fColorIndex >= fColors.length)
            {
                fColorIndex = 0;
            }
            fPanel.setBackground(fColors[fColorIndex]);
            Rectangle r = getBounds();    
            // draw an X
            g.drawLine(0, 0, r.width, r.height);
            g.drawLine(r.width, 0, 0, r.height);
            
            super.paint(g);
    }
    }
    
    
    
    private class Painter extends Thread
    {
        public void run()
    {
            setPriority(Thread.MIN_PRIORITY);
            
            while(!fShouldStop)
            {
                repaint();
                yield();
            }
    }
    }
    
    private class ComponentMucker extends Thread
    {
        public void run() {
            setPriority(Thread.MIN_PRIORITY);
            
            while(!fShouldStop) {
                muckWithComponents();
                yield();
            }
        }
    }
    
    public void toggleStart()
    {
        fRunning = !fRunning;
        fToggleStart.setLabel((fRunning ? sStopString : sStartString));
    }
    
    public void stopThreads() {
        fShouldStop = true;
    }
    
    int count = 0;
    boolean fUp = true;
    String[] names = {"one", "two", "three", "four", "five", "crazy","seven","eight","nine","happy","scratcho","binky","tickles"};
    
    public void muckWithComponents()
    {
        if (fRunning) {
            synchronized (fLock) {
                try {
                    //Thread.sleep(1000);
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
                
                if (count < names.length) {
                    fPanel.add(new Label(names[count]));
                    count++;
                }
                else
                {
                    count = 0;
                    fPanel.removeAll();
                }
                validate();
                repaint();
            }
        }
    }
}
