def props = [:]

props.group = ask('Group [com.example]: ', 'com.example').replace('.' as char, '/' as char).replace('-' as char, '_' as char)
props.version = ask('Выберите версию [0.0.1]: ', '0.0.1')
props.projectName = props.group.split('\\.').last()
props.dockerHost = "192.168.99.100"
props.dns = "172.17.42.1"

String rebuildFileName = 'rebuild-all'

processTemplates "docker-compose.yml", props
processTemplates "settings.gradle", props
processTemplates "build.gradle", props
processTemplates rebuildFileName, props


Path templateRebuildScriptPath = templateDir.toPath().resolve
setPosixFilePermissions templateRebuildScriptPath, PosixFilePermissions.fromString("rwxr-xr-x")
