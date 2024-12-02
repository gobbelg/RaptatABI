/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package src.main.gov.va.vha09.grecc.raptat.ss.sql.exception;

/** @author VHATVHSAHAS1 */
public class NoDataException extends Exception {
  /** */
  private static final long serialVersionUID = 1L;


  public NoDataException(String msg) {
    super(msg);
  }


  public NoDataException(String msg, Throwable throwable) {
    super(msg, throwable);
  }
}
