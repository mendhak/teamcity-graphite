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
import jetbrains.buildServer.serverSide.userChanges.CanceledInfo;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.StringUtil;
import mendhak.teamcity.graphite.ui.GraphiteBuildFeature;
import mendhak.teamcity.graphite.ui.GraphiteServerKeyNames;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;


public class GraphiteClient
{
    private final ExecutorService myExecutor;
    @NotNull

    private final WebLinks myWeb;
    private final BuildsManager buildsManager;
    private final UserModel users;

    public GraphiteClient(@NotNull final ExecutorServices services,
                          @NotNull final WebLinks web,
                          @NotNull final UserModel userModel,
                          BuildsManager manager)
    {
        myWeb = web;
        myExecutor = services.getLowPriorityExecutorService();
        buildsManager = manager;
        this.users = userModel;
    }




    public static interface Handler
    {
        void scheduleBuildMetric(@NotNull final SBuild build,
                                 @NotNull String metricName, @NotNull String metricValue, @NotNull long metricTimestamp);


    }

    @NotNull
    public Handler getUpdateHandler()
    {


        final GraphiteServerKeyNames keyNames = new GraphiteServerKeyNames();


        return new Handler()
        {

            public void scheduleBuildMetric(@NotNull SBuild build,
                                            @NotNull String metricName, @NotNull String metricValue, @NotNull long metricTimestamp) {

                try {

                    String host = build.getParametersProvider().get(keyNames.getServerKey());
                    int port = Integer.valueOf(build.getParametersProvider().get(keyNames.getServerPort()));
                    String metricPrefix = build.getParametersProvider().get(keyNames.getGraphitePrefix());

                    Socket socket = new Socket(host, port);
                    PrintWriter outputStream = new PrintWriter(socket.getOutputStream());
                    outputStream.println(String.format("%s %s %s", metricPrefix + "." + metricName, metricValue, metricTimestamp));

                    outputStream.close();
                    socket.close();
                }
                catch (Exception e) {
                    Logger.LogError("Could not send packet to Graphite", e);
                }
            }



            private void scheduleChangeUpdate(@NotNull final String hash,
                                              @NotNull final SRunningBuild build
                                              )
            {
                Logger.LogInfo("Scheduling Stash status update for hash: " + hash + ", buildId: "
                        + build.getBuildId() );

                myExecutor.submit(ExceptionUtil.catchAll("set change status on Stash", new Runnable() {
                    public void run() {

//                        GraphiteClient client = new GraphiteClient(build.getParametersProvider().get(keyNames.getServerKey()),
//                                feature.getParameters().get(keyNames.getServerPort()),
//                                feature.getParameters().get(keyNames.getGraphitePrefix()));
//
//                        boolean onlyShowLatestBuild =false;
//                                //Boolean.valueOf(feature.getParameters().get(keyNames.getOnlyLatestKey()));
//                        client.SendBuildStatus(status,
//                                onlyShowLatestBuild ? build.getBuildTypeId() : String.valueOf(build.getBuildId()),
//                                getBuildDisplayName(build), myWeb.getViewResultsUrl(build),
//                                getBuildDisplayDescription(build), hash);

                        Logger.LogInfo("Updated Stash status for revision: " + hash + ", buildId: " + build.getBuildId());

                    }
                }));
            }
        };
    }
}
