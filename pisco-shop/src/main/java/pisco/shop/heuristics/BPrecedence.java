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
package pisco.shop.heuristics;


public abstract class BPrecedence {

	public BPrecedence opposite;

	//t1 < t2
	public BTask t1;
	public BTask t2;

	public BPrecedence(final BTask t1, final BTask t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	public void setOpposite(final BPrecedence p) {
		opposite = p;
	}

	public boolean isSameMachine() {
		return t1.machine == t2.machine;
	}

	public boolean isSameJob() {
		return t1.job == t2.job;
	}

	@Override
	public int hashCode() {
		return t1.idxt + t2.idxt * 1000;
	}

	/**
	 * a path in the precedence network exists , or is entailed.
	 *
	 */
	public abstract boolean isSatisfied();


	/**
	 * The constraints need to be checked.
	 *
	 */
	public abstract boolean isChecked();

	@Override
	public String toString() {
		return t1.pretty()+"<<"+t2.pretty();
	}



}


