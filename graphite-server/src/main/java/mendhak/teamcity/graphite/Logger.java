
package mendhak.teamcity.graphite;

public class Logger
{

    public static void LogError(String message, Exception ex)
    {
        System.err.println("TEAMCITY-GRAPHITE::: " + message + "\r\n" +  ex.getMessage() + "\r\n" + ex.getStackTrace());
    }

    public static void LogInfo(String message)
    {
        System.out.println("TEAMCITY-GRAPHITE::: " + message);
    }
}
