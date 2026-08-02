package th.camt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// @EntityScan is required here because this is a multi-module project: the JPA
// entities (Customer, Product, Order, OrderItem, ShippingAddress) live in the
// separate domain-model module under package th.mfu.domain, while this
// @SpringBootApplication class lives in th.camt (the web-service module).
// Spring Boot only auto-detects entities under the SAME package as the
// @SpringBootApplication class by default, so without this annotation Hibernate
// would never register those classes as managed types and every repository bean
// would fail to start with "Not a managed type: ...". Repositories don't need
// the equivalent @EnableJpaRepositories because they live in th.camt.repository,
// which IS under this class's own package and gets picked up automatically.
@SpringBootApplication
@EntityScan(basePackages = "th.mfu.domain")
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("http://localhost:8081");
			}
		};
	}
}


