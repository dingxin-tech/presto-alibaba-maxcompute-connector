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

import com.facebook.airlift.log.Logger;
import com.facebook.presto.maxcompute.MaxComputeErrorCode;
import com.facebook.presto.spi.PrestoException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.concurrent.Callable;

/**
 * @author dingxin (zhangdingxin.zdx@alibaba-inc.com)
 */
public class CommonUtils
{
    private static final Logger LOG = Logger.get(CommonUtils.class);

    private CommonUtils() {}

    public static String serialize(Serializable object)
    {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            byte[] serializedBytes = byteArrayOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(serializedBytes);
        }
        catch (Exception e) {
            throw new PrestoException(MaxComputeErrorCode.MAXCOMPUTE_CONNECTOR_ERROR, "serialize object error", e);
        }
    }

    public static Object deserialize(String serializedString)
    {
        try {
            byte[] serializedBytes = Base64.getDecoder().decode(serializedString);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedBytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        }
        catch (Exception e) {
            throw new PrestoException(MaxComputeErrorCode.MAXCOMPUTE_CONNECTOR_ERROR, "deserialize object error", e);
        }
    }

    public static <T> T executeWithRetry(Callable<T> callable, int maxRetries, long retryDelay)
            throws IOException
    {
        int attempt = 0;
        while (true) {
            try {
                return callable.call();
            }
            catch (Exception e) {
                attempt++;
                if (attempt > maxRetries) {
                    throw new IOException("Failed after retries", e);
                }
                try {
                    LOG.warn("Retrying after exception: " + e.getMessage(), e);
                    Thread.sleep(retryDelay);
                }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", ie);
                }
            }
        }
    }
}
