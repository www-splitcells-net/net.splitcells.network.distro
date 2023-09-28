# RCP Development Guidelines
* Build is based on Maven Tycho in order to support automatic builds or builds without Eclipse.
    It also makes it possible, to have a unified build infrastructure.
* Tycho based subprojects do not need a POM.
    In this case Tycho provides a reasonable default project config.
    If a POM is provided every detail needs to be configured.
    Otherwise, the build may fail.
    Therefore, avoiding POMs can be helpful.
    The `flatten-maven-plugin` can be used in this case,
    in order to generate a POM given a Tycho subproject without a POM.
* If everything breaks, it easiest to recreate the project from scratch,
    in order to find the breaking point.
    Tutorials can help to create a minimal projects.
    Keep in mind that tutorials may not describe,
    how to create one single RCP application via Tycho completely step by step.
    Instead, one has to understand the whole process and add everything that is required,
    which may not be present in the tutorial.
* If there are error with p2 dependencies,
    ensure that the target platform references a repository (=software site),
    whose default JDK/JRE version corresponds to the used Java version.
* Split every component type into its own subproject.
    For example, create a subproject for the target-platform,
    for every plugin and product.