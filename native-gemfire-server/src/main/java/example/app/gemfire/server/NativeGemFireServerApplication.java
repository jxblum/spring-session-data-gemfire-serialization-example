package example.app.gemfire.server;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.ExpirationAction;
import org.apache.geode.cache.ExpirationAttributes;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionFactory;
import org.apache.geode.cache.RegionShortcut;
import org.apache.geode.cache.server.CacheServer;
import org.springframework.session.Session;
import org.springframework.session.data.gemfire.serialization.data.support.DataSerializableSessionSerializerInitializer;

/**
 * The NativeGemFireServerApplication class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class NativeGemFireServerApplication {

  private static final int GEMFIRE_CACHE_SERVER_PORT = CacheServer.DEFAULT_PORT;

  private static final long SESSION_EXPIRATION_TIMEOUT = TimeUnit.MINUTES.toSeconds(30);

  private static final String GEMFIRE_CACHE_SERVER_BIND_ADDRESS = "localhost";
  private static final String GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS = GEMFIRE_CACHE_SERVER_BIND_ADDRESS;
  private static final String GEMFIRE_LOCATOR = "localhost[10334]";
  private static final String GEMFIRE_LOG_LEVEL = "config";
  private static final String GEMFIRE_MANAGER = "true";
  private static final String SESSIONS_REGION_NAME = "Sessions";

  public static void main(String[] args) throws Exception {

    initializeDataSerialization(registerShutdownHook(sessionsRegion(gemfireCacheServer(
      gemfireCache(gemfireProperties())))));
  }

  private static Properties gemfireProperties() {

    Properties gemfireProperties = new Properties();

    gemfireProperties.setProperty("name", "NativeSessionGemFireSerializationServer");
    gemfireProperties.setProperty("log-level", GEMFIRE_LOG_LEVEL);
    gemfireProperties.setProperty("jmx-manager", GEMFIRE_MANAGER);
    gemfireProperties.setProperty("jmx-manager-start", GEMFIRE_MANAGER);
    gemfireProperties.setProperty("start-locator", GEMFIRE_LOCATOR);

    return gemfireProperties;
  }

  private static Cache gemfireCache(Properties gemfireProperties) {
    return new CacheFactory(gemfireProperties).create();
  }

  private static Cache gemfireCacheServer(Cache gemfireCache) throws IOException {

    CacheServer cacheServer = gemfireCache.addCacheServer();

    cacheServer.setBindAddress(GEMFIRE_CACHE_SERVER_BIND_ADDRESS);
    cacheServer.setHostnameForClients(GEMFIRE_CACHE_SERVER_HOSTNAME_FOR_CLIENTS);
    cacheServer.setPort(GEMFIRE_CACHE_SERVER_PORT);
    cacheServer.start();

    return gemfireCache;
  }

  private static Cache sessionsRegion(Cache gemfireCache) {

    RegionFactory sessionsRegionFactory = gemfireCache.createRegionFactory(RegionShortcut.PARTITION);

    sessionsRegionFactory.setKeyConstraint(String.class);
    sessionsRegionFactory.setEntryIdleTimeout(sessionsRegionExpirationPolicy());
    sessionsRegionFactory.setValueConstraint(Session.class);

    Region<String, Session> sessionsRegion = sessionsRegionFactory.create(SESSIONS_REGION_NAME);

    return gemfireCache;
  }

  private static ExpirationAttributes sessionsRegionExpirationPolicy() {
    return new ExpirationAttributes(Long.valueOf(SESSION_EXPIRATION_TIMEOUT).intValue(),
      ExpirationAction.INVALIDATE);
  }

  private static Cache registerShutdownHook(Cache gemfireCache) {

    Runtime.getRuntime()
      .addShutdownHook(new Thread(gemfireCache::close, "GemFire Cache Close Thread"));

    return gemfireCache;
  }

  private static Cache initializeDataSerialization(Cache gemfireCache) {

    new DataSerializableSessionSerializerInitializer(gemfireCache).doInitialization();

    return gemfireCache;
  }
}
