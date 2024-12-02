package src.main.gov.va.vha09.grecc.raptat.gg.oneoffs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager;

/**
 * ****************************************************** Test to demonstrate the use of reflection
 * to get and use an instance of the OptionsManager based on a string
 *
 * @author Glenn Gobbel - Jul 17, 2013 *****************************************************
 */
public class ReflectionTest {

  public ReflectionTest() {
    // TODO Auto-generated constructor stub
  }


  public static void main(String[] args) {
    try {
      // Put in the fully qualified name as a string to get the class
      // itself
      Class<?> optionsClass =
          Class.forName("src.main.gov.va.vha09.grecc.raptat.gg.core.options.OptionsManager");

      // To get the method for the class, the name of the method is the
      // first parameter,
      // supplied as a a string. If the method takes parameters, the
      // classes of each parameter
      // should be supplied as addition parameters to the getMethod()
      // method
      Method getInstanceMethod = optionsClass.getMethod("getInstance");

      // We call this method. In this case, the first parameter is 'null'
      // because the method
      // is static. Otherwise, we would have needed to supply the class of
      // the method
      OptionsManager theManager = (OptionsManager) getInstanceMethod.invoke(null);

      // Now we have an instance and can call methods
      theManager.getAnnotatedFileNames();
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      System.out.println(e);
      e.printStackTrace();
    }
  }
}
