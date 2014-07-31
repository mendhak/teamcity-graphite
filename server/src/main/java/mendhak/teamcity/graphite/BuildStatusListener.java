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
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.util.EventDispatcher;
import mendhak.teamcity.graphite.ui.GraphiteServerKeyNames;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class BuildStatusListener
{
    @NotNull
    private final GraphiteClient updater;

    final GraphiteServerKeyNames keyNames = new GraphiteServerKeyNames();


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
                boolean sendStarted = Boolean.valueOf(build.getParametersProvider().get(keyNames.getSendBuildStarted()));

                if(sendStarted){
                    GraphiteMetric metric = new GraphiteMetric( "started", "1", System.currentTimeMillis()/1000 );
                    h.scheduleBuildMetric(build, metric);
                }

            }

            @Override
            public void buildFinished(SRunningBuild build)
            {
                boolean sendFinished = Boolean.valueOf(build.getParametersProvider().get(keyNames.getSendBuildFinished()));

                if(sendFinished){
                    GraphiteMetric metric = new GraphiteMetric( "finished", String.valueOf(build.getDuration()), System.currentTimeMillis()/1000 );
                    h.scheduleBuildMetric(build, metric);
                }


                //FxCop Metrics
                try
                {

                    ZipFile zip =  new ZipFile(new File(build.getArtifactsDirectory(),
                            build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL_WITH_ARCHIVES_CONTENT).findArtifact("TestResults.zip").getArtifact().getRelativePath()), StandardCharsets.UTF_8);
                    ZipEntry entry =  zip.getEntry("FxCop/Metrics.xml");
                    InputStream zis =  zip.getInputStream(entry);

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder parser = factory.newDocumentBuilder();
                    Document dc= parser.parse(zis);

                    XPathFactory xPathFactory = XPathFactory.newInstance();
                    XPath xpath = xPathFactory.newXPath();
                    XPathExpression expr = xpath.compile("//Module/Metrics/Metric");
                    NodeList metricNodes = (NodeList) expr.evaluate(dc, XPathConstants.NODESET);

                    for(int i=0; i<metricNodes.getLength(); i++)
                    {
                        if(metricNodes.item(i).getParentNode().getParentNode().getNodeName().equalsIgnoreCase("Module"))
                        {
                            String moduleName = metricNodes.item(i).getParentNode().getParentNode().getAttributes().getNamedItem("Name").getNodeValue();
                            String metricName = metricNodes.item(i).getAttributes().getNamedItem("Name").getNodeValue();
                            String metricValue = metricNodes.item(i).getAttributes().getNamedItem("Value").getNodeValue();
                            h.scheduleBuildMetric(build, new GraphiteMetric("fxcop." + moduleName + "." + metricName, metricValue, System.currentTimeMillis()/1000));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //OpenCover
                try
                {

                    ZipFile zip =  new ZipFile(new File(build.getArtifactsDirectory(),
                            build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL_WITH_ARCHIVES_CONTENT).findArtifact("TestResults.zip").getArtifact().getRelativePath()), StandardCharsets.UTF_8);
                    ZipEntry entry =  zip.getEntry("CoverageReport/Summary.xml");
                    InputStream zis =  zip.getInputStream(entry);

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder parser = factory.newDocumentBuilder();
                    Document dc= parser.parse(zis);

                    XPathFactory xPathFactory = XPathFactory.newInstance();
                    XPath xpath = xPathFactory.newXPath();
                    XPathExpression expr = xpath.compile("/CoverageReport/Summary");
                    Node summaryNode = (Node) expr.evaluate(dc, XPathConstants.NODE);

                    int i;
                    for(i=0;i<summaryNode.getChildNodes().getLength(); i++)
                    {
                        if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Assemblies"))
                        {
                            String val = summaryNode.getChildNodes().item(i).getTextContent();
                            h.scheduleBuildMetric(build, new GraphiteMetric("opencover.assemblies",val,System.currentTimeMillis()/1000));
                        }

                        if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Classes"))
                        {
                            String val = summaryNode.getChildNodes().item(i).getTextContent();
                            h.scheduleBuildMetric(build, new GraphiteMetric("opencover.classes",val,System.currentTimeMillis()/1000));
                        }

                        if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Files"))
                        {
                            String val = summaryNode.getChildNodes().item(i).getTextContent();
                            h.scheduleBuildMetric(build, new GraphiteMetric("opencover.files",val,System.currentTimeMillis()/1000));
                        }

                        if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Coveredlines"))
                        {
                            String val = summaryNode.getChildNodes().item(i).getTextContent();
                            h.scheduleBuildMetric(build, new GraphiteMetric("opencover.coveredlines",val,System.currentTimeMillis()/1000));
                        }

                        if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Uncoveredlines"))
                        {
                            String val = summaryNode.getChildNodes().item(i).getTextContent();
                            h.scheduleBuildMetric(build, new GraphiteMetric("opencover.uncoveredlines",val,System.currentTimeMillis()/1000));
                        }

                        if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Coverablelines"))
                        {
                            String val = summaryNode.getChildNodes().item(i).getTextContent();
                            h.scheduleBuildMetric(build, new GraphiteMetric("opencover.coverablelines",val,System.currentTimeMillis()/1000));
                        }


                        if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Totallines"))
                        {
                            String val = summaryNode.getChildNodes().item(i).getTextContent();
                            h.scheduleBuildMetric(build, new GraphiteMetric("opencover.totallines",val,System.currentTimeMillis()/1000));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }



            @Override
            public void buildInterrupted(SRunningBuild build)
            {

            }

            @Override
            public void statisticValuePublished(@NotNull SBuild build, @NotNull String valueTypeKey, @NotNull BigDecimal value) {
                super.statisticValuePublished(build, valueTypeKey, value);
                GraphiteMetric metric = new GraphiteMetric( valueTypeKey.replace(":","."), String.valueOf(value), System.currentTimeMillis()/1000);

                h.scheduleBuildMetric(build, metric);
                Logger.LogInfo(valueTypeKey + " : " + String.valueOf(value));
            }
        });
    }


}
