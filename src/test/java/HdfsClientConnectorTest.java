import HdfsClient.HdfsClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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
    void readSimpleTextTest() {
        Configuration configure = new Configuration();

        final String path = "/test_file/hello.txt";
        Path writePathInHDFS = new Path(path);
        FileSystem hdfs = null;
        try {
            hdfs = FileSystem.get(URI.create(url), configure);

            final String testString = "test hello\n";

            FSDataInputStream inputStream = hdfs.open(writePathInHDFS);

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String readString = br.readLine();

            inputStream.close();

            assertThat(readString).isEqualTo(testString);
            System.out.println("readString = " + readString);
        } catch (IOException e) {
            e.printStackTrace();
            fail("fail write and read");
        }

        try {
            hdfs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void readWriteWithJavaTest(){
        Configuration configure = new Configuration();

        final String path = "/test_file/hello_2.txt";
        Path writePathInHDFS = new Path(path);
        FileSystem hdfs = null;
        try {
            hdfs = FileSystem.get(URI.create(url), configure);

            //write
            final String testString = "test hello java";
            FSDataOutputStream outStream = hdfs.create(writePathInHDFS);
            outStream.writeUTF(testString);
            outStream.close();

            //read
            FSDataInputStream inputStream = hdfs.open(writePathInHDFS);
            String readString = inputStream.readUTF();

            inputStream.close();

            assertThat(readString).isEqualTo(testString);
            System.out.println("readString = " + readString);
        } catch (IOException e) {
            e.printStackTrace();
            fail("fail write and read");
        }

        try {
            hdfs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
