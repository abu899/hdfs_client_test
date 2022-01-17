import HdfsClient.HdfsClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HdfsClientConnectorTest {

    private String url = "hdfs://10.253.1.104:50070";
    Path outputPath = new Path("/tmp/test/app/");

    @Test
    void hdfsConnectionTest() {
        HdfsClient client  = new HdfsClient();
        assertThat(client.connect()).isSameAs(true);
    }

    @Test
    void getFileSystemTest() throws URISyntaxException, IOException {
        Configuration configure = new Configuration();
        assertThat(configure).isNotNull();

        assertDoesNotThrow(()-> FileSystem.get(URI.create(url), configure));

        FileSystem hdfs = FileSystem.get(URI.create(url), configure);

        assertThat(hdfs.exists(outputPath)).isTrue();
    }

    @Test
    void writeAndReadTest() throws IOException {
        System.setProperty("HADOOP_USER_NAME", "root");

        Configuration configure = new Configuration();
        configure.set("fs.defaultFS", "hdfs://namenode:9000/");
        configure.set("dfs.blocksize", "134217728");

        FileSystem hdfs = FileSystem.get(URI.create(url), configure);

        Path writePath = new Path("/user/input/hello.txt");
        FSDataInputStream inputStream = hdfs.open(writePath);
        assertThat(inputStream).isNotNull();
        String inputString = inputStream.readUTF();
        inputStream.close();
    }
}
