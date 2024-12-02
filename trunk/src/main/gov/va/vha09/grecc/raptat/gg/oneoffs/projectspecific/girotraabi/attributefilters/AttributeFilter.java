/**
 *
 */
package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs.projectspecific.girotraabi.attributefilters;

import weka.core.Attribute;

/**
 * @author VHATVHGOBBEG
 *
 */
public interface AttributeFilter {

  public boolean acceptAttribute(Attribute attribute);

  public <T extends Object> void initialize(T instanceInitializer);

}
