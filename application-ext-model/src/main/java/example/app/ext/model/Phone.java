package example.app.ext.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import example.app.core.enums.PhoneType;

/**
 * The Phone class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Data
@ToString(of = { "number" })
@EqualsAndHashCode(of = { "number" })
@RequiredArgsConstructor(staticName = "newPhone")
@SuppressWarnings("unused")
public class Phone implements Serializable {

  private PhoneType type;

  @NonNull
  private String number;

  public Phone asHome() {
    return as(PhoneType.HOME);
  }

  public Phone asMobile() {
    return as(PhoneType.MOBILE);
  }

  public Phone asWork() {
    return as(PhoneType.WORK);
  }

  public Phone as(PhoneType type) {
    setType(type);
    return this;
  }
}
