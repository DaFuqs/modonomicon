<!--
SPDX-FileCopyrightText: 2022 klikli-dev

SPDX-License-Identifier: MIT
-->

# Modonomicon 

Data-driven minecraft in-game documentation with progress visualization.

[![Open in Gitpod](https://gitpod.io/button/open-in-gitpod.svg)](https://gitpod.io/#https://github.dev/klikli-dev/modonomicon)

## Curseforge

https://www.curseforge.com/minecraft/mc-mods/modonomicon

## Maven

See https://cloudsmith.io/~klikli-dev/repos/mods/groups/ for available versions.

```gradle
repositories {

  ...

  maven {
    url "https://dl.cloudsmith.io/public/klikli-dev/mods/maven/"
    content {
        includeGroup "com.klikli_dev"
    }
  }
  
  ...
  
}
```

```gradle
dependencies {
 
    ...
    
    compileOnly fg.deobf("com.klikli_dev:modonomicon-${minecraft_version}-common:${modonomicon_version}")
    implementation fg.deobf("com.klikli_dev:modonomicon-${minecraft_version}-forge:${modonomicon_version}")
    
    ...
    
}
```

## Thanks 

[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=for-the-badge)](https://cloudsmith.com)

Package repository hosting is graciously provided by [Cloudsmith](https://cloudsmith.com).
Cloudsmith is the only fully hosted, cloud-native, universal package management solution, that
enables your organization to create, store and share packages in any format, to any place, with total
confidence.

## Licensing

Copyright 2022 klikli-dev

Code is licensed under the MIT license, view [LICENSES/MIT](./LICENSES/MIT.txt).
Assets are licensed under the CC-BY-SA-4.0 license, view [LICENSES/CC-BY-SA-4.0](./LICENSES/CC-BY-4.0.txt).
Third party software and assets may be distributed under a different license, see [THIRD_PARTY_NOTICES.md](./THIRD_PARTY_NOTICES.md).
