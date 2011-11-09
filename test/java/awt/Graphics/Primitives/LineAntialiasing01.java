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
 * @test
 * @summary This test draws lines and probes points just next to the line to see if it has been aliased correctly.
 * @summary com.apple.junit.java.graphics.primitives
 * @library ../../regtesthelpers
 * @build Waypoint
 * @run main LineAntialiasing01
 */

import test.java.awt.regtesthelpers.Waypoint;
import junit.framework.*;

import java.awt.*;


public class LineAntialiasing01 extends TestCase {

	static final int kPaintTimeOut = 500;

	public static Test suite() {
		return new TestSuite(LineAntialiasing01.class);
	}
	
	protected void setUp() throws AWTException {
		// set up the kRobot for grabbing colors
		if (kRobot == null) {
			kRobot = new Robot();
		}

		// Set up probe points just off the test lines
		if (kProbePoints == null) {
			kProbePoints = new Point[ kRects.length * 2];

			for (int i = 0; i < kRects.length; i++ ) {
				int midW = (int) Math.rint( kRects[i].width/2.0);
				int midH = (int) Math.rint( kRects[i].height/2.0);
				
				if (kRects[i].width > kRects[i].height) {
					kProbePoints[2*i] = new Point( kRects[i].x+midW+1, kRects[i].y+midH );
					kProbePoints[2*i+1] = new Point( kRects[i].x+midW, kRects[i].y+midH+1 );
				}
				else {
					kProbePoints[2*i] = new Point( kRects[i].x+midW-1, kRects[i].y+midH );
					kProbePoints[2*i+1] = new Point( kRects[i].x+midW, kRects[i].y+midH-1 );
				}
			}
		}
	}

 	class TestWindow extends Window {
		Object antialiasing;
 		Waypoint painted;
 		
 		public TestWindow( Frame f, Waypoint painted, Object antialiasing) {
			super(f);
			this.antialiasing = antialiasing;
			this.painted = painted;
 		}
 	
		// Rectangle r = new Rectangle(5,5,256,64);
		Rectangle r = new Rectangle(2,2,30,40);

		public void paint(Graphics g) {
			g.setColor(Color.white);
			g.fillRect( 0, 0, kWidth, kWidth );

			g.setColor(Color.black);
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, antialiasing);

			// Draw the test lines
			int x0,x1,y0,y1;
			for (int i = 0; i < kRects.length; i++ ) {
				x0 = kRects[i].x;
				x1 = kRects[i].x+kRects[i].width;
				y0 = kRects[i].y;
				y1 = kRects[i].y+ kRects[i].height;
				g.drawLine( x0, y0, x1, y1 );
			}

			
			// For debugging -- this turns the probe points blue
			/*
			for (int i = 0; i < kProbePoints.length; i++ ) {
				g.setColor( Color.blue);
				g.fillRect( kProbePoints[i].x, kProbePoints[i].y, 1, 1 );
			}
			*/

			painted.clear();	// paint has been called at least once, now

		}
 	}

	public void testDrawLinesAWT() throws Exception {

		Frame dummy = new Frame();
		Window[] windows = new Window[3];
		Waypoint[] painted = new Waypoint[windows.length];

		for (int i = 0 ; i< 3; i++) {

			painted[i] = new Waypoint();	// this gets cleared in the paint method of the TestWindow, so we know that at least on paint() call has been made
			
			// create the test windows
			switch( i) {
				case 0: windows[i] = new TestWindow(dummy, painted[i], RenderingHints.VALUE_ANTIALIAS_ON );      break;
				case 1: windows[i] = new TestWindow(dummy, painted[i], RenderingHints.VALUE_ANTIALIAS_OFF );     break;
				case 2: windows[i] = new TestWindow(dummy, painted[i], RenderingHints.VALUE_ANTIALIAS_DEFAULT ); break;
				default: 
					throw new Exception("Should not get here!");
			
			}
			
			windows[i].setBounds(50 + (i * (kWidth + 5)), 50, kWidth,KHeight);
			// VisibilityValidator checkpoint = new VisibilityValidator(windows[i]); // Re-add this when VisiblityValidator handes windows (currently only handles frames)
			windows[i].setVisible(true);

			// checkpoint.requireVisible();
			// assertTrue( "Could not confirm test window was visible", checkpoint.isValid() );
			
			Thread.sleep(kPaintTimeOut);
			
			painted[i].requireClear();
			assertTrue( "paint() not called on test window after " + kPaintTimeOut + "ms" , painted[i].isClear() );
			
			// dumpRedsFromImage(w);	// <-- turn this on for debugging info

			for (int ii = 0; ii<kProbePoints.length; ii++) {
			
				// Collect the colors of points just off the test line
				Point screen_offset = windows[i].getLocationOnScreen();
				int x = screen_offset.x + kProbePoints[ii].x;	
				int y = screen_offset.y + kProbePoints[ii].y; 	
				Color fProbeColor= kRobot.getPixelColor(x, y);

				assertNotNull( fProbeColor );
				switch( i) {
					// RenderingHints.VALUE_ANTIALIAS_ON
					case 0: 
						assertTrue("Point just off line with ANTIALIAS_ON should not be white", Color.white.equals(fProbeColor) == false);
						break;

					// RenderingHints.VALUE_ANTIALIAS_OFF
					case 1: 
						assertEquals("Point just off line with ANTIALIAS_OFF should be white", Color.white, fProbeColor);
						break;

					// RenderingHints.VALUE_ANTIALIAS_DEFAULT
					case 2: 
						assertEquals("For non-Aqua, point just off line with VALUE_ANTIALIAS_DEFAULT should be white", Color.white, fProbeColor);
						break;
					default: 
						throw new Exception("Should not get here!");
				
				}
			}

		}	
		
		// make sure the window is up and drawn once
		Thread.sleep(100); // <-- do asserts here
		
		for (int i = 0 ; i< 3; i++) {
			windows[i].dispose();
		}
		
		dummy.dispose();
		
	}


	// ------------- DEBUGGING UTILITIES ----------------
	
	
	public static void dumpRedsFromImage( Window w)
	{
		Point offset = w.getLocationOnScreen();

		System.err.println("");
		System.err.println("");
		System.err.println("");
		
		for (int y = 0; y < w.getHeight(); y++)
		{
			System.err.print("  ");
			for (int x = 0; x < w.getWidth(); x++)
			{
				Color rgb = kRobot.getPixelColor(offset.x+x, offset.y+ y);
				int r = rgb.getRed();
				String c = (r == 0xff ? "X" : (r == 0 ? "." : "?"));
				System.err.print(c + " ");
				// System.err.print( r + " ");
			}
			System.err.println("");
		}
	}
		
	// ------------- JUnit Boilerplate ----------------

    public static void main (String[] args) throws RuntimeException {
        TestResult tr = junit.textui.TestRunner.run(suite());
        if ((tr.errorCount() != 0) || (tr.failureCount() != 0)) {
            throw new RuntimeException("### FAILED: unexpected JUnit errors or failures.");
        }
    }

		
	// -------------  ----------------


	static final int 		kWidth  = 30;
	static final int 		KHeight = 30;
	static final Rectangle	KRect = new Rectangle(0,0,kWidth,KHeight);

	// Some relatively mundane rectangles for use drawing test lines
	static final Rectangle[] kRects = {
		new Rectangle(2,2,26,26), 
	};

	protected Point[] kProbePoints = {
		new Point(14,15),
		new Point(15,14)
	};

	// Some collector objects for picking colors off the screen
	protected static Robot kRobot = null;

}

