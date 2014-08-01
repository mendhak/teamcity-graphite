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
  <th>Prefix<l:star/></th>
  <td>
    <props:textProperty name="${keys.graphitePrefix}" />
     <span class="error" id="error_${keys.graphitePrefix}"></span>
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

<l:settingsGroup title="Extras">
<tr>
    <th>Whitelisted branches</th>
    <td>
        <props:textProperty name="${keys.whitelistBranches}" />
        <label for="${keys.whitelistBranches}"></label>
        <span class="smallNote">(Comma separated list) If the branch name contains any of these words, the metrics will be reported on, else ignored; eg <em>ast</em> will match <em>master</em> and <em>rel</em> will match <em>release-29.1</em>. Leave blank for all branches.</span>
    </td>
</tr>
<tr>
  <th>FxCop Metrics</th>
  <td>
    <props:textProperty name="${keys.fxCopMetricsXml}" />
    <label for="${keys.fxCopMetricsXml}"></label>
    <span class="smallNote">Artifacts zip path to FxCop Metrics XML File, eg: TestResults.zip#FxCop/Metrics.xml (path-to-zip#path-within-zip)</span>
  </td>
</tr>
<tr>
  <th>OpenCover Metrics</th>
  <td>
    <props:textProperty name="${keys.openCoverMetricsXml}" />
    <label for="${keys.openCoverMetricsXml}"></label>
    <span class="smallNote">Artifacts zip path to OpenCover Summary XML file, eg: TestResults.zip#CoverageReport/Summary.xml (path-to-zip#path-within-zip)</span>
  </td>
</tr>
</l:settingsGroup>
