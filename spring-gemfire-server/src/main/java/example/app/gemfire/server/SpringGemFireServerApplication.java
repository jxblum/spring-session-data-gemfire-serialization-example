package example.app.gemfire.server;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.server.CacheServer;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;

/**
 * Spring Boot application class used to configure and bootstrap a Pivotal GemFire Server along with
 * a {@link CacheServer} to enable {@link ClientCache} applications to connect in a client/server topology.
 *
 * This application class will also configure and initialize an embedded GemFire Locator and embedded GemFire Manager.
 *
 * Finally, this application class will also configure and initialize the server components for Spring Session.
 *
 * @author John Blum
 * @see org.apache.geode.cache.client.ClientCache
 * @see org.apache.geode.cache.server.CacheServer
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.builder.SpringApplicationBuilder
 * @see org.springframework.data.gemfire.config.annotation.CacheServerApplication
 * @see org.springframework.data.gemfire.config.annotation.EnableLocator
 * @see org.springframework.data.gemfire.config.annotation.EnableManager
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession
 * @see org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration
 * @since 1.0.0
 */
@SpringBootApplication
@CacheServerApplication(name = "SpringSessionDataGemFireSerializationServer", port = 0)
@EnableGemFireHttpSession(
  regionName = "Sessions",
  sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
)
@EnableLocator
@EnableManager(start = true)
public class SpringGemFireServerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(SpringGemFireServerApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);
  }
}
