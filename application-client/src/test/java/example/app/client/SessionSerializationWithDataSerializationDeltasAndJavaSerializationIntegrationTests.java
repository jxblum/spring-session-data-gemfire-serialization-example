package example.app.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.data.gemfire.util.ArrayUtils.nullSafeArray;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.internal.InternalDataSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.data.gemfire.config.annotation.web.http.EnableGemFireHttpSession;
import org.springframework.session.data.gemfire.config.annotation.web.http.GemFireHttpSessionConfiguration;
import org.springframework.test.context.ContextConfiguration;
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
@ContextConfiguration
@SuppressWarnings("unused")
public class SessionSerializationWithDataSerializationDeltasAndJavaSerializationIntegrationTests {

  @Autowired
  private ClientCache clientCache;

  @Autowired
  private SessionRepository<Session> sessionRepository;

  @Before
  public void assertGemFireConfiguration() {

    assertThat(this.clientCache).isNotNull();
    assertThat(this.clientCache.getPdxSerializer()).isNull();
    assertThat(isCustomerSerializerRegistered()).isFalse();
  }

  private boolean isCustomerSerializerRegistered() {

    return Arrays.stream(nullSafeArray(InternalDataSerializer.getSerializers(), DataSerializer.class))
      .filter(Objects::nonNull)
      .anyMatch(isCustomerSerializer());
  }

  private Predicate<DataSerializer> isCustomerSerializer() {

    return dataSerializer ->
      Arrays.stream(nullSafeArray(dataSerializer.getSupportedClasses(), Class.class))
        .filter(Objects::nonNull)
        .anyMatch(Customer.class::equals);
  }

  @Test
  public void sessionWithCustomerSerializationWorks() {

    Session session = this.sessionRepository.createSession();

    assertThat(session).isNotNull();
    assertThat(session.getId()).isNotEmpty();
    assertThat(session.isExpired()).isFalse();
    assertThat(session.getAttributeNames()).isEmpty();

    Customer jonDoe = Customer.newCustomer("Jon Doe")
      .asMale()
      .using(Phone.newPhone("503-555-1234").asMobile());

    session.setAttribute("jonDoe", jonDoe);

    assertThat(session.getAttributeNames()).containsOnly("jonDoe");
    assertThat(session.<Customer>getAttribute("jonDoe")).isEqualTo(jonDoe);

    this.sessionRepository.save(session);

    Customer janeDoe = Customer.newCustomer("Jane Doe")
      .asFemale()
      .using(Phone.newPhone("503-555-9876"));

    session.setAttribute("janeDoe", janeDoe);

    assertThat(session.getAttributeNames()).containsOnly("jonDoe", "janeDoe");
    assertThat(session.<Customer>getAttribute("jonDoe")).isEqualTo(jonDoe);
    assertThat(session.<Customer>getAttribute("janeDoe")).isEqualTo(janeDoe);

    this.sessionRepository.save(session);

    Session loadedSession = this.sessionRepository.findById(session.getId());

    assertThat(loadedSession).isEqualTo(session);
    assertThat(loadedSession).isNotSameAs(session);
    assertThat(loadedSession.getAttributeNames()).containsOnly("jonDoe", "janeDoe");
    assertThat(loadedSession.<Customer>getAttribute("jonDoe")).isEqualTo(jonDoe);
    assertThat(loadedSession.<Customer>getAttribute("janeDoe")).isEqualTo(janeDoe);
  }

  @ClientCacheApplication(subscriptionEnabled = true)
  @EnableGemFireHttpSession(
    poolName = "DEFAULT",
    regionName = "Sessions",
    sessionSerializerBeanName = GemFireHttpSessionConfiguration.SESSION_DATA_SERIALIZER_BEAN_NAME
  )
  static class GemFireClientConfiguration { }

}
