plugins {
    id("net.neoforged.moddev.legacyforge") version "+"
    id("me.modmuss50.mod-publish-plugin") version "+"
}

base.archivesName.set(p("mod_id"))
group = p("mod_group_id")
version = "${p("mc_version_slim")}-${p("create_version_slim")}-${p("mod_version")}-${p("loader_cap")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

// Jar 打包配置
tasks.jar {
    from("LICENSE")
    manifest.attributes("MixinConfigs" to "${p("mod_id")}.mixins.json")
}
// 包含数据生成器生成的资源
//sourceSets.main.get().resources.srcDir("src/generated/resources")
var generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
    val values = properties.mapValues { it.value.toString() }
    inputs.properties(values)
    expand(values)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}
sourceSets.main.get().resources.srcDir(generateMetadata)

mixin {
    add(sourceSets.main.get(), "${p("mod_id")}.refmap.json")
    config("${p("mod_id")}.mixins.json")
}
legacyForge {
    version = "${p("mc_version")}-${p("loader_version")}"
    parchment {
        mappingsVersion.set(p("parchment_version"))
        minecraftVersion.set(p("mc_version"))
    }
    runs {
        create("client").client()
        configureEach {
            jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
            systemProperty("terminal.jline", "true")
        }
    }
    mods.create(p("mod_id")).sourceSet(sourceSets.main.get())
}
// 仓库配置
repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.createmod.net") // Create, Ponder, Flywheel
    maven("https://maven.tterrag.com") // Registrate
    maven("https://maven.shedaniel.me") // Cloth Config API
//    maven("https://maven.blamejared.com") // JEI
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Modrinth
}

// 依赖配置
dependencies {
    modImplementation("com.simibubi.create:create-${p("mc_version")}:${p("create_version")}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:Ponder-${p("loader_cap")}-${p("mc_version")}:${p("ponder_version")}")
    modImplementation("dev.engine-room.flywheel:flywheel-${p("loader")}-${p("mc_version")}:${p("flywheel_version")}")
    modImplementation("com.tterrag.registrate:Registrate:${p("registrate_version")}")
    modImplementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("cloth_cfg_version")}")
//    modImplementation("mezz.jei:jei-${p("mc_version")}-${p("loader")}:${p("jei_version")}")
//    modImplementation("maven.modrinth:jade:${p("jade_version")}")
    annotationProcessor("org.spongepowered:mixin:${p("mixin_version")}:processor")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${p("mixin_extras_version")}")
    compileOnly("io.github.llamalad7:mixinextras-common:${p("mixin_extras_version")}")
    runtimeOnly("io.github.llamalad7:mixinextras-${p("loader")}:${p("mixin_extras_version")}")
}

publishMods {
    file.set(tasks.named("reobfJar").get().outputs.files.singleFile)
    changelog.set(file("CHANGELOG.md").readText())
    type.set(BETA)
    version.set("${p("mc_version_slim")}-${p("mod_version")}-${p("loader_cap")}")
    displayName.set("${p("mod_version")} for ${p("mc_version")} Create ${p("create_version_slim")} [${p("loader_cap")}]")
    modLoaders.addAll(p("loader_cap"))
    modrinth {
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN_PUBLISH"))
        projectId.set("l9kylKLD")
        minecraftVersions.add(p("mc_version"))
        requires("create", "cloth-config")
    }
//    curseforge {
//        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
//        projectId.set("1233804")
//        minecraftVersions.add(p("mc_version"))
//        requires("create", "cloth-config")
//    }
}

fun p(key: String) = property(key).toString()