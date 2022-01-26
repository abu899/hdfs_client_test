import HdfsClient.HdfsClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class HdfsClientConnectorTest {

    private final String url = "hdfs://10.253.1.104:9000"; // hadoop이 설치된 ip 주소 (default: localhost)
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

        assertThat(hdfs.exists(outputPath)).isFalse();
    }

    @Test
    void writeAndReadTest() {
        Configuration configure = new Configuration();

        final String path = "/test_file/hello.txt";
        Path writePathInHDFS = new Path(path);
        FileSystem hdfs = null;
        try {
            hdfs = FileSystem.get(URI.create(url), configure);

            //write
            final String testString = "test hello\n";
//            FSDataOutputStream outStream = hdfs.create(writePath);
//            outStream.writeUTF(testString);
//            outStream.close();

            //read
            FSDataInputStream inputStream = hdfs.open(writePathInHDFS);
            String readString = inputStream.readUTF();
            inputStream.close();
            assertThat(readString).isEqualTo(testString);
            System.out.println("readString = " + readString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            hdfs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
