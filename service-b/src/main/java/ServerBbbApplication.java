import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author chenchonggui
 * @version 1.0
 * @date_time 2020/12/1 20:57
 */
@SpringBootApplication(scanBasePackages = {"com.adamo.service.*"})
public class ServerBbbApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServerBbbApplication.class, args);
    }
}