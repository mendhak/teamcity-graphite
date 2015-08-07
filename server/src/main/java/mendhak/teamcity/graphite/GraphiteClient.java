
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

                            boolean useUdp = Boolean.valueOf(build.getParametersProvider().get(keyNames.getUseUdp()));
                            String host = build.getParametersProvider().get(keyNames.getServerKey());
                            int port = Integer.valueOf(build.getParametersProvider().get(keyNames.getServerPort()));
                            String metricPrefix = build.getParametersProvider().get(keyNames.getGraphitePrefix());
                            String metricSuffix = build.getParametersProvider().get(keyNames.getGraphiteSuffix());
                            String metricSuffixWithDot = metricSuffix.equals("") ? "" : "." + metricSuffix;

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
