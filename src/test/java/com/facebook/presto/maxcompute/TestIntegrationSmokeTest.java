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

import com.facebook.presto.common.type.Type;
import com.facebook.presto.common.type.VarcharType;
import com.facebook.presto.testing.MaterializedResult;
import com.facebook.presto.testing.QueryRunner;
import com.facebook.presto.tests.AbstractTestIntegrationSmokeTest;

import java.util.Optional;

public class TestIntegrationSmokeTest
        extends AbstractTestIntegrationSmokeTest
{
    @Override
    protected QueryRunner createQueryRunner()
            throws Exception
    {
        return MaxComputeQueryRunner.createMaxComputeEmulatorQueryRunner(Optional.of("8082"));
    }


    // Sqlite used by the mc emulator uses integer and real when creating tables.
    @Override
    protected MaterializedResult getExpectedOrdersTableDescription(boolean dateSupported, boolean parametrizedVarchar)
    {
        return MaterializedResult.resultBuilder(this.getQueryRunner().getDefaultSession(),
                                new Type[] {VarcharType.VARCHAR, VarcharType.VARCHAR, VarcharType.VARCHAR, VarcharType.VARCHAR})
                        .row(new Object[] {"orderkey", "integer", "", ""})
                        .row(new Object[] {"custkey", "integer", "", ""})
                        .row(new Object[] {"orderstatus", "varchar", "", ""})
                        .row(new Object[] {"totalprice", "real", "", ""})
                        .row(new Object[] {"orderdate", "varchar", "", ""})
                        .row(new Object[] {"orderpriority", "varchar", "", ""})
                        .row(new Object[] {"clerk", "varchar", "", ""})
                        .row(new Object[] {"shippriority", "integer", "", ""})
                        .row(new Object[] {"comment", "varchar", "", ""}).build();
    }
}
