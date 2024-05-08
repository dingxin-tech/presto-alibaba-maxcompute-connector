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
package com.facebook.presto.maxcompute.utils;

import com.aliyun.odps.Column;
import com.aliyun.odps.table.arrow.accessor.ArrowVectorAccessor;
import com.aliyun.odps.type.TypeInfo;
import com.facebook.presto.common.PageBuilder;
import com.facebook.presto.common.block.BlockBuilder;
import com.facebook.presto.common.type.ArrayType;
import com.facebook.presto.common.type.Type;
import com.facebook.presto.maxcompute.MaxComputeColumnHandle;
import com.facebook.presto.maxcompute.MaxComputeErrorCode;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.PrestoException;
import io.airlift.slice.Slice;
import org.apache.arrow.memory.ArrowBuf;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.complex.ListVector;
import org.apache.arrow.vector.util.TransferPair;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.arrow.vector.complex.BaseRepeatedValueVector.OFFSET_WIDTH;

/**
 * @author dingxin (zhangdingxin.zdx@alibaba-inc.com)
 */
public class ArrowToPageConverter
{
    private final BufferAllocator allocator;
    private final Map<String, TypeInfo> odpsTypeMap;
    private final List<ColumnHandle> requireColumns;

    public ArrowToPageConverter(BufferAllocator allocator, List<ColumnHandle> requireColumns, List<Column> schema)
    {
        this.allocator = requireNonNull(allocator, "allocator is null");
        this.requireColumns = requireNonNull(requireColumns, "requireColumns is null");

        odpsTypeMap = requireNonNull(schema, "schema is null").stream().collect(Collectors.toMap(Column::getName, Column::getTypeInfo));
    }

    public void convert(PageBuilder pageBuilder, VectorSchemaRoot vectorSchemaRoot)
    {
        pageBuilder.declarePositions(vectorSchemaRoot.getRowCount());
        for (int column = 0; column < requireColumns.size(); column++) {
            String filedName = ((MaxComputeColumnHandle) requireColumns.get(column)).getName();
            FieldVector vector = vectorSchemaRoot.getVector(filedName);

            ArrowVectorAccessor dataAccessor = ArrowUtils.createColumnVectorAccessor(vector, odpsTypeMap.get(filedName));
            BlockBuilder blockBuilder = pageBuilder.getBlockBuilder(column);
            Type prestoType = ((MaxComputeColumnHandle) requireColumns.get(column)).getType();
            TypeInfo odpsType = odpsTypeMap.get(filedName);

            transferData(dataAccessor, blockBuilder, prestoType, odpsType, vector.getValueCount());
        }
    }

    private void transferData(ArrowVectorAccessor dataAccessor, BlockBuilder blockBuilder, Type prestoType, TypeInfo odpsType, int valueCount)
    {
        Class<?> javaType = prestoType.getJavaType();
        for (int index = 0; index < valueCount; index++) {
            Object data = ArrowUtils.getData(dataAccessor, odpsType, index);
            if (data == null) {
                blockBuilder.appendNull();
                return;
            }
            if (javaType == boolean.class) {
                prestoType.writeBoolean(blockBuilder, (Boolean) data);
            }
            else if (javaType == long.class) {
                prestoType.writeLong(blockBuilder, (Long) data);
            }
            else if (javaType == double.class) {
                prestoType.writeDouble(blockBuilder, (Double) data);
            }
            else if (javaType == Slice.class) {
                prestoType.writeSlice(blockBuilder, (Slice) data);
            }
            else {
                // TODO: support complex type
                throw new PrestoException(MaxComputeErrorCode.MAXCOMPUTE_CONNECTOR_ERROR, "Unsupported type: " + javaType);
            }
        }
    }

    private void writeArrayBlock(BlockBuilder output, ArrayType arrayType, FieldVector vector, int index)
    {
        Type elementType = arrayType.getElementType();
        ArrowBuf offsetBuffer = vector.getOffsetBuffer();
        int start = offsetBuffer.getInt((long) index * OFFSET_WIDTH);
        int end = offsetBuffer.getInt((long) (index + 1) * OFFSET_WIDTH);
        FieldVector innerVector = ((ListVector) vector).getDataVector();
        TransferPair transferPair = innerVector.getTransferPair(allocator);
        transferPair.splitAndTransfer(start, end - start);

        try (FieldVector sliced = (FieldVector) transferPair.getTo()) {
            int valueCount = sliced.getValueCount();
            // Iterate over the elements in the sliced vector and add each one to the output block builder
            for (int i = 0; i < valueCount; i++) {
                if (sliced.isNull(i)) {
                    output.appendNull();
                }
                else {
                    // Convert the type and write the element to the output block builder
                }
            }
        }
    }
}
