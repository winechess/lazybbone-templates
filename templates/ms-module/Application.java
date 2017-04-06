package ${ group };

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class ${appName} {
    public static void main(String[] args) {
        SpringApplication.run(${appName}.class, args);
    }
}