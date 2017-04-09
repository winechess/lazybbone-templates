import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

import static java.nio.file.Files.*
import static java.nio.file.Paths.get

def props = [:]

props.group = ask('Group [com.example.root.module]: ', 'com.example.root.module').replace('.' as char, '/' as char).replace('-' as char, '_' as char)
props.version = ask('Version [0.0.1]: ', '0.0.1')
props.appName = ask('Application name [MyApplication]: ', 'MyApplication')
props.projectName = projectDir.name
props.dockerHost = "192.168.99.100"

String mainApplicationFileName = 'Application.java'
String testMainApplicationFileName = 'ApplicationTest.java'
String rebuildScriptFileName = 'rebuild.sh'
String rebuildScriptFileNamePrefix = 'rebuild'

processTemplates "build.gradle", props
//processTemplates "src/main/resources/bootstrap.yml", props
processTemplates 'Dockerfile', props
processTemplates 'README.md', props
processTemplates mainApplicationFileName, props
processTemplates testMainApplicationFileName, props
processTemplates rebuildScriptFileName, props

String pkgPath = props.group

Path templatePath = templateDir.toPath()
Path projectPath = projectDir.toPath()

Path javaSourceDirWithPackage = get projectPath as String, 'src/main/java/', pkgPath
Path destinationAppFilePath = javaSourceDirWithPackage.resolve props.appName+".java"
Path templateApplicationPath = templatePath.resolve mainApplicationFileName
Path templateRebuildScriptPath = templatePath.resolve rebuildScriptFileName

javaSourceDirWithPackage.toFile() mkdirs()

Path javaTestSourceDirWithPackage = get projectPath as String, 'src/test/java/', pkgPath
Path templateTestApplicationPath = templatePath.resolve testMainApplicationFileName
Path destinationTestAppFilePath = javaTestSourceDirWithPackage.resolve props.appName+"Tests.java"

javaTestSourceDirWithPackage.toFile() mkdirs()

try {

    move templateApplicationPath, destinationAppFilePath
    move templateTestApplicationPath, destinationTestAppFilePath

    Path newRebuildScriptPath = projectPath.parent.resolve rebuildScriptFileNamePrefix+'-'+projectDir.name as String
    if(notExists(newRebuildScriptPath)){
        move templateRebuildScriptPath, newRebuildScriptPath
        setPosixFilePermissions newRebuildScriptPath, PosixFilePermissions.fromString("rwxr-xr-x")
    }

    Path newSettingsPath = projectPath.parent.resolve('settings.gradle')
    File newSettingsFile = newSettingsPath.toFile()

    if (notExists(newSettingsPath)) {
        newSettingsFile = createFile newSettingsPath toFile()
    }

    def currentConfigDump = newSettingsFile.text

    newSettingsFile.withWriter { writer ->
        currentConfigDump.eachLine { line ->
            if (line.contains('end includes')) {
                writer << "include '${projectDir.name}'"
                writer << '\n'
            }

            writer << line
            writer << '\n'

        }
        writer.close()
    }
} catch (ignored) {
    ignored.printStackTrace()
    println '^'*50
    println 'ignored exception'.center(50,'^')
    println '^'*50
}
