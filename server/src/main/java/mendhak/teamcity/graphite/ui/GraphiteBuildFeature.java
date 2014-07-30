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


package mendhak.teamcity.graphite.ui;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class GraphiteBuildFeature extends BuildFeature
{
    public static final String FEATURE_TYPE = "teamcity.graphite.status";
    private final PluginDescriptor descriptor;

    public GraphiteBuildFeature(@NotNull final PluginDescriptor descriptor)
    {
        this.descriptor = descriptor;
    }

    @NotNull
    @Override
    public String getType()
    {
        return FEATURE_TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName()
    {
        return "Send metrics to Graphite";
    }

    @Nullable
    @Override
    public String getEditParametersUrl()
    {
        return descriptor.getPluginResourcesPath("feature.html");
    }

    @NotNull
    @Override
    public String describeParameters(@NotNull Map<String, String> params)
    {
        return "Build metrics will be sent to " + params.get(new GraphiteServerKeyNames().getServerKey()) + ":" + params.get(new GraphiteServerKeyNames().getServerPort());

    }

    @Nullable
    @Override
    public PropertiesProcessor getParametersProcessor()
    {
        final GraphiteServerKeyNames keyNames = new GraphiteServerKeyNames();
        return new PropertiesProcessor()
        {
            private void validate(@NotNull final Map<String, String> properties,
                                  @NotNull final String key,
                                  @NotNull final String message,
                                  @NotNull final Collection<InvalidProperty> res)
            {
                if (jetbrains.buildServer.util.StringUtil.isEmptyOrSpaces(properties.get(key)))
                {
                    res.add(new InvalidProperty(key, message));
                }
            }

            @NotNull
            public Collection<InvalidProperty> process(@Nullable final Map<String, String> propertiesMap)
            {
                final Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();
                if (propertiesMap == null)
                {
                    return result;
                }


                validate(propertiesMap, keyNames.getServerKey(), "Graphite server cannot be empty", result);

                return result;
            }
        };
    }

    @Nullable
    @Override
    public Map<String, String> getDefaultParameters()
    {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(new GraphiteServerKeyNames().getServerKey(), "127.0.0.1");
        map.put(new GraphiteServerKeyNames().getServerPort(), "8125");
        map.put(new GraphiteServerKeyNames().getGraphitePrefix(), "test.build.myapi");
        map.put(new GraphiteServerKeyNames().getSendBuildStarted(), "true");
        map.put(new GraphiteServerKeyNames().getSendBuildFinished(), "true");
        return map;
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed()
    {
        return true;
    }
}
