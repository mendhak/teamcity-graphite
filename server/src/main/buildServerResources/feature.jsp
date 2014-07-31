<%@ include file="/include-internal.jsp"%>
<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%--
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
  --%>

<jsp:useBean id="keys" class="mendhak.teamcity.graphite.ui.GraphiteServerKeyNames"/>

<tr>
  <td colspan="2">Specify Graphite server details and what to send</td>
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
    <span class="smallNote">Specify Graphite/StatsD Port (eg. 2003, 8125)</span>
  </td>
</tr>

<tr>
  <th>Use UDP</th>
  <td>
    <props:checkboxProperty name="${keys.useUdp}" />
    <label for="${keys.useUdp}">Check for UDP, uncheck for TCP</label>
    <span class="smallNote"> </span>
  </td>
</tr>

</l:settingsGroup>
<l:settingsGroup title="What to send">
<tr>
  <th>Prefix</th>
  <td>
    <props:textProperty name="${keys.graphitePrefix}" />
    <label for="${keys.graphitePrefix}"></label>
    <span class="smallNote">Prefix to use for metrics, eg: build.myapi</span>
  </td>
</tr>
<tr>
  <th>Send Build Start</th>
  <td>
    <props:checkboxProperty name="${keys.sendBuildStarted}" />
    <label for="${keys.sendBuildStarted}">Send 'started' metric</label>
    <span class="smallNote"> </span>
  </td>
</tr>
<tr>
  <th>Send Build Finished</th>
  <td>
    <props:checkboxProperty name="${keys.sendBuildFinished}" />
    <label for="${keys.sendBuildFinished}">Send 'finished' metric</label>
    <span class="smallNote"> </span>
  </td>
</tr>

</l:settingsGroup>
