package pisco.common.parsers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import choco.kernel.common.logging.ChocoLogging;
import parser.instances.InstanceFileParser;

public abstract class AbstractTextParser implements InstanceFileParser {

	public static final Logger LOGGER = ChocoLogging.getMainLogger();
	private static final Pattern COMMENT = Pattern.compile("(#|//).*");

	private File input;
	private Scanner sc;

	public AbstractTextParser() {
		super();
	}


	@Override
	public void cleanup() {
		input = null;
		sc = null;
	}
	
	@Override
	public final File getInstanceFile() {
		return input;
	}

	public final Scanner getScanner() {
		return sc;
	}
	@Override
	public final void loadInstance(File file) {
		try {
			input = file;
			sc = new Scanner(input);
		}
		catch(FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "parser...[loading][FAIL]", e);
			ChocoLogging.flushLogs();
			System.exit(2);
		}
	}
	
	protected final void close() {
		skipComments();
		if(sc.hasNext()) {
			sc.close();
			throw new IllegalArgumentException("parser...[close:can-read-more][FAIL]");
		}
		sc.close();
	}

	protected final void skipComments() {
		while (sc.hasNext(COMMENT)) {
			sc.nextLine();
		}
	}

	protected final int nextInt() {
		skipComments();
		if(sc.hasNextInt()) {return sc.nextInt();}
		else throw new IllegalArgumentException("parser...[read-int][FAIL]");
	}

	protected final double nextFloat() {
		skipComments();
		if(sc.hasNextFloat()) {return sc.nextFloat();}
		else throw new IllegalArgumentException("parser...[read-float][FAIL]");
	}
	
	protected final double nextDouble() {
		skipComments();
		if(sc.hasNextDouble()) {return sc.nextDouble();}
		else throw new IllegalArgumentException("parser...[read-double][FAIL]");
	}

}