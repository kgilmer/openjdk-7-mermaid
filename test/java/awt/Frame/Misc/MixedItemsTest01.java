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
 @summary Simple test of various awt controls
 @summary com.apple.junit.java.awt;
 @library ../../regtesthelpers
 @build VisibilityValidator
 @run main MixedItemsTest01
 */

// classes necessary for this test

import junit.framework.*;

import java.awt.*;

import test.java.awt.regtesthelpers.VisibilityValidator;

public class MixedItemsTest01 extends TestCase {

    // A panel with a little bit of everything...
    class BusyPanel extends Panel {
        class ExtendedButton extends Button {
            public ExtendedButton( String name ) {
                super();
                setLabel(  name + " " + this.getClass().getName() );
            }
        }
        
        class ExtendedCanvas extends Canvas {
            protected String name;

            public ExtendedCanvas( String name ) {
                super();
                this.name = name;
            }
        
            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                g.setColor( Color.red );
                g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
                FontMetrics fm = g.getFontMetrics();
                int ascent = fm.getAscent();
                g.drawString(name + " " + this.getClass().getName(), 4, ascent+4);
            }
        }
        
        class ExtendedCheckBox extends Checkbox {
            public ExtendedCheckBox( String name ) {
                super();
                setLabel(  name + " " + this.getClass().getName() );
            }
        }
        
        class ExtendedChoice extends Choice {
            public ExtendedChoice( String name ) {
                super();
                this.add(  "a" + " " + name );
                this.add(  "b" + " " + this.getClass().getName() );
                this.add(  "c" );
                this.add(  "e" );
                this.add(  "f" );
            }
        }
        
        class ExtendedLabel extends Label {
            public ExtendedLabel( String name ) {
                super();
                setText(  name + " " + this.getClass().getName() );
            }
        }
        
        class ExtendedList extends List {
            public ExtendedList( String name ) {
                super();
                this.add(  "a" + " " + name );
                this.add(  "b" + " " + this.getClass().getName() );
                this.add(  "c" );
                this.add(  "e" );
                this.add(  "f" );
            }
        }
        
        class ExtendedComponent extends Component {
            protected String name;
            
            public ExtendedComponent( String name ) {
                super();
                this.name = name;
            }
        
            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                g.setColor( Color.red );
                g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
                FontMetrics fm = g.getFontMetrics();
                int ascent = fm.getAscent();
                g.drawString(name + " " + this.getClass().getName(), 4, ascent+4);
            }
        }
        
        class ExtendedPanel extends Panel {
            protected String name;
            
            public ExtendedPanel( String name ) {
                super();
                this.name = name;
            }
        
            public void paint( Graphics g ) {
                super.paint( g );
                Dimension dim = getSize();
                g.setColor( Color.red );
                g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
                FontMetrics fm = g.getFontMetrics();
                int ascent = fm.getAscent();
                g.drawString(name + " " + this.getClass().getName(), 4, ascent+4);
            }
        }
        
        class ExtendedScrollBar extends Scrollbar {
            public ExtendedScrollBar( String name ) {
                super();
                setName(name);
                // setLabel(  name + " " + this.getClass().getName() );
            }
        }
        
        class ExtendedScrollPane extends ScrollPane {
            class SPCanvas extends Canvas {
                protected String name;
    
                public SPCanvas( String name ) {
                    super();
                    this.name = name;
                }
            
                public Dimension getPreferredSize() {
                    return new Dimension(500,500);
                }
                
                public void paint( Graphics g ) {
                    super.paint( g );
                    Dimension dim = getSize();
                    g.setColor( Color.red );
                    g.drawRect( 0, 0, dim.width - 1, dim.height - 1 );
                    FontMetrics fm = g.getFontMetrics();
                    int ascent = fm.getAscent();
                    g.drawString(name + " " + this.getParent().getClass().getName(), 4, ascent+4);
                }
            }

            public ExtendedScrollPane( String name ) {
                super(SCROLLBARS_ALWAYS);
                add( new SPCanvas(name) );
            }
        }
        
        class ExtendedTextArea extends TextArea {
            public ExtendedTextArea( String name ) {
                super();
                setText(  name + " " + this.getClass().getName() );
            }
        }
        
        class ExtendedTextField extends TextField {
            public ExtendedTextField( String name ) {
                super();
                setText(  name + " " + this.getClass().getName() );
            }
        }
    
    
        public BusyPanel( String name ) {
            int num_objects = 12;
            setLayout( new GridLayout( num_objects/4 + 1, 4, 5, 5 ) );
            for (int i = 0; i < num_objects; i++) {
                switch (i % num_objects) {
                    case  0:  add( new ExtendedButton(name + " " + i) );        break;
                    case  1:  add( new ExtendedCanvas (name + " " + i) );        break;
                    case  2:  add( new ExtendedCheckBox (name + " " + i) );    break;
                    case  3:  add( new ExtendedChoice (name + " " + i) );        break;
                    case  4:  add( new ExtendedComponent (name + " " + i) );    break;
                    case  5:  add( new ExtendedLabel (name + " " + i) );        break;
                    case  6:  add( new ExtendedList (name + " " + i) );        break;
                    case  7:  add( new ExtendedPanel (name + " " + i) );        break;
                    case  8:  add( new ExtendedScrollBar (name + " " + i) );    break;
                    case  9:  add( new ExtendedScrollPane (name + " " + i) );    break;
                    case 10:  add( new ExtendedTextArea (name + " " + i) );    break;
                    case 11:  add( new ExtendedTextField (name + " " + i) );    break;
                }
            }
        }
        
    }

    public void testMixedComponents() throws Exception {
        Frame frame = null;
        // Thread.currentThread().setName( "testMixedComponents" );
        for (int i = 0; i < 10; i++) {
            try {
                // Bring up a test frame
                frame = new Frame( "BusyFrame "  + i);
                assertNotNull( frame);
                BusyPanel panel = new BusyPanel("Test");
                frame.add( panel );
                frame.pack();

            /*
                Dimension d = frame.getSize();
                System.out.print( d.width + "\t" + d.height );
            */

                VisibilityValidator.setVisibleAndConfirm(frame);
                pause(250);

            /*
                d = frame.getSize();
                System.out.println("\t\t" + d.width + "\t" + d.height );
            */

                frame.dispose();
                frame = null;
            }
            finally {
                if (frame != null) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
        }
    }

    //
    //    Boilerplate from here on down
    //

    public static void pause( int duration ) {
        try {
            Thread.sleep( duration );
        }
        catch (Throwable t) {
        }
    }

    public static Test suite() {
        return new TestSuite( MixedItemsTest01.class);
    }

    public static void main( String[] args ) {
        junit.textui.TestRunner.run( suite() );
    }
}
