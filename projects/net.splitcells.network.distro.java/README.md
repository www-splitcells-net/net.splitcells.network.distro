# net.splitcells.network.distro
## DistroGuiLauncher does not work in IDE.
The error is something like `java.lang.NoClassDefFoundError: org/springframework/core/io/support/PathMatchingResourcePatternResolver`.
Maybe the IDE imported the project at the root folder of this git repo.
In this case, it could be the cases, that the IDE only loads the dependencies of the root project,
which happened once in Intellij IDEA.
Import the subproject `net.splitcells.network.distro/projects/net.splitcells.network.distro`
instead, in order to fix the problem.