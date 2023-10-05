# Eclipse RCP Usage Reasons
* Eclipse provides one of few really trustworthy Java GUI launcher binaries (=`eclipse.exe`).
    It also provides binaries for different operation systems and CPU architectures.
* Eclipse build process can bundle JRE.
* The EPL-2.0 license of Eclipse's source code fits to the default license of the network project,
    which was =EPL-2.0 OR GPL-2.0-or-later during the RCP's creation.
* Alternatives have a lot lower popularity and therefore a higher chance of being marked as insecure.
# Future RCP plans.
* Update dependencies semi-automatically.
# RCP Problems
* Eclipse IDE is not able to build project like Tycho.
    Tycho is the definitive build.
# RCP Development Guidelines
* `flatten-maven-plugin` is not compatible with Tycho.
* Build is based on Maven Tycho in order to support automatic builds or builds without Eclipse.
    It also makes it possible, to have a unified build infrastructure.
* Tycho based subprojects do not need a POM.
    In this case Tycho provides a reasonable default project config.
    If a POM is provided every detail needs to be configured.
    Otherwise, the build may fail.
    Therefore, avoiding POMs can be helpful.
    The flatten-maven-plugin can be used in this case,
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
* If a required feature of a plugin cannot be found during the build,
    try adding the dependency to the includeBundles of the target definition file.
* A product based on features is a product, that has plugins and features.
    A product based on plugins is a product, that has only plugins and
    any mentioned feature in the product is silently ignored.
* In order to rule out, that dependencies and etc. are referenced with correct id:
    delete local `.m2` repo and clean up git repo containing project.
    After that, check every dependency used.
    Especially, dependency between locally build modules.
* Speculation: if in the product file `useFeatures="false"` is set,
    then plugins in the product files are ignored and vice versa.
* Use Maven debug flag `mvn [...] -X` in order to get meaningful build output during Tycho based builds.