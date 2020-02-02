package org.synyx.urlaubsverwaltung.mail.config;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
    "spring.mail.host=my.smtp.server",
    "spring.mail.port=1025",
    "uv.mail.sender=sender@example.org",
    "uv.mail.administrator=admin@example.org",
    "uv.mail.application-url=http://localhost:8080"
})
public class SpringBootConfiguredMailConfigIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void hasSpringBootConfiguredMailConfig() {
        assertThat(applicationContext.containsBean("springBootConfiguredMailConfig")).isTrue();
        assertThat(applicationContext.containsBean("webConfiguredMailConfig")).isFalse();
    }
}
