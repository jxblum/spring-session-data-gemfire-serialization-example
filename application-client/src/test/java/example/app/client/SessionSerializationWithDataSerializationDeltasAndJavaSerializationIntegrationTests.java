package example.app.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.internal.InternalDataSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.ClientCacheConfigurer;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.core.model.Customer;
import example.app.ext.model.Phone;

/**
 * The SessionSerializationWithDataSerializationDeltasAndJavaSerializationIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
  "spring.data.gemfire.pool.locators=localhost[10334]"
})
@SuppressWarnings("unused")
public class SessionSerializationWithDataSerializationDeltasAndJavaSerializationIntegrationTests {

  private static final String GEMFIRE_LOG_LEVEL = "error";

  private static Session touch(Session session) {

    session.setLastAccessedTime(Instant.now());

    return session;
  }

  @Autowired
  private ClientCache clientCache;

  @Autowired
  private SessionRepository<Session> sessionRepository;

  @Before
  public void assertGemFireConfiguration() {

    assertThat(this.clientCache).isNotNull();
    assertThat(this.clientCache.getPdxSerializer()).isNull();
    assertThat(isCustomerDataSerializerRegistered()).isFalse();
  }

  private boolean isCustomerDataSerializerRegistered() {

    return Arrays.stream(nullSafeArray(InternalDataSerializer.getSerializers(), DataSerializer.class))
      .filter(Objects::nonNull)
      .anyMatch(isCustomerDataSerializer());
  }

  private Predicate<DataSerializer> isCustomerDataSerializer() {

    return dataSerializer ->
      Arrays.stream(nullSafeArray(dataSerializer.getSupportedClasses(), Class.class))
        .filter(Objects::nonNull)
        .anyMatch(Customer.class::equals);
  }

  @Test
  public void sessionWithCustomerSerializationWorks() {

    // 1. Create and Save (new) Session
    Session session = this.sessionRepository.createSession();

    assertThat(session).isNotNull();
    assertThat(session.getId()).isNotEmpty();
    assertThat(session.isExpired()).isFalse();
    assertThat(session.getAttributeNames()).isEmpty();

    this.sessionRepository.save(session);

    // 2. Update (touch) Session, sending delta causing the Session to be deserialized on the server
    session = touch(session);

    this.sessionRepository.save(session);

    // 3. Add Session Attribute
    Customer jonDoe = addCustomerJonDoe(session);

    this.sessionRepository.save(session);

    //Customer janeDoe = addCustomerJaneDoe(session);

    // 4. Update Session Attribute, sending delta causing the Session Attribute (Customer; Jon Doe)
    // to be deserialized on the server
    jonDoe = updateCustomer(session, "jonDoe", jonDoe);

    this.sessionRepository.save(session);

    Session loadedSession = this.sessionRepository.findById(session.getId());

    assertThat(loadedSession).isEqualTo(session);
    assertThat(loadedSession).isNotSameAs(session);
    assertThat(loadedSession.getAttributeNames()).containsOnly("jonDoe");
    assertThat(loadedSession.<Customer>getAttribute("jonDoe")).isEqualTo(jonDoe);

    /*
    assertThat(loadedSession.getAttributeNames()).containsOnly("jonDoe", "janeDoe");
    assertThat(loadedSession.<Customer>getAttribute("jonDoe")).isEqualTo(jonDoe);
    assertThat(loadedSession.<Customer>getAttribute("janeDoe")).isEqualTo(janeDoe);
    */
  }

  private Customer addCustomerJaneDoe(Session session) {

    Customer janeDoe = Customer.newCustomer("Jane Doe")
      .asFemale()
      .using(Phone.newPhone("503-555-9876"));

    session.setAttribute("janeDoe", janeDoe);

    assertThat(session.getAttributeNames()).contains("janeDoe");
    assertThat(session.<Customer>getAttribute("janeDoe")).isEqualTo(janeDoe);

    return janeDoe;
  }

  private Customer addCustomerJonDoe(Session session) {

    Customer jonDoe = Customer.newCustomer("Jon Doe")
      .asMale()
      .using(Phone.newPhone("503-555-1234").asMobile());

    session.setAttribute("jonDoe", jonDoe);

    assertThat(session.getAttributeNames()).containsOnly("jonDoe");
    assertThat(session.<Customer>getAttribute("jonDoe")).isEqualTo(jonDoe);

    return jonDoe;
  }

  private Customer updateCustomer(Session session, String sessionAttributeName, Customer customer) {

    session.setAttribute(sessionAttributeName, customer.using(Phone.newPhone("503-555-5151")));

    return customer;
  }

  @ClientCacheApplication(logLevel = GEMFIRE_LOG_LEVEL, subscriptionEnabled = true)
  @EnableGemFireHttpSession(
    poolName = "DEFAULT",
    regionName = "Sessions",
    sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
  )
  static class GemFireClientConfiguration {

    @Bean
    ClientCacheConfigurer defaultPoolReadTimeoutConfigurer() {

      return (beanName, clientCacheFactoryBean) ->
        clientCacheFactoryBean.setReadTimeout(Long.valueOf(TimeUnit.MINUTES.toMillis(5)).intValue());
    }
  }
}
