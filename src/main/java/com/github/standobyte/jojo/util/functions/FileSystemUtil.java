package com.github.standobyte.jojo.util.functions;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

public class FileSystemUtil {
	
	public static BufferedWriter newWriterMkDir(File file, Charset charset) throws IOException {
		checkNotNull(file);
		checkNotNull(charset);
		return new BufferedWriter(new OutputStreamWriter(FileUtils.openOutputStream(file), charset));
	}
}
