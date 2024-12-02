/** */
package src.main.gov.va.vha09.grecc.raptat.gg.core.attributeconflictmanagers;

/**
 * Indicates methods that can be used to assign an attribute to an AnnotatedPhrase object.
 *
 * @author Glenn T. Gobbel Jul 22, 2016
 */
public enum AttributeAssignmentMethod {
  // @formatter:off

  /*
   * A reference assignment generally indicates that the attribute was assigned
   * from a reference standard that was imported into Raptat
   */
  REFERENCE,

  CONTEXTUAL,
  PROBABILISTIC,
  CUE_SCOPE_CRF,

  /* A resolved assignment indicates that the
   * attribute was assigned by an AttributeConflictManager
   * that resolves conflicts between values for an attribute
   * of the same name/type */
  RESOLVED,

  /* An SQL assignment means that it was pulled from a SQL database */
  SQL;
  // @formatter:on

}
