package ${ group };

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class ${mainClass} {
    public static void main(String[] args) {
        SpringApplication.run(${mainClass}.class, args);
    }
}