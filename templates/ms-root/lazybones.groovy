def props = [:]

props.group = ask('Group [com.example]: ', 'com.example')
props.version = ask('Выберите версию [0.0.1]: ', '0.0.1')
props.projectName = props.group.split('\\.').last()
props.dockerHost = "192.168.99.100"
props.dns = "172.17.42.1"

processTemplates "docker-compose.yml", props
processTemplates "settings.gradle", props
processTemplates "build.gradle", props