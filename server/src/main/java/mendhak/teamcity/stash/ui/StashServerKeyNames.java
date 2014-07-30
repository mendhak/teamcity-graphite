/*
*    This file is part of TeamCity Stash.
*
*    TeamCity Stash is free software: you can redistribute it and/or modify
*    it under the terms of the GNU General Public License as published by
*    the Free Software Foundation, either version 3 of the License, or
*    (at your option) any later version.
*
*    TeamCity Stash is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public License
*    along with TeamCity Stash.  If not, see <http://www.gnu.org/licenses/>.
*/


package mendhak.teamcity.stash.ui;

import jetbrains.buildServer.agent.Constants;

//These methods must follow a specific naming pattern
//If your feature.jsp has a key called ${keys.graphitePrefix},
//the method must be getGraphitePrefix()

public class StashServerKeyNames
{
    public String getServerKey()
    {
        return "graphite_host";
    }

    public String getServerPort()
    {
        return "graphite_port";
    }


    public String getGraphitePrefix()
    {
        return "graphite_prefix";
    }

    public String getSendBuildStarted()
    {
        return "graphite_buildstarted";
    }

    public String getSendBuildFinished()
    {
        return "graphite_buildfinished";
    }

    public String getVCSIgnoreKey()
    {
        return "stash_vcsignorecsv";
    }






}
