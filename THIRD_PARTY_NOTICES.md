# Third-party notices

The Gradle desktop distribution includes separate, unmodified third-party artifacts in `lib/`. The accompanying `licenses/` directory contains applicable license texts. [Runtime and build dependency inventory](docs/foundation/dependencies.md)

| Component | License and upstream material |
|---|---|
| libGDX core, LWJGL3 backend, desktop natives and gdx-jnigen loader | Apache License 2.0. [libGDX 1.14.2 license](https://github.com/libgdx/libgdx/blob/1.14.2/LICENSE) · [gdx-jnigen loader project](https://github.com/libgdx/gdx-jnigen) |
| LWJGL 3.3.3 bindings and bundled native libraries | BSD 3-Clause for LWJGL. Native libraries carry their upstream terms. [LWJGL license](https://github.com/LWJGL/lwjgl3/blob/3.3.3/LICENSE.md) · [GLFW](https://www.glfw.org/license) · [OpenAL Soft](https://github.com/kcat/openal-soft) · [jemalloc](https://github.com/jemalloc/jemalloc/blob/dev/COPYING) · [stb](https://github.com/nothings/stb/blob/master/LICENSE) |
| JLayer 1.0.1-gdx | LGPL 2.1. [libGDX JLayer source](https://github.com/libgdx/jlayer) · [published source artifact](https://repo.maven.apache.org/maven2/com/badlogicgames/jlayer/jlayer/1.0.1-gdx/jlayer-1.0.1-gdx-sources.jar) |
| JOrbis 0.0.17 | GNU Library General Public License v2 or later, per the source headers. Copyright 2000 ymnk, JCraft, Inc. [Published source artifact](https://repo.maven.apache.org/maven2/org/jcraft/jorbis/0.0.17/jorbis-0.0.17-sources.jar) |
| libGDX `LSans` bitmap font | SIL Open Font License 1.1. The upstream libGDX bitmap is derived from Liberation Sans and kept inside the libGDX JAR. [libGDX adoption and naming](https://github.com/libgdx/libgdx/pull/6727) · [Liberation license and copyright](https://github.com/liberationfonts/liberation-fonts/blob/2.1.5/LICENSE) |

The two audio codec JARs are included by the backend's dependency metadata. Audio initialization is disabled in the foundation. Source and binary bundles are separate. Review redistribution and any native-component notices before publishing a release.
