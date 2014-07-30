<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%--
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
  --%>

<jsp:useBean id="keys" class="mendhak.teamcity.stash.ui.StashServerKeyNames"/>

<tr>
  <td colspan="2">Specify Stash repository name and credentials to push status updates to</td>
</tr>
<l:settingsGroup title="Graphite Server Details">
<tr>
  <th>Server<l:star/></th>
  <td>
    <props:textProperty name="${keys.serverKey}" className="longField"/>
    <span class="error" id="error_${keys.serverKey}"></span>
    <span class="smallNote">Specify Graphite Server</span>
  </td>
</tr>

<tr>
  <th>Port<l:star/></th>
  <td>
    <props:textProperty name="${keys.serverPort}" className="longField"/>
    <span class="error" id="error_${keys.serverPort}"></span>
    <span class="smallNote">Specify Graphite Port for UDP connections</span>
  </td>
</tr>

</l:settingsGroup>
<l:settingsGroup title="Other">
<tr>
  <th>Prefix:</th>
  <td>
    <props:textProperty name="${keys.graphitePrefix}" />
    <label for="${keys.graphitePrefix}">Prefix to use for metrics</label>
    <span class="smallNote">build.myapi, for example</span>
  </td>
</tr>
<tr>
  <th>Send Build Start:</th>
  <td>
    <props:checkboxProperty name="${keys.sendBuildStarted}" />
    <label for="${keys.sendBuildStarted}">Sends indicator that build has started</label>
    <span class="smallNote"> </span>
  </td>
</tr>
<tr>
  <th>Send Build Finished:</th>
  <td>
    <props:checkboxProperty name="${keys.sendBuildFinished}" />
    <label for="${keys.sendBuildFinished}">Sends indicator that build has finished</label>
    <span class="smallNote"> </span>
  </td>
</tr>
<tr>
  <th>...:</th>
  <td>
    <props:textProperty name="${keys.VCSIgnoreKey}" className="longField"/>
    <span class="error" id="error_${keys.VCSIgnoreKey}"></span>
    <span class="smallNote">(Optional) VCS root names to ignore, comma separated</span>
  </td>
</tr>
</l:settingsGroup>
