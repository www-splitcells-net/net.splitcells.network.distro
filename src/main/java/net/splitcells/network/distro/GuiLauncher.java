/*
 * Copyright (c) 2021 Contributors To The `net.splitcells.*` Projects
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License v2.0 or later
 * which is available at https://www.gnu.org/licenses/old-licenses/gpl-2.0-standalone.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static net.splitcells.dem.resource.communication.log.Domsole.domsole;
import static net.splitcells.dem.utils.ExecutionException.executionException;

public class GuiLauncher {
    public static void main(String... args) {
        FlatLightLaf.setup();
        final var mainFrame = new JFrame("Splitcells Network Distro");
        mainFrame.setSize(400, 200);
        mainFrame.setResizable(false);
        {
            final var pane = mainFrame.getContentPane();
            final var layout = new GridLayout(2, 2);
            pane.setLayout(layout);
            final var exitButton = new JButton("Exit");
            exitButton.addActionListener(actionEvent -> {
                // TODO HACK This is an quick hack. Please make clean program exit.
                System.exit(1);
            });
            final var openButton = new JButton("Open");
            openButton.addActionListener(actionEvent -> {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:8443/index"));
                } catch (Throwable th) {
                    domsole().appendWarning(th);
                    throw executionException(th);
                }
            });
            pane.add(new JLabel("URL"));
            final var urlText = new JTextField("http://localhost:8443/index");
            urlText.setEditable(false);
            pane.add(urlText);

            pane.add(openButton);
            pane.add(exitButton);
        }
        // TODO HACK This is an quick hack. Please make clean program exit.
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.setVisible(true);
    }
}
