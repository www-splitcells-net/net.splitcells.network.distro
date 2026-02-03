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
import net.splitcells.dem.lang.annotations.JavaLegacy;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

import static net.splitcells.dem.resource.communication.log.Logs.logs;
import static net.splitcells.dem.utils.ExecutionException.execException;

@JavaLegacy
public class GuiLauncher {

    private static final int DEFAULT_MARGIN = 5;
    private static final int DEFAULT_PADDING = 5;

    /**
     * <p>TODO FIX Shutdown distro gracefully, instead of using {@link System#exit(int)}.</p>
     *
     * @param config
     */
    public static void startGuiLauncher(GuiLauncherConfig config) {
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
                    if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(config.url()));
                    } else {
                        Runtime.getRuntime().exec(new String[]{"xdg-open", config.url()}).onExit().join();
                    }
                } catch (Throwable th) {
                    try {
                        Runtime.getRuntime().exec(new String[]{"xdg-open", config.url()}).onExit().join();
                    } catch (Throwable th2) {
                        logs().warn("Could not open the servers website via the desktops Internet browser.", th);
                        logs().warn("Could not open the servers website via the desktops Internet browser.", th2);
                        throw execException(th);
                    }
                }
            });
            final var urlLabel = new JLabel("URL");
            urlLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            final var urlText = new JTextField(config.url());
            urlText.setEditable(false);
            final var infoText = new JTextArea();
            infoText.setText(config.helpText());
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
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }
}
