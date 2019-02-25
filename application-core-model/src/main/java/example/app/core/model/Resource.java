package example.app.core.model;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The Resource class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class Resource {

  private final Builder builder;

  protected Resource(Builder builder) {

    assertThat(builder).describedAs("Builder is required").isNotNull();

    this.builder = builder;
  }

  public Resource init() {

    if (Boolean.getBoolean("example.app.resource.failure")) {
      throw new RuntimeException("TEST");
    }

    return this;
  }

  public static class Builder {

    public static Builder create() {
      return new Builder();
    }

    public Builder() { }

    public Resource build() {
      return new Resource(this);
    }
  }
}
