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

import com.facebook.presto.spi.ConnectorTableLayoutHandle;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import static java.util.Objects.requireNonNull;

/**
 * @author dingxin (zhangdingxin.zdx@alibaba-inc.com)
 */
public class MaxComputeTableLayoutHandle
        implements ConnectorTableLayoutHandle
{
    private final MaxComputeTableHandle tableHandle;

    @JsonCreator
    public MaxComputeTableLayoutHandle(@JsonProperty("tableHandle") MaxComputeTableHandle tableHandle)
    {
        this.tableHandle = requireNonNull(tableHandle, "tableHandle is null");
    }

    @JsonProperty
    public MaxComputeTableHandle getTableHandle()
    {
        return tableHandle;
    }
}