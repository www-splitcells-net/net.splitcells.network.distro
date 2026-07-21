/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.swt;

import net.splitcells.dem.lang.annotations.JavaLegacy;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;


@JavaLegacy
public class Main {
    public static void main(String[] args) {
        Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }
}
