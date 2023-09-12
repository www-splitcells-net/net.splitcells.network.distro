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
import java.net.URI;

import static net.splitcells.dem.resource.communication.log.Domsole.domsole;
import static net.splitcells.dem.utils.ExecutionException.executionException;

public class GuiLauncher {

    private static final int DEFAULT_MARGIN = 5;
    private static final int DEFAULT_PADDING = 5;

    private static final String HELP_TEXT = "This application's only GUI is this launcher. "
            + "This application is a server program. "
            + "Therefore, the program has to be accessed via an internet browser: "
            + "click on the `open` button or insert the `URL` into your favorite browser.";

    public static void main(String... args) {
        FlatLightLaf.setup();
        final var mainFrame = new JFrame("Splitcells Network Distro");
        mainFrame.setResizable(false);
        {
            final var pane = mainFrame.getContentPane();
            final var layout = new GridBagLayout();
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
            final var urlLabel = new JLabel("URL");
            urlLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            final var urlText = new JTextField("http://localhost:8443/index");
            urlText.setEditable(false);
            final var infoText = new JTextArea();
            infoText.setText(HELP_TEXT);
            infoText.setSize(50, 100);
            infoText.setLineWrap(true);
            infoText.setEditable(false);
            infoText.setWrapStyleWord(true);
            infoText.setRows(6);
            infoText.setColumns(20);

            pane.add(urlLabel, new GridBagConstraints(0
                    , 0
                    , 1
                    , 1
                    , 0
                    , 0
                    , GridBagConstraints.LINE_END
                    , GridBagConstraints.BOTH
                    , new Insets(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN)
                    , DEFAULT_PADDING
                    , DEFAULT_PADDING));
            pane.add(urlText, new GridBagConstraints(1
                    , 0
                    , 3
                    , 1
                    , 0
                    , 0
                    , GridBagConstraints.CENTER
                    , GridBagConstraints.BOTH
                    , new Insets(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN / 2, DEFAULT_MARGIN)
                    , DEFAULT_PADDING
                    , DEFAULT_PADDING
            ));
            pane.add(exitButton, new GridBagConstraints(0
                    , 1
                    , 1
                    , 1
                    , 0
                    , 0
                    , GridBagConstraints.LINE_END
                    , 0
                    , new Insets(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN / 2, DEFAULT_MARGIN)
                    , DEFAULT_PADDING
                    , DEFAULT_PADDING));
            pane.add(openButton, new GridBagConstraints(0
                    , 2
                    , 1
                    , 1
                    , 0
                    , 0
                    , GridBagConstraints.LINE_END
                    , 0
                    , new Insets(DEFAULT_MARGIN / 2, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN)
                    , DEFAULT_PADDING
                    , DEFAULT_PADDING));
            pane.add(new JLabel(" "), new GridBagConstraints(0
                    , 3
                    , 1
                    , 1
                    , 0
                    , 0
                    , GridBagConstraints.LINE_END
                    , 0
                    , new Insets(DEFAULT_MARGIN / 2, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN)
                    , DEFAULT_PADDING
                    , DEFAULT_PADDING));
            pane.add(new JScrollPane(infoText), new GridBagConstraints(1
                    , 1
                    , 3
                    , 3
                    , 0
                    , 0
                    , GridBagConstraints.LINE_END
                    , GridBagConstraints.BOTH
                    , new Insets(DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN, DEFAULT_MARGIN)
                    , DEFAULT_PADDING
                    , DEFAULT_PADDING));
        }
        // TODO HACK This is an quick hack. Please make clean program exit.
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private static GridBagConstraints gbc(int gridX, int gridY) {
        return new GridBagConstraints(gridX
                , gridY
                , 1
                , 1
                , 0
                , 0
                , GridBagConstraints.CENTER
                , GridBagConstraints.CENTER
                , new Insets(10, 10, 10, 10)
                , 10
                , 10
        );
    }
}
