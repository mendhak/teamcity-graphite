
package mendhak.teamcity.graphite;


import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.util.EventDispatcher;
import mendhak.teamcity.graphite.ui.GraphiteBuildFeature;
import mendhak.teamcity.graphite.ui.GraphiteServerKeyNames;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.rmi.CORBA.Util;
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

    private boolean isValidBranch(SBuild build)
    {
        if(build.getBranch() == null)
        {
            return true;
        }

        String whitelistedBranchesCsv = build.getParametersProvider().get(keyNames.getWhitelistBranches());

        if(StringUtil.isEmptyOrSpaces(whitelistedBranchesCsv))
        {
            return true;
        }

        String[] whiteListedBranches = whitelistedBranchesCsv.split(",");

        for(String whitelist : whiteListedBranches)
        {
            if(build.getBranch().getDisplayName().contains(whitelist))
            {
                return true;
            }
        }

        return false;
    }

    // Look through the buildType's build features and return a boolean describing whether Graphite feature is enabled.
    @NotNull
    private boolean IsFeatureEnabled (@NotNull SBuild build)
    {
        for (SBuildFeatureDescriptor feature : build.getBuildType().getBuildFeatures()) {
            String featureType  = feature.getBuildFeature().getType();
            if (! featureType.equals(GraphiteBuildFeature.FEATURE_TYPE))
                continue;

            boolean isEnabled = build.getBuildType().isEnabled(feature.getId());
            if (!isEnabled)
                break;

            Loggers.SERVER.debug("[Graphite] Graphite build feature is enabled.");

        }

        Loggers.SERVER.debug("[Graphite] Graphite build feature is not enabled.");
        return false;
    }

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

                if(!IsFeatureEnabled(build)) { return; }
                if(!isValidBranch(build))    { return; }

                boolean sendStarted = Boolean.valueOf(build.getParametersProvider().get(keyNames.getSendBuildStarted()));

                if(sendStarted){
                    GraphiteMetric metric = new GraphiteMetric( "started", "1", System.currentTimeMillis()/1000, true);
                    Logger.LogInfo("started");
                    h.scheduleBuildMetric(build, metric);
                }
            }

            @Override
            public void buildFinished(SRunningBuild build)
            {
                if(!IsFeatureEnabled(build)) { return; }
                if(!isValidBranch(build))    { return; }

                boolean sendFinished = Boolean.valueOf(build.getParametersProvider().get(keyNames.getSendBuildFinished()));

                if(sendFinished){
                    GraphiteMetric metric = new GraphiteMetric( "finished", String.valueOf(build.getDuration()), System.currentTimeMillis()/1000, true);
                    Logger.LogInfo("finished");
                    h.scheduleBuildMetric(build, metric);
                }


                String fxCopXmlPath = build.getParametersProvider().get(keyNames.getFxCopMetricsXml());

                //FxCop Metrics
                if(!StringUtil.isEmptyOrSpaces(fxCopXmlPath) && fxCopXmlPath.contains("#"))
                {
                    Logger.LogInfo("FXCop: " + fxCopXmlPath);
                    String fxCopZip = fxCopXmlPath.split("#")[0];
                    String fxCopXml = fxCopXmlPath.split("#")[1];

                    try
                    {
                        ZipFile zip =  new ZipFile(new File(build.getArtifactsDirectory(),
                                build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL_WITH_ARCHIVES_CONTENT).findArtifact(fxCopZip).getArtifact().getRelativePath()), StandardCharsets.UTF_8);
                        ZipEntry entry =  zip.getEntry(fxCopXml);
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
                                Logger.LogInfo("fxcop." + moduleName + "." + metricName + ":" + metricValue);
                                h.scheduleBuildMetric(build, new GraphiteMetric("fxcop." + moduleName + "." + metricName, metricValue, System.currentTimeMillis()/1000, false));
                            }
                        }
                    } catch (Exception e) {
                        Logger.LogError("Could not process FxCop file", e);
                        e.printStackTrace();
                    }
                }

                String openCoverPath = build.getParametersProvider().get(keyNames.getOpenCoverMetricsXml());

                //FxCop Metrics
                if(!StringUtil.isEmptyOrSpaces(openCoverPath) && openCoverPath.contains("#"))
                {
                    String openCoverZip = openCoverPath.split("#")[0];
                    String openCoverXml = openCoverPath.split("#")[1];

                    //OpenCover
                    try
                    {
                        ZipFile zip =  new ZipFile(new File(build.getArtifactsDirectory(),
                                build.getArtifacts(BuildArtifactsViewMode.VIEW_ALL_WITH_ARCHIVES_CONTENT).findArtifact(openCoverZip).getArtifact().getRelativePath()), StandardCharsets.UTF_8);
                        ZipEntry entry =  zip.getEntry(openCoverXml);
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
                                Logger.LogInfo("opencover.assemblies:" + val);
                                h.scheduleBuildMetric(build, new GraphiteMetric("opencover.assemblies",val,System.currentTimeMillis()/1000, false));
                            }

                            if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Classes"))
                            {
                                String val = summaryNode.getChildNodes().item(i).getTextContent();
                                Logger.LogInfo("opencover.classes:" + val);
                                h.scheduleBuildMetric(build, new GraphiteMetric("opencover.classes",val,System.currentTimeMillis()/1000, false));
                            }

                            if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Files"))
                            {
                                String val = summaryNode.getChildNodes().item(i).getTextContent();
                                Logger.LogInfo("opencover.files:" + val);
                                h.scheduleBuildMetric(build, new GraphiteMetric("opencover.files",val,System.currentTimeMillis()/1000, false));
                            }

                            if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Coveredlines"))
                            {
                                String val = summaryNode.getChildNodes().item(i).getTextContent();
                                Logger.LogInfo("opencover.coveredlines:" + val);
                                h.scheduleBuildMetric(build, new GraphiteMetric("opencover.coveredlines",val,System.currentTimeMillis()/1000, false));
                            }

                            if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Uncoveredlines"))
                            {
                                String val = summaryNode.getChildNodes().item(i).getTextContent();
                                Logger.LogInfo("opencover.uncoveredlines:" + val);
                                h.scheduleBuildMetric(build, new GraphiteMetric("opencover.uncoveredlines",val,System.currentTimeMillis()/1000, false));
                            }

                            if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Coverablelines"))
                            {
                                String val = summaryNode.getChildNodes().item(i).getTextContent();
                                Logger.LogInfo("opencover.coverablelines:" + val);
                                h.scheduleBuildMetric(build, new GraphiteMetric("opencover.coverablelines",val,System.currentTimeMillis()/1000, false));
                            }


                            if(summaryNode.getChildNodes().item(i).getNodeName().equalsIgnoreCase("Totallines"))
                            {
                                String val = summaryNode.getChildNodes().item(i).getTextContent();
                                Logger.LogInfo("opencover.totallines:" + val);
                                h.scheduleBuildMetric(build, new GraphiteMetric("opencover.totallines",val,System.currentTimeMillis()/1000, false));
                            }
                        }
                    } catch (Exception e) {
                        Logger.LogError("Could not process OpenCover file", e);
                        e.printStackTrace();
                    }
                }

            }


            @Override
            public void buildInterrupted(SRunningBuild build)
            {
                if(!IsFeatureEnabled(build)) { return; }
                if(!isValidBranch(build))    { return; }

            }

            @Override
            public void statisticValuePublished(@NotNull SBuild build, @NotNull String valueTypeKey, @NotNull BigDecimal value) {
                super.statisticValuePublished(build, valueTypeKey, value);

                if(!IsFeatureEnabled(build)) { return; }
                if(!isValidBranch(build))    { return; }

                /*
                    relevant keys known at the time of writing:
                    - BuildArtifactsPublishingTime
                    - BuildCheckoutTime
                    - BuildDuration
                    - BuildDurationNetTime
                    - buildStageDuration:artifactsPublishing
                    - buildStageDuration:buildFinishing
                    - buildStageDuration:buildStepRUNNER_*
                    - buildStageDuration:firstStepPreparation
                    - buildStageDuration:sourcesUpdate
                    - TimeSpentInQueue
                 */
                boolean isTimer = valueTypeKey.matches(".*(Time|Duration).*");

                /*
                    As documented in https://github.com/mendhak/teamcity-graphite/issues/10 we will have to make sure that
                    we do not send any timer stats for buildStageDuration:buildStepRUNNER_* steps if the value if the metrics
                    is noted to 0; rationale for this has been discussed in details in the issue avobe as well as in the
                    TeamCity ticket https://youtrack.jetbrains.com/issue/TW-47322.

                    Note:  BigDecimal.equals() takes the scale into consideration; we will use comapreTo() instead to nullify scaling
                           new BigDecimal("0.00").equals(BigDecimal.ZERO);            // false
                           new BigDecimal("0.00").compareTo(BigDecimal.ZERO) == 0  ;  // true
                */
                if (isTimer && valueTypeKey.matches(".*buildStepRUNNER_.*") && value.compareTo(BigDecimal.ZERO) == 0 ){
                    Logger.LogInfo("Ignoring zero values for " + valueTypeKey + " metric.");
                    return;
                }

                GraphiteMetric metric = new GraphiteMetric( valueTypeKey.replace(":","."), String.valueOf(value), System.currentTimeMillis()/1000, isTimer);

                h.scheduleBuildMetric(build, metric);
                Logger.LogInfo(valueTypeKey + " : " + String.valueOf(value));
            }
        });
    }


}
