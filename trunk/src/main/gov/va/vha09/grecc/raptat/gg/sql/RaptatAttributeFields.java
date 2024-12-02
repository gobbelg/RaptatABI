/** */
package src.main.gov.va.vha09.grecc.raptat.gg.sql;

import java.util.List;

/**
 * Class used to build RaptatAttribute object from fields
 *
 * @author Glenn Gobbel
 */
public class RaptatAttributeFields {
  public final String id;
  public final String typeName;
  public final List<String> values;


  /**
   * @param id
   * @param typeName
   * @param values
   */
  public RaptatAttributeFields(String id, String typeName, List<String> values) {
    super();
    this.id = id;
    this.typeName = typeName;
    this.values = values;
  }
}
