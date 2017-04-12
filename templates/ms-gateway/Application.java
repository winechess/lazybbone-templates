package ${ group };

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
public class ${mainClass} {
    public static void main(String[] args) {
        SpringApplication.run(${mainClass}.class, args);
    }
}