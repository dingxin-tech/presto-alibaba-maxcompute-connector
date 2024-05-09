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

import com.aliyun.odps.Odps;
import com.aliyun.odps.OdpsException;
import com.aliyun.odps.PartitionSpec;
import com.aliyun.odps.Table;
import com.aliyun.odps.table.TableIdentifier;
import com.aliyun.odps.table.enviroment.EnvironmentSettings;
import com.aliyun.odps.table.read.TableBatchReadSession;
import com.aliyun.odps.table.read.TableReadSessionBuilder;
import com.facebook.presto.common.predicate.TupleDomain;
import com.facebook.presto.maxcompute.utils.MaxComputeUtils;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorSplitSource;
import com.facebook.presto.spi.ConnectorTableLayoutHandle;
import com.facebook.presto.spi.FixedSplitSource;
import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.connector.ConnectorSplitManager;
import com.facebook.presto.spi.connector.ConnectorTransactionHandle;

import javax.inject.Inject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MaxComputeSplitManager
        implements ConnectorSplitManager
{
    private final String connectorId;
    private final Odps odps;
    private final EnvironmentSettings settings;

    @Inject
    public MaxComputeSplitManager(MaxComputeConnectorId connectorId, MaxComputeConfig config)
    {
        this.connectorId = requireNonNull(connectorId, "connectorId is null").toString();
        this.odps = MaxComputeUtils.getOdps(requireNonNull(config, "config is null"));
        this.settings = MaxComputeUtils.getEnvironmentSettings(requireNonNull(config, "config is null"));
    }

    @Override
    public ConnectorSplitSource getSplits(
            ConnectorTransactionHandle handle,
            ConnectorSession session,
            ConnectorTableLayoutHandle layout,
            SplitSchedulingContext splitSchedulingContext)
    {
        try {
            MaxComputeTableHandle tableHandle = ((MaxComputeTableLayoutHandle) layout).getTableHandle();
            Table table = odps.tables().get(tableHandle.getProjectId(), tableHandle.getSchemaName(), tableHandle.getTableName());

            TableReadSessionBuilder tableReadSessionBuilder =
                    new TableReadSessionBuilder().identifier(TableIdentifier.of(table.getProject(), table.getSchemaName(), table.getName()))
                            .withSettings(settings);

            if (tableHandle.getProjectedColumns().isPresent()) {
                tableReadSessionBuilder.requiredDataColumns(tableHandle.getProjectedColumns().get().stream().map(e -> ((MaxComputeColumnHandle) e).getName()).collect(Collectors.toList()));
            }
            if (table.isPartitioned()) {
                tableReadSessionBuilder.requiredPartitions(extractPartition(table, tableHandle.getConstraint()));
            }

            TableBatchReadSession readSession = tableReadSessionBuilder.buildBatchReadSession();
            List<MaxComputeSplit> splits = Arrays.stream(readSession.getInputSplitAssigner().getAllSplits()).map(e -> {
                MaxComputeInputSplit maxComputeInputSplit = new MaxComputeInputSplit(e);
                return new MaxComputeSplit(maxComputeInputSplit, readSession, Collections.emptyMap());
            }).collect(Collectors.toList());
            return new FixedSplitSource(splits);
        }
        catch (OdpsException e) {
            throw MaxComputeUtils.wrapOdpsException(e);
        }
        catch (IOException e) {
            throw new PrestoException(MaxComputeErrorCode.MAXCOMPUTE_CONNECTOR_ERROR, e);
        }
    }

    private List<PartitionSpec> extractPartition(Table table, TupleDomain<ColumnHandle> constraint)
    {
        // TODO: support partitioned table
        return null;
    }
}
