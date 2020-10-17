package main.com.capgemini.employeepayrollmain;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class JavaWatchService {

	private final WatchService watcher;
	private final Map<WatchKey, Path> dirWatchers;
	private Kind<?> ENTRY_CREATE;
	private Kind<?> ENTRY_DELETE;
	private Kind<?> ENTRY_MODIFY;

	// Create watch service and register the directory
	public JavaWatchService(Path dir) throws IOException {
		this.watcher = FileSystems.getDefault().newWatchService();
		this.dirWatchers = new HashMap<WatchKey, Path>();
		scanAndRegisterDirectories(dir);
	}

	// Register the given directory with the watch service
	private void registerDirWatchers(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		dirWatchers.put(key, dir);
	}

	// Register the directory and all its sub-directories with watch service
	private void scanAndRegisterDirectories(Path dir) throws IOException {
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes basicFileAttributes)
					throws IOException {
				registerDirWatchers(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	// Process all the events inside the directory
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void processEvents() throws IOException {
		while (true) {
			WatchKey key;
			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				return;
			}
			Path dir = dirWatchers.get(key);
			if (dir == null)
				continue;
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind kind = event.kind();
				Path name = ((WatchEvent<Path>) event).context();
				Path child = dir.resolve(name);
				System.out.format("%s: %s\n", event.kind().name(), child);

				if (kind == ENTRY_CREATE) {
					if (Files.isDirectory(child))
						scanAndRegisterDirectories(child);
				} else if (kind.equals(ENTRY_DELETE)) {
					if (Files.isDirectory(child))
						dirWatchers.remove(key);
				}
			}

			boolean valid = key.reset();
			if (!valid) {
				dirWatchers.remove(key);
				if (dirWatchers.isEmpty())
					break;
			}
		}
	}
}
