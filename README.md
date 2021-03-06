[![GitHub release](https://img.shields.io/github/release/sismics/play-gateway.svg?style=flat-square)](https://github.com/sismics/play-gateway/releases/latest)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# play-gateway plugin

This plugin adds gateway support for a REST API to Play! Framework 1 applications.

# How to use

####  Add the dependency to your `dependencies.yml` file

```
require:
    - gateway -> gateway 1.1.0

repositories:
    - sismicsNexusRaw:
        type: http
        artifact: "https://nexus.sismics.com/repository/sismics/[module]-[revision].zip"
        contains:
            - gateway -> *

```
####  Run the `play deps` command

####  Add a `routes-gateway` file to your `conf/` directory

```
# Gateway routes
# This file defines all gateway routes (Higher priority routes first)
# ~~~~

/api/geocode                                     https://anotherapi.company.com/geocode
/api/timezone                                    https://anotherapi.company.com/timezone

```

####  Programmatic configuration

Alternately, you can also add gateway APIs with Java code: 

```
public static void geocode() {
    new Gateway.Builder()
                .setBasicAuth("username", "password")
                .build()
                .proxy(request, response, "https://anotherapi.company.com/geocode");
}
```

# License

This software is released under the terms of the Apache License, Version 2.0. See `LICENSE` for more
information or see <https://opensource.org/licenses/Apache-2.0>.
