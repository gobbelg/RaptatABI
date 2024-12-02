package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

import org.apache.commons.lang3.builder.StandardToStringStyle;

public class RaptatStringStyle {
  private static final long serialVersionUID = -1864918035136071511L;

  private static StandardToStringStyle raptatStyleInstance;;


  private RaptatStringStyle() {
    RaptatStringStyle.raptatStyleInstance = new StandardToStringStyle();
    RaptatStringStyle.raptatStyleInstance.setUseIdentityHashCode(false);
    RaptatStringStyle.raptatStyleInstance.setUseClassName(false);
    RaptatStringStyle.raptatStyleInstance.setContentStart("[");
    RaptatStringStyle.raptatStyleInstance.setFieldSeparator(",  ");
    RaptatStringStyle.raptatStyleInstance.setFieldSeparatorAtStart(false);
    RaptatStringStyle.raptatStyleInstance.setContentEnd("]");
  }


  public static synchronized StandardToStringStyle getInstance() {
    if (RaptatStringStyle.raptatStyleInstance == null) {
      new RaptatStringStyle();
    }
    return RaptatStringStyle.raptatStyleInstance;
  }
}
