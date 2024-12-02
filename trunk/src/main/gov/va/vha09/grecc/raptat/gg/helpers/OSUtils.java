package src.main.gov.va.vha09.grecc.raptat.gg.helpers;

/**
 * ***************************************** Keeps track of the operating system
 *
 * @author Glenn Gobbel - March 8, 2012
 *         <p>
 *         *****************************************
 */
public final class OSUtils {
  private static String os = null;


  public static String getOsName() {
    if (OSUtils.os == null) {
      OSUtils.os = System.getProperty("os.name").toLowerCase();
    }
    return OSUtils.os;
  }


  public static boolean isLinux() {
    return getOsName().startsWith("linux");
  }


  public static boolean isMac() {
    return getOsName().startsWith("mac");
  }


  public static boolean isSunOS() {
    return getOsName().startsWith("sunos");
  }


  public static boolean isUnix() {
    if (isLinux() || isSunOS()) {
      return true;
    }
    if (isMac() && System.getProperty("os.version", "").startsWith("10.")) {
      return true;
    }
    return false;
  }


  public static boolean isWindows() {
    return getOsName().startsWith("windows");
  }
}
