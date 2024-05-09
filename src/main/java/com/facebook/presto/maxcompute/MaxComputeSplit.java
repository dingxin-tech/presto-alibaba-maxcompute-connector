/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.maxcompute;

import com.aliyun.odps.table.read.TableBatchReadSession;
import com.facebook.presto.maxcompute.utils.CommonUtils;
import com.facebook.presto.spi.ConnectorSplit;
import com.facebook.presto.spi.HostAddress;
import com.facebook.presto.spi.NodeProvider;
import com.facebook.presto.spi.SplitWeight;
import com.facebook.presto.spi.schedule.NodeSelectionStrategy;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

import static java.util.Objects.requireNonNull;

public class MaxComputeSplit
        implements ConnectorSplit
{
    private final MaxComputeInputSplit split;
    private final String session;
    private Map<String, String> properties;

    @JsonCreator
    public MaxComputeSplit(
            @JsonProperty("split") MaxComputeInputSplit split,
            @JsonProperty("session") String session,
            @JsonProperty("properties") Map<String, String> properties)
    {
        this.split = requireNonNull(split, "split is null");
        this.session = requireNonNull(session, "session is null");
        this.properties = requireNonNull(properties, "properties is null");
    }

    public MaxComputeSplit(MaxComputeInputSplit split, TableBatchReadSession session, Map<String, String> properties)
    {
        this.split = requireNonNull(split, "split is null");
        this.session = getSerializeSession(requireNonNull(session, "session is null"));
        this.properties = requireNonNull(properties, "properties is null");
    }

    @JsonProperty
    public MaxComputeInputSplit getSplit()
    {
        return split;
    }

    @JsonProperty
    public String getSession()
    {
        return session;
    }

    @JsonProperty
    public Map<String, String> getProperties()
    {
        return properties;
    }

    @Override
    public NodeSelectionStrategy getNodeSelectionStrategy()
    {
        return NodeSelectionStrategy.NO_PREFERENCE;
    }

    @Override
    public List<HostAddress> getPreferredNodes(NodeProvider nodeProvider)
    {
        return Collections.emptyList();
    }

    @Override
    public Object getInfo()
    {
        return this;
    }

    @Override
    public Map<String, String> getInfoMap()
    {
        return properties;
    }

    @Override
    public Object getSplitIdentifier()
    {
        return ConnectorSplit.super.getSplitIdentifier();
    }

    @Override
    public OptionalLong getSplitSizeInBytes()
    {
        return ConnectorSplit.super.getSplitSizeInBytes();
    }

    @Override
    public SplitWeight getSplitWeight()
    {
        return ConnectorSplit.super.getSplitWeight();
    }

    public TableBatchReadSession getReadSession()
    {
        return (TableBatchReadSession) CommonUtils.deserialize(session);
    }

    public void saveOdpsConf(MaxComputeConfig config)
    {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        if (properties != null) {
            builder.putAll(properties);
        }
        builder.put("odpsConfig", CommonUtils.serialize(config));
        properties = builder.build();
    }

    public MaxComputeConfig getOdpsConf()
    {
        String odpsConfig = properties.get("odpsConfig");
        return (MaxComputeConfig) CommonUtils.deserialize(odpsConfig);
    }

    private String getSerializeSession(TableBatchReadSession session)
    {
        return CommonUtils.serialize(session);
    }
}
