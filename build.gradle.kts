import com.modrinth.minotaur.TaskModrinthUpload
import java.time.LocalDateTime
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseRelation
import net.minecraftforge.gradle.common.util.RunConfig
import wtf.gofancy.fancygradle.script.extensions.curse
import wtf.gofancy.fancygradle.script.extensions.curseForge
import wtf.gofancy.fancygradle.script.extensions.deobf
import wtf.gofancy.fancygradle.patch.Patch

plugins {
    java
    idea
    id("net.minecraftforge.gradle") version "5.+"
    id("com.matthewprenger.cursegradle") version "1.4.0"
    id("com.modrinth.minotaur") version "1.1.0"
    id("wtf.gofancy.fancygradle") version "1.0.1"
}

val versionMc: String by project
val versionMajor: String by project
val versionMinor: String by project
val versionPatch: String by project
val versionClassifierRaw: String by project
val versionJEI: String by project
val versionBaubles: String by project
val versionIC2: String by project
val versionCyclic: String by project
val versionCoffeeSpawner: String by project
val versionRailcraft: String by project
val coremodPath: String by project
val curseForgeID: String by project
val modrinthID: String by project

val versionClassifier = if (versionClassifierRaw.isNotEmpty()) "-$versionClassifierRaw" else ""
val releaseClassifier = if (versionClassifierRaw.isNotEmpty()) versionClassifierRaw.split(".")[0] else "release"

version = versionMajor + "." + versionMinor + (if (versionPatch != "0") ".$versionPatch" else "") + versionClassifier
group = "com.kingrunes.somnia"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

minecraft {
    mappings("stable", "39-1.12")
    
    accessTransformer(file("src/main/resources/META-INF/somnia_at.cfg"))

    runs {
        val config = Action<RunConfig> {
            properties(
                mapOf(
                    "forge.logging.markers" to "SCAN,REGISTRIES,REGISTRYDUMP,COREMODLOG",
                    "forge.logging.console.level" to "debug"
                )
            )
            workingDirectory = project.file("run").canonicalPath
            source(sourceSets["main"])
            jvmArgs.add("-Dfml.coreMods.load=$coremodPath")
        }

        create("client", config)
        create("server", config)
    }
}

fancyGradle {
    patches {
        patch(Patch.RESOURCES, Patch.COREMODS, Patch.CODE_CHICKEN_LIB, Patch.ASM)
    }
}

repositories {
    maven {
        name = "ic2"
        url = uri("https://maven.ic2.player.to/")
    }
    curseForge()
    maven {
        name = "ModMaven"
        url = uri("https://maven.mcmoddev.com")
    }
    maven {
        name = "Progwml6 maven"
        url = uri("https://dvs1.progwml6.com/files/maven/")
    }
}

dependencies {
    minecraft(group = "net.minecraftforge", name = "forge", version = "1.12.2-14.23.5.2855")
    
    implementation(fg.deobf(group = "mezz.jei", name = "jei_1.12.2", version = versionJEI))
    compileOnly(fg.deobf(group = "com.azanor", name = "Baubles", version = versionBaubles))
    compileOnly(fg.deobf(group = "net.industrial-craft", name = "industrialcraft-2", version = versionIC2))
    compileOnly(fg.deobf(curse(mod = "cyclic", projectId = 239286, fileId = versionCyclic.toLong())))
    compileOnly(fg.deobf(curse(mod = "coffee-spawner", projectId = 257588, fileId = versionCoffeeSpawner.toLong())))
    compileOnly(fg.deobf(curse(mod = "railcraft", projectId = 51195, fileId = versionRailcraft.toLong())))
}

tasks {
    jar {
        manifest { 
            attributes(
                "Specification-Title" to "somnia",
                "Specification-Vendor" to "kingrunes",
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "kingrunes",
                "Implementation-Timestamp" to LocalDateTime.now(),
                
                "FMLCorePlugin" to coremodPath,
                "FMLCorePluginContainsFMLMod" to true,
                "FMLAT" to "somnia_at.cfg"
            )
        }
    }
    
    processResources {
        inputs.properties(
            "version" to project.version,
            "mcversion" to versionMc
        )
    
        filesMatching("mcmod.info") {
            expand(
                "version" to project.version,
                "mcversion" to versionMc
            )
        }
    }
    
    register<TaskModrinthUpload>("publishModrinth") {
        token = System.getenv("MODRINTH_TOKEN") ?: project.findProperty("MODRINTH_TOKEN")?.toString() ?: "DUMMY"
        projectId = modrinthID
        versionName = getVersionDisplayName()
        versionNumber = version.toString().split("-")[0]
        uploadFile = jar
        addLoader("forge")
        releaseType = releaseClassifier
        changelog = System.getenv("CHANGELOG") ?: System.getProperty("CHANGELOG") ?: ""
        addGameVersion(versionMc)
    }
}

curseforge {
    apiKey = System.getenv("CURSEFORGE_TOKEN") ?: project.findProperty("CURSEFORGE_TOKEN")?.toString() ?: "DUMMY"
    project(closureOf<CurseProject> { 
        id = curseForgeID
        changelogType = "markdown"
        changelog = System.getenv("CHANGELOG") ?: System.getProperty("CHANGELOG") ?: ""
        releaseType = releaseClassifier
        mainArtifact(tasks.jar, closureOf<CurseArtifact> { 
            displayName = getVersionDisplayName()
            relations(closureOf<CurseRelation> {
                optionalDependency("openblocks")
                optionalDependency("cyclic")
                optionalDependency("dark-utilities")
                optionalDependency("railcraft")
            })
        })
        addGameVersion("Forge")
        addGameVersion(versionMc)
    })
}

fun getVersionDisplayName(): String {
    val classifier: String = 
        if (versionClassifier.isNotEmpty()) { 
            val classifierName = versionClassifier
            val firstLetter = classifierName.substring(0, 1).toUpperCase()
            val remainingLetters = classifierName.substring(1, classifierName.length)

            val parts = classifierName.split(".")
            firstLetter + remainingLetters + (if (parts.size > 1) " ${parts[1]}" else "") 
        } else ""

    return "$name ${version.toString().split("-")[0]} $classifier"
}
