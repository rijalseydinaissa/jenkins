package com.example.Jenkins;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class JenkinsApplication {

	public static void main(String[] args) {
		SpringApplication.run(JenkinsApplication.class, args);
	}

}
@RestController
class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello Jenkins Demo! L'application fonctionne parfaitement 🚀";
    }

    @GetMapping("/health")
    public String health() {
        return "OK - Application is healthy";
    }

    @GetMapping("/api/demo")
    public DemoResponse demo() {
        return new DemoResponse("Jenkins CI/CD", "SUCCESS", System.currentTimeMillis());
    }
}

class DemoResponse {
    private String project;
    private String status;
    private long timestamp;

    public DemoResponse(String project, String status, long timestamp) {
        this.project = project;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters
    public String getProject() { return project; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
}




