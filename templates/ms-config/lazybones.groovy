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
props.mainClass = ask('Main class [MyApplication]: ', 'MyApplication')
props.projectName = projectDir.name
String pkgPath = props.group.replace('.' as char, '/' as char)
/******************************************************************************/
/******************** DEFINE TEMPLATE FILE NAMES SECTION  *********************/
/******************************************************************************/

String mainApplicationFileName = 'Application.java'
String testMainApplicationFileName = 'ApplicationTest.java'
String resourceFileName = 'application.yml'
String testResourceFileName = 'application-test.yml'
String defaultResourceFileName = 'application-default.yml'
String rebuildScriptFileName = 'rebuild.sh'
String rebuildScriptFileNamePrefix = 'rebuild'
String buildFileName = 'build.gradle'
String dockerFileName = 'Dockerfile'
String readmeFileName = 'README.md'

/******************************************************************************/
/****************  PROCESS FILES SECTION START  *******************************/
/******************************************************************************/

processTemplates buildFileName, props
processTemplates resourceFileName, props
processTemplates testResourceFileName, props
processTemplates dockerFileName, props
processTemplates readmeFileName, props
processTemplates mainApplicationFileName, props
processTemplates testMainApplicationFileName, props
processTemplates rebuildScriptFileName, props

/******************************************************************************/
/*****************  DEFINE PATHS SECTION START  *******************************/
/******************************************************************************/

Path templatePath = templateDir.toPath()
Path projectPath = projectDir.toPath()
Path configDirPath = projectPath.parent.resolve('config-repo')

//MAIN STRUCTURE

// define java source path
Path srcDirWithPkg = get projectPath as String, 'src/main/java/', pkgPath
// define java resource path
Path javaResourceDir = projectPath.resolve 'src/main/resources'

// define path to main class file in template
Path mainClassTpl = templatePath.resolve mainApplicationFileName
// define path to destination main class file
Path mainClassDst = srcDirWithPkg.resolve props.mainClass+".java"

// define path to application property file in template
Path propFileTpl = templatePath.resolve resourceFileName
// define path to destination application property file
Path propFileDst = javaResourceDir.resolve resourceFileName

// define path to rebuild script file in template
Path rebuildScriptTpl = templatePath.resolve rebuildScriptFileName

//TEST STRICTURE

// define test source path
Path testDirWithPkg = get projectPath as String, 'src/test/java/', pkgPath
// define test resource path
Path testResDirWithPkg = projectPath.resolve 'src/test/resources'

// define path to test main class
Path testMainClassTpl = templatePath.resolve testMainApplicationFileName
//define destination to main class in test package
Path testMainClassDst = testDirWithPkg.resolve props.mainClass+"Test.java"

// define path to test application property file in template
Path testPropFileTpl = templatePath.resolve testResourceFileName
// define destination path to test application property file
Path testPropFileDst = testResDirWithPkg.resolve resourceFileName

//CONFIG REPO STRUCTURE

// define path to default application property file in template
Path defaultPropFileTpl = templatePath.resolve defaultResourceFileName
// define destination path to test application property file
Path defaultPropFileDst = configDirPath.resolve resourceFileName


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
    move propFileTpl, propFileDst
    //move property file to test resource dir
    move testPropFileTpl, testPropFileDst
    //move default property file to config dir
    move defaultPropFileTpl, defaultPropFileDst

    Path newRebuildScriptPath = projectPath.parent.resolve rebuildScriptFileNamePrefix+'-'+projectDir.name as String
    if(notExists(newRebuildScriptPath)){
        move rebuildScriptTpl, newRebuildScriptPath
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
