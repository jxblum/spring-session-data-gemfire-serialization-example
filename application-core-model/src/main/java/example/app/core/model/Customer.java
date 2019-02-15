package example.app.core.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import example.app.core.enums.Gender;
import example.app.ext.model.Phone;

/**
 * The Customer class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Data
@ToString(of = { "name"})
@EqualsAndHashCode(of = { "name" })
@RequiredArgsConstructor(staticName = "newCustomer")
@SuppressWarnings("unused")
public class Customer implements Serializable {

  private Gender gender;

  private Phone phone;

  @NonNull
  private String name;

  public Customer asFemale() {
    return as(Gender.FEMALE);
  }

  public Customer asMale() {
    return as(Gender.MALE);
  }

  public Customer as(Gender gender) {
    setGender(gender);
    return this;
  }

  public Customer using(Phone phone) {
    setPhone(phone);
    return this;
  }
}
