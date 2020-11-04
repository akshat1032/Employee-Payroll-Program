package test.com.capgemini.niofileapitest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

public class NIOFileAPITest {
	
	private static String HOME = System.getProperty("user.home");
	private static String  PLAY_WITH_NIO = "TempPlayGround";
	
	@Test
	public void testPathAndConfirm() throws IOException {
		
		// Check if file exists
		Path homePath = Paths.get(HOME);
		Assert.assertTrue(Files.exists(homePath));
		
		// Delete file and check if file exists or not
		Path playPath = Paths.get(HOME+"/"+PLAY_WITH_NIO);
		if(Files.exists(playPath)) FileUtils.deleteFiles(playPath.toFile());
		Assert.assertTrue(Files.notExists(playPath));
		
		//Create directory
		Files.createDirectory(playPath);
		Assert.assertTrue(Files.exists(playPath));
		
		// Create File
		IntStream.range(1, 10).forEach(center -> {
			Path tempFile = Paths.get(playPath+"/temp"+center);
			Assert.assertTrue(Files.notExists(tempFile));
			try {Files.createFile(tempFile);}
			catch(IOException e) {}
			Assert.assertTrue(Files.exists(tempFile));
		});
		
		//List Files, Directories and Files with extensions
		Files.list(playPath).filter(Files::isRegularFile).forEach(System.out::println);
		Files.newDirectoryStream(playPath).forEach(System.out::println);
		Files.newDirectoryStream(playPath, path -> path.toFile().isFile() &&
				path.toString().startsWith("temp"))
		.forEach(System.out::println);
	}
}
