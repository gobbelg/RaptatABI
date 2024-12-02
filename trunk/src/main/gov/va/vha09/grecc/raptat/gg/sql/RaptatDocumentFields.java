/** */
package src.main.gov.va.vha09.grecc.raptat.gg.sql;

import java.util.Optional;
import src.main.gov.va.vha09.grecc.raptat.gg.textanalysis.RaptatDocument;

/**
 * Class used to build RaptatDocument object from fields in SQL database
 *
 * @author Glenn Gobbel
 */
public class RaptatDocumentFields {
  public final String textSource;

  private RaptatDocumentFields(String textSource) {
    this.textSource = textSource;
  }


  private RaptatDocument getRaptatDocumentFromFields() {
    RaptatDocument document = new RaptatDocument();
    document.setTextSourcePath(Optional.of(this.textSource));

    return document;
  }
}
