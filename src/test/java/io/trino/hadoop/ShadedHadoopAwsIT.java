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
package io.trino.hadoop;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;

import static org.testng.Assert.assertTrue;

public class ShadedHadoopAwsIT
{
    @DataProvider
    public Object[][] serviceTypes()
    {
        return new Object[][] {
                {"io.trino.hadoop.internal.software.amazon.awssdk.http.SdkHttpService"},
                {"io.trino.hadoop.internal.software.amazon.awssdk.http.async.SdkAsyncHttpService"},
        };
    }

    @Test(dataProvider = "serviceTypes")
    public void testServiceProvider(String serviceName)
            throws Exception
    {
        URL jar = Path.of(System.getProperty("shadedJarPath")).toUri().toURL();
        // Use only the packaged JAR so unshaded Maven test dependencies cannot hide packaging defects.
        try (URLClassLoader loader = new URLClassLoader(new URL[] {jar}, ClassLoader.getPlatformClassLoader())) {
            Class<?> service = loader.loadClass(serviceName);
            Iterator<?> providers = ServiceLoader.load(service, loader).iterator();
            assertTrue(providers.hasNext(), "No provider for " + serviceName);
            assertTrue(service.isInstance(providers.next()));
        }
    }
}
