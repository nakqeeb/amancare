package com.nakqeeb.amancare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * الكلاس الرئيسي لتطبيق نظام إدارة العيادات الطبية
 *
 * @author Your Name
 * @version 1.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class AmancareApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmancareApplication.class, args);
		System.out.println("🏥 نظام إدارة العيادات الطبية بدأ التشغيل بنجاح!");
		System.out.println("📖 وثائق API متاحة على: http://localhost:8080/api/v1/swagger-ui.html");
	}

}
