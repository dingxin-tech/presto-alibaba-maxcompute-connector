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

import com.facebook.airlift.configuration.Config;

import java.io.Serializable;

public class MaxComputeConfig
        implements Serializable
{
    private String project;
    private String accessId;
    private String accessKey;
    private String endpoint;
    private String tunnelEndpoint;

    public String getProject()
    {
        return project;
    }

    public String getAccessId()
    {
        return accessId;
    }

    public String getAccessKey()
    {
        return accessKey;
    }

    public String getEndPoint()
    {
        return endpoint;
    }

    public String getTunnelEndPoint()
    {
        return tunnelEndpoint;
    }

    @Config("odps.project.name")
    public MaxComputeConfig setProject(String project)
    {
        this.project = project;
        return this;
    }

    @Config("odps.access.id")
    public MaxComputeConfig setAccessId(String accessId)
    {
        this.accessId = accessId;
        return this;
    }

    @Config("odps.access.key")
    public MaxComputeConfig setAccessKey(String accessKey)
    {
        this.accessKey = accessKey;
        return this;
    }

    @Config("odps.end.point")
    public MaxComputeConfig setEndPoint(String endpoint)
    {
        this.endpoint = endpoint;
        return this;
    }

    @Config("odps.tunnel.end.point")
    public MaxComputeConfig setTunnelEndPoint(String tunnelEndpoint)
    {
        this.tunnelEndpoint = tunnelEndpoint;
        return this;
    }
}
