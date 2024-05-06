# Changelog
The changelog format can be found [here](./src/main/md/net/splitcells/network/guidelines/changelog.md).
## [Unreleased]
### Added
* **2024-05-06 \#c11** Use SSL encryption (HTTPS) for communication with web server.
  The certificate is generated via [Let's Encrypt](https://letsencrypt.org/) automatically.
* **2023-09-04 \#199** [Build website without private and personal repo of author](https://github.com/www-splitcells-net/net.splitcells.network/issues/199):
  The distro project now can serve the website based on only publicly available repos and
  without files from personal projects.
  This is considered the default distribution of the software with default settings.