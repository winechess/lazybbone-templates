import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

import static java.nio.file.Files.*
import static java.nio.file.Paths.get

/** ****************************************************************************/
/********************  DEFINE PROPS SECTION START  ****************************/
/** ****************************************************************************/

def props = [:]

props.group = ask('Group [com.example.root.module]: ', 'com.example.root.module').replace('-' as char, '_' as char)
props.version = ask('Version [0.0.1]: ', '0.0.1')
props.mainClass = ask('Main class [DiscoveryServerApplication]: ', 'DiscoveryServerApplication')
props.serverPort = ask('Port to run on [8761]: ', '8761')
props.configDir = ask('Directory for configurations [config-repo]: ', 'config-repo')
props.projectName = projectDir.name

String configRepoDir = props.configDir
String pkgPath = props.group.replace('.' as char, '/' as char)

/** ****************************************************************************/
/******************** DEFINE TEMPLATE FILE NAMES SECTION  *********************/
/** ****************************************************************************/

String mainApplicationFileName = 'Application.java'
String testMainApplicationFileName = 'ApplicationTest.java'
String appBootstrapFileName = 'bootstrap-app.yml'
String appBootstrapFileNameDst = 'bootstrap.yml'
String testBootstrapFileName = 'bootstrap-test.yml'
String testBootstrapFileNameDst = 'bootstrap.yml'
String configBootstrapFileName = 'bootstrap-config.yml'
String configBootstrapFileNameDst = "${props.projectName}.yml"
String buildFileName = 'build.gradle'
String dockerFileName = 'Dockerfile'
String readmeFileName = 'README.md'

/** ****************************************************************************/
/****************  PROCESS FILES SECTION START  *******************************/
/** ****************************************************************************/

processTemplates buildFileName, props
processTemplates mainApplicationFileName, props
processTemplates testMainApplicationFileName, props
processTemplates readmeFileName, props
processTemplates appBootstrapFileName, props
processTemplates testBootstrapFileName, props
processTemplates configBootstrapFileName, props
processTemplates dockerFileName, props

/** ****************************************************************************/
/*****************  DEFINE PATHS SECTION START  *******************************/
/** ****************************************************************************/

Path templatePath = templateDir.toPath()
Path projectPath = projectDir.toPath()

//MAIN STRUCTURE

// define java source path
Path srcDirWithPkg = get projectPath as String, 'src/main/java/', pkgPath
// define java resource path
Path javaResourceDir = projectPath.resolve 'src/main/resources'

// define path to main class file in template
Path mainClassTpl = templatePath.resolve mainApplicationFileName
// define path to destination main class file
Path mainClassDst = srcDirWithPkg.resolve "${props.mainClass}.java"

// define path to application property file in template
Path appBootstrapTpl = templatePath.resolve appBootstrapFileName
// define path to destination application property file
Path appBootstrapDst = javaResourceDir.resolve appBootstrapFileNameDst

//TEST STRICTURE

// define test source path
Path testDirWithPkg = get projectPath as String, 'src/test/java/', pkgPath
// define test resource path
Path testResDirWithPkg = projectPath.resolve 'src/test/resources'

// define path to test main class
Path testMainClassTpl = templatePath.resolve testMainApplicationFileName
//define destination to main class in test package
Path testMainClassDst = testDirWithPkg.resolve "${props.mainClass}Test.java"

// define path to test application property file in template
Path testBootstrapTpl = templatePath.resolve testBootstrapFileName
// define destination path to test application property file
Path testBootstrapDst = testResDirWithPkg.resolve testBootstrapFileNameDst

//CONFIG_SERVER_BOOTSTRAP

Path configBootstrapTpl = templatePath.resolve configBootstrapFileName
Path configBootstrapDst = projectPath.parent.resolve(configRepoDir).resolve configBootstrapFileNameDst

/** ****************************************************************************/
/*****************  CREATING PROJECT STRUCTURE SECTION START  *****************/
/** ****************************************************************************/

//Creating java source dir with package
srcDirWithPkg.toFile() mkdirs()
//Creating test source dir with package
testDirWithPkg.toFile() mkdirs()

try {
    //move main class to main dir
    move mainClassTpl, mainClassDst
    //move main class to test dir
    move testMainClassTpl, testMainClassDst

    //move property file to main resource dir
    move appBootstrapTpl, appBootstrapDst
    //move property file to test resource dir
    move testBootstrapTpl, testBootstrapDst
    //move config-server configuration to config-repo folder of root project
    move configBootstrapTpl, configBootstrapDst


    createRebuildScript templatePath, projectPath, props
    attachProjectToRoot projectPath
    addProjectToDockerCompose projectPath, props


} catch (ignored) {
    ignored.printStackTrace()
    println '^' * 50
    println 'ignored exception'.center(50, '^')
    println '^' * 50
}

void createRebuildScript(templatePath, projectPath, props) {

    String rebuildScriptFileName = 'rebuild.sh'
    processTemplates rebuildScriptFileName, props
    Path rebuildScriptTpl = templatePath.resolve rebuildScriptFileName

    String rebuildScriptFileNamePrefix = 'rebuild'
    Path newRebuildScriptPath = projectPath.parent.resolve "${rebuildScriptFileNamePrefix}-${projectDir.name}"

    if (notExists(newRebuildScriptPath)) {
        move rebuildScriptTpl, newRebuildScriptPath
        setPosixFilePermissions newRebuildScriptPath, PosixFilePermissions.fromString("rwxr-xr-x")
    }

}

void attachProjectToRoot(projectPath) {
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
}


void addProjectToDockerCompose(projectPath, props) {
    Path dockerComposePath = projectPath.parent.resolve('docker-compose.yml')
    File dockerComposeFile = dockerComposePath.toFile()

    if (notExists(dockerComposePath)) {
        dockerComposeFile = createFile dockerComposePath toFile()
    }

    def currentConfigDump = dockerComposeFile.text

    dockerComposeFile.withWriter { writer ->
        currentConfigDump.eachLine { line ->
            if (line.contains('end includes')) {
                writer << "\t${projectDir.name}:"
                writer << '\n'
                writer << "\t\timage: ${projectDir.name}"
                writer << '\n'
                writer << "\t\trestart: always"
                writer << '\n'
                writer << "\t\tports:"
                writer << '\n'
                writer << "\t\t\t- ${props.serverPort}:${props.serverPort}"
                writer << '\n'
            }

            writer << line
            writer << '\n'

        }
        writer.close()
    }
}