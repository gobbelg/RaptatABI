package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

/**
 * ******************************************************* Thrown during construction of an
 * ListInputStream to indicate that it is trying to open a file that was not written by an
 * ListOutputStream, which is the only file type that an InterableInputStream instance should read
 *
 * @author Glenn Gobbel - Feb 16, 2012 *******************************************************
 */
public class NotListOutputStreamFile extends Exception {
  private static final long serialVersionUID = 2802956172226992807L;


  public NotListOutputStreamFile(String msg) {
    super(msg);
  }
}
