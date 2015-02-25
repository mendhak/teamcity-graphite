/*
*    This file is part of TeamCity Graphite.
*
*    TeamCity Graphite is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    TeamCity Graphite is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with TeamCity Graphite.  If not, see <http://www.gnu.org/licenses/>.
*/


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

                            if(useUdp)
                            {
                                // StatsD over UDP
                                boolean sendTimers      = Boolean.valueOf(build.getParametersProvider().get(keyNames.getSendTimers()));
                                DatagramSocket sock     = new DatagramSocket();
                                InetAddress addr        = InetAddress.getByName(host);
                                // "xyz.abc.def:1|c" or "xyz.abc.def:1000|ms"
                                String metricTypeSuffix = sendTimers && metric.isTimer() ? "ms" : "c";
                                byte[] message          = String.format("%s:%s|%s", metricPrefix + "." + metric.getName() , metric.getValue(), metricTypeSuffix).toLowerCase().getBytes();
                                DatagramPacket packet   = new DatagramPacket(message, message.length, addr, port);
                                sock.send(packet);
                                sock.close();
                            }
                            else
                            {
                                // Graphite over TCP
                                Socket socket = new Socket(host, port);
                                PrintWriter outputStream = new PrintWriter(socket.getOutputStream());
                                outputStream.println(String.format("%s %s %s", metricPrefix + "." + metric.getName(), metric.getValue(), metric.getTimestamp()).toLowerCase());

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
