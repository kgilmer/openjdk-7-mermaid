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
 * @summary A utility class for resizing frames 
 * @summary com.apple.junit.utils
 */

package test.java.awt.regtesthelpers;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.*;

public abstract class LiveResizeAction implements Runnable {
	Robot robot; 
	Frame frame;

	public LiveResizeAction(Frame frame, Robot robot) {
		this.frame = frame;
		this.robot = robot; 
	}

	public abstract void performResizeFor(final int x, final int y, final int frameX) throws InterruptedException;

	@Override
	public void run() {
		int frameX = (int) (frame.getLocationOnScreen().getX());
		int frameY = (int) (frame.getLocationOnScreen().getY());

		boolean mouseDown = false;
		try {
			robot.setAutoWaitForIdle(true);

			// Move to the resize thumb
			int width = (int) (frame.getBounds().getWidth());
			int height = (int) (frame.getBounds().getHeight());
			int x = frameX + width - 6;
			int y = frameY + height - 6;

			// Hold down the mouse
			robot.mouseMove(x, y);
			mouseDown = true;
			robot.mousePress(InputEvent.BUTTON1_MASK);

			// Move the mouse through the appropriate path
			performResizeFor(x, y, frameX);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mouseDown) {
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			}
		}
	}

	public static class SingleMove extends LiveResizeAction {
		public SingleMove(Frame frame, Robot robot) {
			super(frame, robot);
		}

		@Override
		public void performResizeFor(int x, int y, int frameX) throws InterruptedException {
			Thread.sleep(2000);
			robot.mouseMove(x + 100, y + 100);
		}
	}

	public static class TripleMove extends LiveResizeAction {
		static int[][] moves = {
			// duration, xoff, yoff
			{ 2000, 100, 100 },
			{  500, -50, -50 },
			{  500, 100, 100 }
		};

		public TripleMove(Frame frame, Robot robot) {
			super(frame, robot);
		}

		@Override
		public void performResizeFor(int x, int y, int frameX) throws InterruptedException {
			for (int[] motion: moves) {
				Thread.sleep(motion[0]);
				robot.mouseMove(x + motion[1], y + motion[2]);
			}
		}
	}

	public static class VariableMove extends LiveResizeAction {
		// Wiggle the mouse around on the diagonal for a few seconds
		// Keep Gerard's magic numbers until we have any reason to vary them 

		public VariableMove(Frame frame, Robot robot) {
			super(frame, robot);
		}

		static final int TESTTIME = 4000; 
		private volatile boolean done = false;
		class Stopper extends TimerTask {
			public void run(){
				done = true;
			}
		}

		@Override
		public void performResizeFor(int startX, int startY, int frameX) throws InterruptedException {
			Timer timer = new Timer();
			try {
				timer.schedule(new Stopper(), TESTTIME);
				int x = startX;
				int y = startY;
				int maxX = frameX + (int) (frame.getBounds().getWidth()) + 50;
				int minX = frameX + (int) (frame.getBounds().getWidth()) - 50;
				int offset = 0;
				int direction = 1;
				int delta = 10;
				while (!done) {
					// invert direction when you bounce off a min
					if ((x + offset >= maxX) || (x + offset <= minX)) {
						direction *= -1;
					}						
					offset += (delta * direction);

					robot.mouseMove(x + offset, y + offset);
					robot.waitForIdle();
					robot.delay(robot.getAutoDelay());
					Thread.sleep(15);
				}
			} finally {
				timer.cancel();
			}
		}
	}
}
