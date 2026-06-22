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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.s3a.S3AFileSystem;
import org.testng.annotations.Test;

import java.net.URI;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;

public class TestHadoopAws
{
    @Test
    public void testS3AFileSystemCanBeResolved()
            throws Exception
    {
        Configuration configuration = new Configuration(false);
        configuration.set("fs.s3a.impl", S3AFileSystem.class.getName());
        configuration.set("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider");
        configuration.set("fs.s3a.endpoint", "http://127.0.0.1:9");
        configuration.set("fs.s3a.path.style.access", "true");
        configuration.set("fs.s3a.connection.ssl.enabled", "false");
        configuration.set("fs.s3a.bucket.probe", "0");

        Class<? extends FileSystem> fileSystemClass = FileSystem.getFileSystemClass("s3a", configuration);
        assertSame(fileSystemClass, S3AFileSystem.class);
        try (FileSystem fileSystem = fileSystemClass.getConstructor().newInstance()) {
            fileSystem.initialize(URI.create("s3a://trino-smoke-test/"), configuration);
        }

        assertNotNull(Class.forName("software.amazon.awssdk.services.s3.S3Client"));
        assertNotNull(Class.forName("software.amazon.awssdk.services.sts.StsClient"));
        assertNotNull(Class.forName("software.amazon.awssdk.services.kms.KmsClient"));
        assertNotNull(Class.forName("software.amazon.awssdk.transfer.s3.S3TransferManager"));
        assertNotNull(Class.forName("software.amazon.s3.analyticsaccelerator.S3SeekableInputStreamFactory"));
    }
}
