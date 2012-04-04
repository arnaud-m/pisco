/**
*  Copyright (c) 2011, Arnaud Malapert
*  All rights reserved.
*  Redistribution and use in source and binary forms, with or without
*  modification, are permitted provided that the following conditions are met:
*
*      * Redistributions of source code must retain the above copyright
*        notice, this list of conditions and the following disclaimer.
*      * Redistributions in binary form must reproduce the above copyright
*        notice, this list of conditions and the following disclaimer in the
*        documentation and/or other materials provided with the distribution.
*      * Neither the name of the Arnaud Malapert nor the
*        names of its contributors may be used to endorse or promote products
*        derived from this software without specific prior written permission.
*
*  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
*  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
*  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
*  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
*  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
*  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
*  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
*  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
*  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
*  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 */
package pisco.shop.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import parser.instances.InstanceFileParser;
import choco.kernel.common.logging.ChocoLogging;




/**
 * @author Arnaud Malapert
 *
 */
public abstract class AbstractTextParser implements InstanceFileParser {

	public final static Logger LOGGER= ChocoLogging.getMainLogger();
		
	protected BufferedReader reader;

	protected StreamTokenizer tokenizer;

	public File input;

	
	@Override
	public void cleanup() {
		input = null;
		reader = null;
		tokenizer = null;
	}

	protected final void close() {
		try {
			reader.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "txtparser...[close][FAIL]", e);
		}
	}
	
	

//	@Override
//	public void parse(boolean displayInstance)
//			throws UnsupportedConstraintException {
//		
//	}

	@Override
	public final File getInstanceFile() {
		return input;
	}


	@Override
	public void loadInstance(File file) {
		try {
			reader = new BufferedReader(new FileReader(file));
			input = file;
		}
		catch(FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, "txtparser...[loading][FAIL]", e);
			ChocoLogging.flushLogs();
			System.exit(2);
		}
		tokenizer = new StreamTokenizer(reader);
		tokenizer.slashSlashComments(true);
		tokenizer.slashStarComments(true);
		tokenizer.commentChar('#');
	}


	public final int[][] readArray(int n, int m,boolean rotate) {
		try {
			int lineno;
			int[][] r= rotate ? new int[m][n] : new int[n][m];
			for (int i = 0; i < n; i++) {
				if(rotate) {r[0][i]=readInteger();}
				else {r[i][0]=readInteger();}
				//r[i][0]=readInteger();
				lineno=tokenizer.lineno();
				for (int j = 1; j < m; j++) {
					if(rotate) {r[j][i]=ireadInteger();}
					else {r[i][j]=ireadInteger();}
					if(lineno!=tokenizer.lineno()) {
						throw new IllegalArgumentException("illegal array format");
					}
				}
			}
			return r;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "txtparser...[read-ints][FAIL]", e);
		}
		return null;
	}

	public final int[][] readArray(int n, int m) {
		return readArray(n, m, false);
	}

	public final int[] readArray(int n) {
		try {
			int[] r=new int[n];
			for (int i = 0; i < r.length; i++) {
				r[i]=ireadInteger();
			}
			return r;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "txtparser...[read-ints][FAIL]", e);
		}
		return null;
	}

	protected final int ireadInteger() throws IOException {
		if(tokenizer.nextToken()==StreamTokenizer.TT_NUMBER) {
			if(tokenizer.nval<=0) {
				LOGGER.log(Level.SEVERE, "txtparser...[read-negative-int:{0}][?]", tokenizer.nval);
			}
			if(tokenizer.nval != Math.round(tokenizer.nval)) {
				LOGGER.log(Level.SEVERE, "txtparser...[read-double:{0}][?]", tokenizer.nval);
			}
			return Double.valueOf(tokenizer.nval).intValue();
		}else {
			throw new IllegalArgumentException("txtparser...[read-int][FAIL]");
		}
	}
	
	protected final double ireadDouble() throws IOException {
		if(tokenizer.nextToken()==StreamTokenizer.TT_NUMBER) {
			return tokenizer.nval;
		}else {
			throw new IllegalArgumentException("txtparser...[read-int][FAIL]");
		}
	}

	public final String readString() {
		try {
			if(tokenizer.nextToken()!=StreamTokenizer.TT_EOF) {
				return tokenizer.sval;
			}else {
				throw new IllegalArgumentException("txtparser...[read-string][FAIL]");
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("txtparser...[read-string][FAIL]");
		}
	}
	public final int readInteger()  {
		try {
			return ireadInteger();
		} catch (IOException e) {
			throw new IllegalArgumentException("txtparser...[read-int][FAIL]");
		}
	}
	
	public final double readDouble()  {
		try {
			return ireadDouble();
		} catch (IOException e) {
			throw new IllegalArgumentException("txtparser...[read-double][FAIL]");
		}
	}


}
