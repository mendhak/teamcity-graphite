
package mendhak.teamcity.graphite;

public class GraphiteMetric {

    String name;
    String value;
    long timestamp;
    boolean isTimer;

    public GraphiteMetric(String name, String value, long timeStamp, boolean isTimer){
        this.name = name;
        this.value = value;
        this.timestamp = timeStamp;
        this.isTimer = isTimer;
    }

    public String getName(){
        return name;
    }

    public String getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isTimer() {
        return isTimer;
    }
}
