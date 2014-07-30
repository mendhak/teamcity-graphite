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
import jetbrains.buildServer.util.StringUtil;
import mendhak.teamcity.graphite.ui.GraphiteBuildFeature;
import mendhak.teamcity.graphite.ui.GraphiteServerKeyNames;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.*;


public class BuildStatusListener
{
    @NotNull
    private final ChangeStatusUpdater updater;

    final GraphiteServerKeyNames keyNames = new GraphiteServerKeyNames();

    private String getGraphitePrefix()
    {
        return "flight.gogdhak";
    }

    private void pushStatToGraphite(@NotNull String host, @NotNull int port, @NotNull String metricName, @NotNull String metricValue, @NotNull long metricTimestamp)
    {
        //TODO Consider refactoring to its own individual class
        try {
            Socket socket = new Socket(host, port);
            PrintWriter outputStream = new PrintWriter(socket.getOutputStream());
            outputStream.println(String.format("%s %s %s", metricName, metricValue, metricTimestamp));

            outputStream.close();
            socket.close();
        }
        catch (Exception e) { }
    }

    public BuildStatusListener(@NotNull final EventDispatcher<BuildServerListener> listener,
                               @NotNull final ChangeStatusUpdater updater)
    {
        this.updater = updater;
        listener.addListener(new BuildServerAdapter()
        {
            @Override
            public void changesLoaded(SRunningBuild build)
            {
                updateBuildStatus(build, true);
            }

            @Override
            public void buildFinished(SRunningBuild build)
            {
                try {
                    String metricName = getGraphitePrefix() + ".finished";
                    String metricValue = String.valueOf(build.getDuration());

                    pushStatToGraphite("127.0.0.1", 2003, metricName, metricValue, System.currentTimeMillis() / 1000);
                }
                catch(Exception e) { }

                updateBuildStatus(build, false);
            }

            @Override
            public void buildInterrupted(SRunningBuild build)
            {
                updateBuildStatus(build, false);
            }

            @Override
            public void statisticValuePublished(@NotNull SBuild build, @NotNull String valueTypeKey, @NotNull BigDecimal value) {
                build.getParametersProvider().get(keyNames.getVCSIgnoreKey());
                super.statisticValuePublished(build, valueTypeKey, value);
                Logger.LogInfo(valueTypeKey + " : " + String.valueOf(value));
            }
        });
    }

    private void updateBuildStatus(@NotNull final SRunningBuild build, boolean isStarting)
    {
        SBuildType buildType = build.getBuildType();
        if (buildType == null)
        {
            return;
        }

        for (SBuildFeatureDescriptor feature : buildType.getBuildFeatures())
        {
            if (!feature.getType().equals(GraphiteBuildFeature.FEATURE_TYPE))
            {
                continue;
            }


            final ChangeStatusUpdater.Handler h = updater.getUpdateHandler(feature);

            List<String> changes = getLatestChanges(build, feature);

            if (changes.isEmpty())
            {
                Logger.LogInfo("No revisions were found to update Stash with. Build Id:" + String.valueOf(build.getBuildId()));
            }

            for (String change : changes)
            {
                if (isStarting)
                {
                    h.scheduleChangeStarted(change, build);
                }
                else
                {
                    h.scheduleChangeCompleted(change, build);
                }
            }
        }
    }

    private List<String> getLatestChanges( final SRunningBuild build, final SBuildFeatureDescriptor feature)
    {
        final List<String> revisions = new ArrayList<String>();

        for(BuildRevision revision : build.getRevisions())
        {
            if(ShouldIgnoreVCSRoot(revision, feature))
            {
                Logger.LogInfo("VCS Root " + revision.getRoot().getName() + " is being ignored.");
                continue;
            }

            String revisionHash = revision.getRevision();
            if(revisionHash.contains("@"))
            {
                revisionHash = revisionHash.substring(0,revisionHash.indexOf("@"));
            }

            revisions.add(revisionHash);
        }

        return revisions;
    }

    private boolean ShouldIgnoreVCSRoot(final BuildRevision revision, final SBuildFeatureDescriptor feature)
    {
        String ignoreRootsCSV = feature.getParameters().get(keyNames.getVCSIgnoreKey());

        if(StringUtil.isEmptyOrSpaces(ignoreRootsCSV))
        {
            return false;
        }

        ignoreRootsCSV =  ignoreRootsCSV.replace(" ","").toLowerCase();

        List<String> ignoreRoots = Arrays.asList(ignoreRootsCSV.split("\\s*,\\s*"));

        String currentRoot = revision.getRoot().getName().replace(" ","").toLowerCase();

        return (ignoreRoots.size() > 0 && ignoreRoots.contains(currentRoot));

    }


}
