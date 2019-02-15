package example.app.gemfire.server;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;

/**
 * The GemFireServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "SpringSessionDataGemFireSerializationServer")
@EnableGemFireHttpSession(
  regionName = "Sessions",
  sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
)
@EnableLocator
@EnableManager(start = true)
public class GemFireServerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(GemFireServerApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);
  }
}
