import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions

import static java.nio.file.Files.setPosixFilePermissions

def props = [:]

props.group = ask('Group [com.example.root]: ', 'com.example.root').replace('-' as char, '_' as char)
props.version = ask('Выберите версию [0.0.1]: ', '0.0.1')
props.projectName = props.group.split('\\.').last()

processTemplates "settings.gradle", props
processTemplates "build.gradle", props
processTemplates "README.md", props


Path templateRebuildScriptPath = templateDir.toPath().resolve 'rebuild-all'
setPosixFilePermissions templateRebuildScriptPath, PosixFilePermissions.fromString("rwxr-xr-x")
