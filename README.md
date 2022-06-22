# Quarkus Messaginghub Pooled JMS Extension
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![Build](https://github.com/quarkiverse/quarkus-pooled-jms/workflows/Build/badge.svg?branch=main)](https://github.com/quarkiverse/quarkus-pooled-jms/actions?query=workflow%3ABuild)
[![License](https://img.shields.io/github/license/quarkiverse/quarkus-pooled-jms)](http://www.apache.org/licenses/LICENSE-2.0)
[![Central](https://img.shields.io/maven-central/v/io.quarkiverse.messaginghub/quarkus-pooled-jms-parent?color=green)](https://search.maven.org/search?q=g:io.quarkiverse.messaginghub%20AND%20a:quarkus-pooled-jms-parent)
<!-- ALL-CONTRIBUTORS-BADGE:START -->
<!-- ALL-CONTRIBUTORS-BADGE:END -->

This extension provides JMS Connection pool for messaging applications that provides pooling for JMS Connections, Sessions and MessageProducers. Also it can be integrated with transaction manager to support XA JTA which is provided by [Quarkus Narayana JTA](https://quarkus.io/guides/transaction). Add the following dependency in your pom.xml to get started,

```xml
<dependency>
    <groupId>io.quarkiverse.messaginghub</groupId>
    <artifactId>quarkus-pooled-jms</artifactId>
</dependency>
```

For more information and quickstart, you can check the complete [documentation](https://quarkiverse.github.io/quarkiverse-docs/quarkus-pooled-jms/dev/index.html).

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):
<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://zhfeng.github.io/"><img src="https://avatars.githubusercontent.com/u/1246139?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Amos Feng</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-pooled-jms/commits?author=zhfeng" title="Code">💻</a> <a href="#maintenance-zhfeng" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->
<!-- ALL-CONTRIBUTORS-LIST:START -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!
