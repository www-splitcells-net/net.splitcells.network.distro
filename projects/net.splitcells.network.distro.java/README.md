# net.splitcells.network.distro.java
> This is a GUI application, for people to easily run and use the server locally.
## Shell based Launcher
The launcher is a shell script, as Windows allows executing downloaded and not signed shell scripts.
The same is not true for EXE files.
## DistroGuiLauncher does not work in IDE.
The error is something like `java.lang.NoClassDefFoundError: org/springframework/core/io/support/PathMatchingResourcePatternResolver`.
Maybe the IDE imported the project at the root folder of this git repo.
In this case, it could be the cases, that the IDE only loads the dependencies of the root project,
which happened once in Intellij IDEA.
Import the subproject `net.splitcells.network.distro/projects/net.splitcells.network.distro`
instead, in order to fix the problem.
## Swing Choice
JavaFX's browser seems to be too old for JavaScript and does not look like a local browser,
where icons are quite different there.
Therefore, it is best for now to just have a GUI launcher with a window.
It provides a button, that opens a link to localhost,
which is automatically opened via the local browser.
As Swing is provided by Java itself, it is the most portable and easiest way to implement a GUI launcher.

One could consider using SWT in order to integrate the local browser into the GUI itself,
which may be the best mix of both worlds.
Currently, this is not acitvely pursued or decieded to be done.