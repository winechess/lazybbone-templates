import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

import static java.nio.file.Files.*
import static java.nio.file.Paths.get

/******************************************************************************/
/********************  DEFINE PROPS SECTION START  ****************************/
/******************************************************************************/

def props = [:]

props.group = ask('Group [com.example.root.module]: ', 'com.example.root.module').replace('-' as char, '_' as char)
props.version = ask('Version [0.0.1]: ', '0.0.1')
props.mainClass = ask('Main class [ConfigServerApplication]: ', 'ConfigServerApplication')
//props.serverPort = ask('Port to run on [8888]: ', '8888')
props.configDir = ask('Directory for configurations [config-repo]: ', 'config-repo')
props.projectName = projectDir.name

String pkgPath = props.group.replace('.' as char, '/' as char)
String configRepoDir = props.configDir

/******************************************************************************/
/******************** DEFINE TEMPLATE FILE NAMES SECTION  *********************/
/******************************************************************************/

String mainClassFilename = 'Application.java'
String testMainClassFilename = 'ApplicationTest.java'
String configFileName = 'application.yml'
String testConfigFileName = 'application-test.yml'
String commonConfigFilename = 'application-default.yml'
String buildFileName = 'build.gradle'
String dockerFileName = 'Dockerfile'
String readmeFileName = 'README.md'

/******************************************************************************/
/****************  PROCESS FILES SECTION START  *******************************/
/******************************************************************************/

processTemplates buildFileName, props
processTemplates configFileName, props
processTemplates testConfigFileName, props
processTemplates dockerFileName, props
processTemplates readmeFileName, props
processTemplates mainClassFilename, props
processTemplates testMainClassFilename, props

/******************************************************************************/
/*****************  DEFINE PATHS SECTION START  *******************************/
/******************************************************************************/

Path templatePath = templateDir.toPath()
Path projectPath = projectDir.toPath()
Path configDirPath = projectPath.parent.resolve("${configRepoDir}")

//MAIN STRUCTURE

// define java source path
Path srcDirWithPkg = get projectPath as String, 'src/main/java/', pkgPath
// define java resource path
Path javaResourceDir = projectPath.resolve 'src/main/resources'

// define path to main class file in template
Path mainClassTpl = templatePath.resolve mainClassFilename
// define path to destination main class file
Path mainClassDst = srcDirWithPkg.resolve "${props.mainClass}.java"

// define path to application property file in template
Path configTpl = templatePath.resolve configFileName
// define path to destination application property file
Path configDst = javaResourceDir.resolve configFileName


//TEST STRICTURE

// define test source path
Path testDirWithPkg = get projectPath as String, 'src/test/java/', pkgPath
// define test resource path
Path testResDirWithPkg = projectPath.resolve 'src/test/resources'

// define path to test main class
Path testMainClassTpl = templatePath.resolve testMainClassFilename
//define destination to main class in test package
Path testMainClassDst = testDirWithPkg.resolve "${props.mainClass}Test.java"

// define path to test application property file in template
Path testConfigTpl = templatePath.resolve testConfigFileName
// define destination path to test application property file
Path testConfigDst = testResDirWithPkg.resolve configFileName

//CONFIG REPO STRUCTURE

// define path to default application property file in template
Path commonConfigTpl = templatePath.resolve commonConfigFilename
// define destination path to test application property file
Path commonConfigDst = configDirPath.resolve configFileName


/******************************************************************************/
/*****************  CREATING PROJECT STRUCTURE SECTION SATRT  *****************/
/******************************************************************************/

//Creating java source dir with package
srcDirWithPkg.toFile() mkdirs()
//Creating test source dir with package
testDirWithPkg.toFile() mkdirs()
configDirPath.toFile() mkdirs()

try {
    //move main class to main dir
    move mainClassTpl, mainClassDst
    //move main class to test dir
    move testMainClassTpl, testMainClassDst

    //move property file to main resource dir
    move configTpl, configDst
    //move property file to test resource dir
    move testConfigTpl, testConfigDst
    //move default property file to config dir
    move commonConfigTpl, commonConfigDst

    createRebuildScript templatePath, projectPath, props
    attachProjectToRoot projectPath
    addProjectToDockerCompose projectPath, props

} catch (ignored) {
    ignored.printStackTrace()
    println '^'*50
    println 'ignored exception'.center(50,'^')
    println '^'*50
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
                writer << "${projectDir.name}:"
                writer << '\n'
                writer << "  image: ${projectDir.name}"
                writer << '\n'
                writer << "  restart: always"
                writer << '\n'
                writer << "  ports:"
                writer << '\n'
                writer << "    - 8080"
                writer << '\n'
                writer << "  logging:"
                writer << '\n'
                writer << "    options:"
                writer << '\n'
                writer << "      max-size: 10m"
                writer << '\n'
                writer << "      max-file: 10"
                writer << '\n'
            }

            writer << line
            writer << '\n'

        }
        writer.close()
    }
}