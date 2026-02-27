#!/usr/bin/env sh
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
# SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
set -e
jre/bin/java \
  --module-path "./javafx-lib" --add-modules javafx.controls,javafx.web \
  -cp './deployable-jars/*' net.splitcells.network.distro.javafx.JavaFxGuiLauncher