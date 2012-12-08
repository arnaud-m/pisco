package pisco.shop.choco.branching.nogood;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Logger;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class NogoodUtils {

	private NogoodUtils() {}
	
	
	public static void formula2constraints(LinkedList<LinkedList<Term>> formula, BoolConstraintFactory ctFactory) {
		for (LinkedList<Term> terms : formula) {
			int npos=0;
			for (Term term : terms) {
				if(term.isPositive()) npos++;
			}
			IntDomainVar[] posLits = new IntDomainVar[npos];
			IntDomainVar[] negLits = new IntDomainVar[terms.size() - npos];
			int pidx=0;
			int nidx=0;
			for (Term term : terms) {
				if(term.isPositive()) posLits[pidx++]=term.getLitteral();
				else negLits[nidx++]=term.getLitteral();
			}
			ctFactory.postBoolConstraint(posLits, negLits);
			
		}
		
	}
	
	private final static LinkedList<LinkedList<Term>> dnf2cnf(Term lit, LinkedList<Term> and) {
		LinkedList<LinkedList<Term>> cnf = new LinkedList<LinkedList<Term>>();
		for (Term clit: and) {
			LinkedList<Term> or = new LinkedList<Term>();
			or.add(lit);
			or.add(clit);
			cnf.add(or);
		}
		return cnf;
	}
	
	public static LinkedList<LinkedList<Term>> dnf2cnf(LinkedList<LinkedList<Term>> dnf, BoolVariableFactory boolFactory) {
		if(dnf.isEmpty()) return new LinkedList<LinkedList<Term>>();
		LinkedList<LinkedList<Term>> cnf = null;
		LinkedList<Term> p = dnf.removeFirst();
		assert p.size() > 0;
		if(p.size() == 1) {
			if(dnf.size() == 1) {
				cnf = dnf2cnf(p.getFirst(), dnf.removeFirst());
			} else {
				cnf = dnf2cnf(dnf, boolFactory);
				for (LinkedList<Term> or : cnf) {
					or.addFirst(p.getFirst());
				}
			}

		} else {
			IntDomainVar b= boolFactory.makeBoolVar();
			Term z = new Term(b,true);
			Term nz = new Term(b, false);
			if(dnf.size() == 1) {
				cnf = dnf2cnf(nz, dnf.removeFirst());
			} else {
				cnf = dnf2cnf(dnf, boolFactory);
				for (LinkedList<Term> or : cnf) {
					or.addFirst(nz);
				}
			}
			//Less insertions, but at the beginning of the list for readability
			cnf.addAll(0, dnf2cnf(z,p));
		}
		return cnf;
	}
	
	private static String formula2string(LinkedList<LinkedList<Term>> formula, String termSep, String elemSep) {
		StringBuilder b =new StringBuilder();
		if(! formula.isEmpty() ) {	
		for (LinkedList<Term> and : formula) {
			if(and.size() == 1) {
				b.append(and.getFirst());
			} else {
				b.append('(');
				for (Term litteral : and) {
					b.append(litteral).append(termSep);
				}
				b.delete(b.length() -termSep.length(), b.length());
				b.append(")");
			}
			b.append(elemSep);
		}
		b.delete(b.length() -elemSep.length(), b.length());
		}
		return b.toString();
	}

	public static String dnf2string(LinkedList<LinkedList<Term>> dnf) {
		return formula2string(dnf, " & ", " |\n");
	}

	public static String cnf2string(LinkedList<LinkedList<Term>> cnf) {
		return formula2string(cnf, " | ", " &\n");
	}

	
	
}



