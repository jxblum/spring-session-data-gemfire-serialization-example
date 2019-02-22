package example.app.gemfire.server;

import static org.springframework.data.gemfire.util.CollectionUtils.asSet;

import java.util.Set;

import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.server.CacheServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.gemfire.config.annotation.CacheServerApplication;
import org.springframework.data.gemfire.config.annotation.EnableHttpService;
import org.springframework.data.gemfire.config.annotation.EnableLocator;
import org.springframework.data.gemfire.config.annotation.EnableManager;
import org.springframework.lang.Nullable;
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
@EnableHttpService
@EnableLocator
@EnableManager(start = true)
@SuppressWarnings("unused")
public class SpringGemFireServerApplication {

  public static void main(String[] args) {

    new SpringApplicationBuilder(SpringGemFireServerApplication.class)
      .web(WebApplicationType.NONE)
      .build()
      .run(args);
  }

  @Bean
  @Profile("disable-session-indexes")
  BeanPostProcessor sessionIndexDisablingBeanPostProcessor() {

    Set<String> sessionIndexBeanNames = asSet("principalNameIndex", "sessionAttributesIndex");

    return new BeanPostProcessor() {

      @Nullable @Override @SuppressWarnings("all")
      public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return isIndexBean(bean, beanName) ? DummyObjectFactoryBean.INSTANCE : bean;
      }

      private boolean isIndexBean(Object bean, String beanName) {
        return sessionIndexBeanNames.contains(beanName);
      }
    };
  }

  @SuppressWarnings("all")
  private static final class DummyObjectFactoryBean implements FactoryBean<Object> {

    private static final DummyObjectFactoryBean INSTANCE = new DummyObjectFactoryBean();

    private static final Object DUMMY = new Object();

    @Nullable @Override
    public Object getObject() throws Exception {
      return DUMMY;
    }

    @Nullable @Override
    public Class<?> getObjectType() {
      return DUMMY.getClass();
    }
  }
}
