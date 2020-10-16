package test.com.capgemini.niofileapitest;

import java.io.File;

public class FileUtils {
	public static boolean deleteFiles(File fileToDelete) {
		File[] path = fileToDelete.listFiles();
		if (path !=null) {
			for (File eachFile : path) {
				deleteFiles(eachFile);
			}
		}
		return fileToDelete.delete();
	}
}
