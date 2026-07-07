# 依赖配置

## build.gradle

```groovy
repositories {
    maven { url = "https://maven.createmod.net" } // Create, Ponder, Flywheel
    maven { url = "https://maven.ithundxr.dev/mirror" } // Registrate
    maven { url = "https://raw.githubusercontent.com/Fuzss/modresources/main/maven/" } // ForgeConfigAPIPort
}

dependencies {
    implementation(fg.deobf("com.simibubi.create:create-${minecraft_version}:${create_version}:slim") { transitive = false })
    implementation(fg.deobf("net.createmod.ponder:Ponder-Forge-${minecraft_version}:${ponder_version}"))
    compileOnly(fg.deobf("dev.engine-room.flywheel:flywheel-forge-api-${minecraft_version}:${flywheel_version}"))
    runtimeOnly(fg.deobf("dev.engine-room.flywheel:flywheel-forge-${minecraft_version}:${flywheel_version}"))
    implementation(fg.deobf("com.tterrag.registrate:Registrate:${registrate_version}"))
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:0.4.1"))
    implementation("io.github.llamalad7:mixinextras-forge:0.4.1")
}
```

## gradle.properties

```properties
minecraft_version = 1.20.1
create_version = 6.0.8-289
ponder_version = 1.0.91
flywheel_version = 1.0.5
registrate_version = MC1.20-1.3.3
```

## Mixin Refmap 重映射

```groovy
minecraft {
    runs {
        client {
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
        }
        server {
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
        }
    }
}
```

## mods.toml

```toml
[[dependencies.GoetyMech]]
modId="create"
mandatory=true
versionRange="[6.0.8,6.1.0)"
ordering="NONE"
side="BOTH"
```
