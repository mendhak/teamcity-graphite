/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mendhak.teamcity.graphite;

import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.parameters.AbstractBuildParametersProvider;
import mendhak.teamcity.graphite.ui.GraphiteBuildFeature;
import mendhak.teamcity.graphite.ui.GraphiteServerKeyNames;
import org.jetbrains.annotations.NotNull;

import java.util.Map;


public class GraphiteBuildParametersProvider extends AbstractBuildParametersProvider {
    @Override
    @NotNull
    public Map<String, String> getParameters(@NotNull SBuild build, boolean emulationMode) {
        Map<String, String> parameters = super.getParameters(build,emulationMode);

        final GraphiteServerKeyNames keyNames = new GraphiteServerKeyNames();

        SBuildType buildType = build.getBuildType();
        if (buildType != null){
            for (SBuildFeatureDescriptor feature : buildType.getBuildFeatures()){
                if (feature.getBuildFeature().getType().equals(GraphiteBuildFeature.FEATURE_TYPE)){

                    ValueResolver resolver = build.getValueResolver();

                    parameters.put(keyNames.getServerKey(), resolver.resolve(feature.getParameters().get(keyNames.getServerKey())).getResult());
                    parameters.put(keyNames.getServerPort(), resolver.resolve(feature.getParameters().get(keyNames.getServerPort())).getResult());
                    parameters.put(keyNames.getGraphitePrefix(), resolver.resolve(feature.getParameters().get(keyNames.getGraphitePrefix())).getResult());

                    String graphiteSuffix = "";
                    if(feature.getParameters().get(keyNames.getGraphiteSuffix()) != null){
                        graphiteSuffix = resolver.resolve(feature.getParameters().get(keyNames.getGraphiteSuffix())).getResult();
                    }
                    parameters.put(keyNames.getGraphiteSuffix(), graphiteSuffix);

                    String sendBuildStarted = "false";
                    if(feature.getParameters().get(keyNames.getSendBuildStarted()) != null)
                    {
                        sendBuildStarted = resolver.resolve(feature.getParameters().get(keyNames.getSendBuildStarted())).getResult();
                    }
                    parameters.put(keyNames.getSendBuildStarted(), sendBuildStarted);

                    String sendBuildFinished = "false";
                    if(feature.getParameters().get(keyNames.getSendBuildFinished()) != null)
                    {
                        sendBuildFinished = resolver.resolve(feature.getParameters().get(keyNames.getSendBuildFinished())).getResult();
                    }
                    parameters.put(keyNames.getSendBuildFinished(), sendBuildFinished);

                    String getUseUdp = "false";
                    if(feature.getParameters().get(keyNames.getUseUdp()) != null){
                        getUseUdp = resolver.resolve(feature.getParameters().get(keyNames.getUseUdp())).getResult();
                    }
                    parameters.put(keyNames.getUseUdp(), getUseUdp);

                    String getSendTimers = "false";
                    if(feature.getParameters().get(keyNames.getSendTimers()) != null){
                        getSendTimers = resolver.resolve(feature.getParameters().get(keyNames.getSendTimers())).getResult();
                    }
                    parameters.put(keyNames.getSendTimers(), getSendTimers);

                    String fxCopMetricsXml = "";
                    if(feature.getParameters().get(keyNames.getFxCopMetricsXml()) != null){
                        fxCopMetricsXml = resolver.resolve(feature.getParameters().get(keyNames.getFxCopMetricsXml())).getResult();
                    }
                    parameters.put(keyNames.getFxCopMetricsXml(), fxCopMetricsXml);

                    String openCoverXml = "";
                    if(feature.getParameters().get(keyNames.getOpenCoverMetricsXml()) != null){
                        openCoverXml = resolver.resolve(feature.getParameters().get(keyNames.getOpenCoverMetricsXml())).getResult();
                    }
                    parameters.put(keyNames.getOpenCoverMetricsXml(), openCoverXml);

                    String whiteListBranches = "";
                    if(feature.getParameters().get(keyNames.getWhitelistBranches()) != null){
                        whiteListBranches = resolver.resolve(feature.getParameters().get(keyNames.getWhitelistBranches())).getResult();
                    }
                    parameters.put(keyNames.getWhitelistBranches(), whiteListBranches);
                }
            }
        }
        return parameters;
    }
}
