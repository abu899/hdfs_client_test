package HdfsClient;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class HdfsClient {

    private FileSystem hdfs;

    public boolean connect() {
        try {
            System.out.println("HdfsClient.connect");
            hdfs = getFileSystem();
            return true;
        }
//        catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
        catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("HdfsClient.connect");
            return false;
        }
    }

    public void printTestString() {
        String testFileString = "there is no string";
        try {
            System.out.println("HdfsClient.printTestString");
            testFileString = getTestFileString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("testFileString = " + testFileString);
    }

    public FileSystem getFileSystem() throws IOException {
        Configuration configure = new Configuration();
        configure.setBoolean("dfs.client.use.datanode.hostname", true);
        configure.setBoolean("dfs.datanode.use.datanode.hostname", true);
        configure.setBoolean("dfs.permissions.enabled", false);
        configure.setBoolean("dfs.client.block.write.replace-datanode-on-failure.enable", true);
        configure.setBoolean("dfs.client.block.write.replace-datanode-on-failure.best-effort", true);
        configure.set("dfs.client.block.write.replace-datanode-on-failure.policy", "DEFAULT");

        configure.set("fs.defaultFS", "hdfs://namenode:9000");

//        String url = "hdfs://localhost:9000";
        return FileSystem.get(configure);
    }

    public String getTestFileString() throws IOException {
        Path writePath = new Path("/test_file/hello.txt");
        FSDataInputStream inputStream = hdfs.open(writePath);
        String inputString = inputStream.readUTF();
        inputStream.close();

        return inputString;
    }

    public static void main(String[] args) throws InterruptedException {
//        Thread.sleep(15000);
        HdfsClient client = new HdfsClient();
        if (client.connect()) {
            client.printTestString();
        }
    }
}
