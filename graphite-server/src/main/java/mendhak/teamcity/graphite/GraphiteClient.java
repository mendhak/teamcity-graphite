
package mendhak.teamcity.graphite;

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.executors.ExecutorServices;
import mendhak.teamcity.graphite.ui.GraphiteServerKeyNames;
import org.jetbrains.annotations.NotNull;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


public class GraphiteClient
{
    private final ExecutorService executor;



    public GraphiteClient(@NotNull final ExecutorServices services)
    {
        executor = services.getLowPriorityExecutorService();
    }


    public static interface Handler
    {
        void scheduleBuildMetric(@NotNull final SBuild build,
                                 @NotNull GraphiteMetric metric);
    }

    @NotNull
    public Handler getUpdateHandler()
    {
        final GraphiteServerKeyNames keyNames = new GraphiteServerKeyNames();

        return new Handler()
        {
            public void scheduleBuildMetric(@NotNull final SBuild build,
                                            @NotNull final GraphiteMetric metric) {

                executor.submit(new Runnable() {
                    public void run() {
                        try {
                            // Let's collect the target server and port configurred to send the messages; they can't be
                            // empty to be able to send the messages. Although this is reinforced via the GUI, we verify
                            // here too... If they are empty we will silently ignore as it is poinetless to send messages
                            // to an empty server or a port number... :-)
                            String host = build.getParametersProvider().get(keyNames.getServerKey());
                            String stringPort = build.getParametersProvider().get(keyNames.getServerPort());
                            if (host == null || stringPort ==null ) { return; }
                            int port = Integer.valueOf(stringPort);  // port number must be an integer

                            // let's collect the prefix and suffix of the metric key name to be constructed
                            String metricPrefix = build.getParametersProvider().get(keyNames.getGraphitePrefix());
                            String metricSuffix = build.getParametersProvider().get(keyNames.getGraphiteSuffix());
                            String metricSuffixWithDot = metricSuffix.equals("") ? "" : "." + metricSuffix;

                            // Let's send the data now, for StatsD it is UDP packet and for Carbon it is TCP packet
                            boolean useUdp = Boolean.valueOf(build.getParametersProvider().get(keyNames.getUseUdp()));
                            if(useUdp)
                            {
                                // StatsD over UDP
                                boolean sendTimers      = Boolean.valueOf(build.getParametersProvider().get(keyNames.getSendTimers()));
                                DatagramSocket sock     = new DatagramSocket();
                                InetAddress addr        = InetAddress.getByName(host);
                                // "xyz.abc.def:1|c" or "xyz.abc.def:1000|ms"
                                String metricTypeSuffix = sendTimers && metric.isTimer() ? "ms" : "c";
                                byte[] message          = String.format("%s:%s|%s", metricPrefix + "." + metric.getName() + metricSuffixWithDot, metric.getValue(), metricTypeSuffix).toLowerCase().getBytes();
                                DatagramPacket packet   = new DatagramPacket(message, message.length, addr, port);
                                sock.send(packet);
                                sock.close();
                            }
                            else
                            {
                                // Graphite over TCP
                                Socket socket = new Socket(host, port);
                                PrintWriter outputStream = new PrintWriter(socket.getOutputStream());
                                outputStream.println(String.format("%s %s %s", metricPrefix + "." + metric.getName() + metricSuffixWithDot, metric.getValue(), metric.getTimestamp()).toLowerCase());

                                outputStream.close();
                                socket.close();
                            }
                        } catch (Exception e) {
                            Logger.LogError("Could not send packet to Graphite", e);
                        }
                    }
                });
            }

        };
    }
}
