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
package pisco.pack;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import parser.instances.InstanceFileParser;

final class BinPackingParser
  implements InstanceFileParser
{
  public File file;
  public int capacity;
  public int[] sizes;

  public File getInstanceFile()
  {
    return this.file;
  }

  public void loadInstance(File file)
  {
    this.file = file;
  }

  public void parse(boolean displayInstance)
  {
    Scanner sc = null;
    try {
      sc = new Scanner(this.file);
      int nb = sc.nextInt();
      this.capacity = sc.nextInt();
      this.sizes = new int[nb];
      for (int i = 0; i < nb; i++) {
        this.sizes[i] = sc.nextInt();
      }

      if (sc.hasNext())
        throw new InputMismatchException(this.file.getName() + ": too much entries.");
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();

      if ((displayInstance) && (LOGGER.isLoggable(Level.INFO)))
        LOGGER.log(Level.INFO, "capacity : {0},\nWeights : {1}.\n", 
          new String[] { Integer.toString(this.capacity), Arrays.toString(this.sizes) });
    }
  }

  public String getInstanceMessage() {
    return this.sizes.length + " ITEMS    " + this.capacity + " CAPACITY";
  }

  public void cleanup()
  {
    this.file = null;
    this.capacity = 0;
    this.sizes = null;
  }

  public Object getParameters() {
    return new Object[] { this.sizes, Integer.valueOf(this.capacity) };
  }
}

/* Location:           /home/nono/recovery/pack/
 * Qualified Name:     pisco.pack.BinPackingParser
 * JD-Core Version:    0.6.0
 */