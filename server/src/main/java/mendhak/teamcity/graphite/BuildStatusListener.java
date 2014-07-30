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
import jetbrains.buildServer.util.EventDispatcher;
import mendhak.teamcity.graphite.ui.GraphiteServerKeyNames;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;


public class BuildStatusListener
{
    @NotNull
    private final GraphiteClient updater;

    final GraphiteServerKeyNames keyNames = new GraphiteServerKeyNames();

//    private String getGraphitePrefix()
//    {
//        return "flight.gogdhak";
//    }

//    private void pushStatToGraphite(@NotNull String host, @NotNull int port, @NotNull String metricName, @NotNull String metricValue, @NotNull long metricTimestamp)
//    {
//        //TODO Consider refactoring to its own individual class
//        try {
//            Socket socket = new Socket(host, port);
//            PrintWriter outputStream = new PrintWriter(socket.getOutputStream());
//            outputStream.println(String.format("%s %s %s", metricName, metricValue, metricTimestamp));
//
//            outputStream.close();
//            socket.close();
//        }
//        catch (Exception e) { }
//    }

    public BuildStatusListener(@NotNull final EventDispatcher<BuildServerListener> listener,
                               @NotNull final GraphiteClient graphiteClient)
    {
        this.updater = graphiteClient;
        listener.addListener(new BuildServerAdapter()
        {

            final GraphiteClient.Handler h = graphiteClient.getUpdateHandler();

            @Override
            public void changesLoaded(SRunningBuild build)
            {
                //updateBuildStatus(build, true);
            }

            @Override
            public void buildFinished(SRunningBuild build)
            {
                try {

                    String metricName = "finished";
                    String metricValue = String.valueOf(build.getDuration());
                    h.scheduleBuildMetric(build, metricName, metricValue,  System.currentTimeMillis() / 1000 );
                }
                catch(Exception e) { }

            }

            @Override
            public void buildInterrupted(SRunningBuild build)
            {

            }

            @Override
            public void statisticValuePublished(@NotNull SBuild build, @NotNull String valueTypeKey, @NotNull BigDecimal value) {
                super.statisticValuePublished(build, valueTypeKey, value);
                String metricName = valueTypeKey.replace(":",".");

                h.scheduleBuildMetric(build, metricName, String.valueOf(value), System.currentTimeMillis()/1000 );
                //build.getParametersProvider().get(keyNames.getSendBuildStarted());

                Logger.LogInfo(valueTypeKey + " : " + String.valueOf(value));
            }
        });
    }


}
