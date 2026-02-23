plugins {
    id("net.neoforged.moddev") version "+"
    id("me.modmuss50.mod-publish-plugin") version "+"
}

base.archivesName.set(p("mod_id"))
group = p("mod_group_id")
version = "${p("mc_version_slim")}-${p("create_version_slim")}-${p("mod_version")}-${p("loader_cap")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

// Jar 打包配置
tasks.jar { from("LICENSE") }
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

neoForge {
    version = p("loader_version")
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
    maven("https://mvn.devos.one/snapshots") // Registrate
    maven("https://maven.shedaniel.me") // Cloth Config API
//    maven("https://maven.blamejared.com") // JEI
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Modrinth
}

// 依赖配置
dependencies {
    implementation("com.simibubi.create:create-${p("mc_version")}:${p("create_version")}:slim") { isTransitive = false }
    implementation("dev.engine-room.flywheel:flywheel-${p("loader")}-${p("mc_version")}:${p("flywheel_version")}")
    implementation("net.createmod.ponder:ponder-${p("loader")}:${p("ponder_version")}+mc${p("mc_version")}") {
        isTransitive = false
    }
    implementation("com.tterrag.registrate:Registrate:${p("registrate_version")}")
    implementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("cloth_cfg_version")}")
//    modImplementation("mezz.jei:jei-${p("mc_version")}-${p("loader")}:${p("jei_version")}")
//    modImplementation("maven.modrinth:jade:${p("jade_version")}")
}

publishMods {
    file.set(tasks.jar.get().archiveFile)
    changelog.set(file("CHANGELOG.md").readText())
    type.set(BETA)
    version.set("${p("mod_version")}-${p("mc_version_slim")}-${p("create_version_slim")}-${p("loader_cap")}")
    displayName.set("${p("mod_version")} for Create ${p("create_version_slim")} [${p("mc_version")} ${p("loader_cap")}]")
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