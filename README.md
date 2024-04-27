# net.splitcells.network.distro
This is a software distribution of the Splitcells Network project cluster.
It bundles software together, in order to provide a deployable program with all the cluster's features.
It also provides additional features, that are reasonably expected for a sane deployment,
like retrieving a `Let's Encrypt` certificate in order to encrypt the communication with the server.
## Why does this distro project is located in a separate repo?
This distribution is not located in the main repo,
because the main repo contains only core functionality.
Other things like plugins, software integrations and extensions, documents and media files are optional.
This way the main repo is not dependent on optional software.