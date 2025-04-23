package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean
	Hibernate6Module hibernate5Module() {
		// 지연 로딩(Lazy Loading)된 연관 필드도 JSON으로 직렬화할 때 강제로 DB에서 조회해서 포함
		// hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, true);
		return new Hibernate6Module();
	}

}
