import HdfsClient.HdfsClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.io.IOException;
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
    void writeAndReadTest() {
        Configuration configure = new Configuration();

//        System.setProperty("HADOOP_USER_NAME", "root");
//        configure.set("HADOOP_USER_NAME", "root");
//        configure.set("HADOOP_NAMENODE_USER", "root");
//        configure.set("HADOOP_DATANODE_USER", "root");

        configure.set("HADOOP_HOE", "/opt/hadoop");

        final String path = "/test_file/hello.txt";
        Path writePathInHDFS = new Path(path);
        FileSystem hdfs = null;
        try {
            hdfs = FileSystem.get(URI.create(url), configure);

            //write
            final String testString = "test hello\n";
//            FSDataOutputStream outStream = hdfs.create(writePathInHDFS);
//            outStream.writeUTF(testString);
//            outStream.close();

            //read
//            int fileSize = 0;
            FileStatus fileStatus = hdfs.getFileStatus(writePathInHDFS);
            FSDataInputStream inputStream = hdfs.open(writePathInHDFS);
//            String readString = inputStream.readUTF();
            int data = 0;
            StringBuilder readString = new StringBuilder();
            while((data = inputStream.read()) != -1){
                readString.append((char) data);
            }

            ByteBuffer byteBuffer = ByteBuffer.allocate((int) fileStatus.getLen());
            inputStream.readFully(0, byteBuffer);
//            byte[] expected = new byte[locations.length];
//            inputStream.readFully(0, expected);
//            String readString = expected.toString();
            inputStream.close();

            String byteBufferStr = StandardCharsets.UTF_8.decode(byteBuffer).toString();
            assertThat(readString.toString()).isEqualTo(testString);
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
