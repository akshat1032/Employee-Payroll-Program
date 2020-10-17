package test.com.capgemini.niofileapitest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import main.com.capgemini.employeepayrollmain.JavaWatchService;

public class TestWatchService {
	private static String HOME = System.getProperty("user.home");
	private static String  PLAY_WITH_NIO = "TempPlayGround";

	@Test
	public void testWatcherService() throws IOException{
		Path dir = Paths.get(HOME+"/"+PLAY_WITH_NIO);
		Files.list(dir).filter(Files::isRegularFile).forEach(System.out::println);
		new JavaWatchService(dir).processEvents();
	}
}
