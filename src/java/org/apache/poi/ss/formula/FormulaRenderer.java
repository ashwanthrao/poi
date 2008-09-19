package org.apache.poi.ss.formula;

import java.util.List;
import java.util.Stack;

import org.apache.poi.hssf.record.formula.AttrPtg;
import org.apache.poi.hssf.record.formula.MemAreaPtg;
import org.apache.poi.hssf.record.formula.MemErrPtg;
import org.apache.poi.hssf.record.formula.MemFuncPtg;
import org.apache.poi.hssf.record.formula.OperationPtg;
import org.apache.poi.hssf.record.formula.ParenthesisPtg;
import org.apache.poi.hssf.record.formula.Ptg;

public class FormulaRenderer {
    /**
     * Convenience method which takes in a list then passes it to the
     *  other toFormulaString signature.
     * @param book   workbook for 3D and named references
     * @param lptgs  list of Ptg, can be null or empty
     * @return a human readable String
     */
    public static String toFormulaString(FormulaRenderingWorkbook book, List lptgs) {
        String retval = null;
        if (lptgs == null || lptgs.size() == 0) return "#NAME";
        Ptg[] ptgs = new Ptg[lptgs.size()];
        ptgs = (Ptg[])lptgs.toArray(ptgs);
        retval = toFormulaString(book, ptgs);
        return retval;
    }
    
    /**
     * Static method to convert an array of Ptgs in RPN order
     * to a human readable string format in infix mode.
     * @param book  workbook for named and 3D references
     * @param ptgs  array of Ptg, can be null or empty
     * @return a human readable String
     */
    public static String toFormulaString(FormulaRenderingWorkbook book, Ptg[] ptgs) {
        if (ptgs == null || ptgs.length == 0) {
            // TODO - what is the justification for returning "#NAME" (which is not "#NAME?", btw)
            return "#NAME";
        }
        Stack stack = new Stack();

        for (int i=0 ; i < ptgs.length; i++) {
            Ptg ptg = ptgs[i];
            // TODO - what about MemNoMemPtg?
            if(ptg instanceof MemAreaPtg || ptg instanceof MemFuncPtg || ptg instanceof MemErrPtg) {
                // marks the start of a list of area expressions which will be naturally combined
                // by their trailing operators (e.g. UnionPtg)
                // TODO - put comment and throw exception in toFormulaString() of these classes
                continue;
            }
            if (ptg instanceof ParenthesisPtg) {
                String contents = (String)stack.pop();
                stack.push ("(" + contents + ")");
                continue;
            }
            if (ptg instanceof AttrPtg) {
                AttrPtg attrPtg = ((AttrPtg) ptg);
                if (attrPtg.isOptimizedIf() || attrPtg.isOptimizedChoose() || attrPtg.isGoto()) {
                    continue;
                }
                if (attrPtg.isSpace()) {
                    // POI currently doesn't render spaces in formulas
                    continue;
                    // but if it ever did, care must be taken:
                    // tAttrSpace comes *before* the operand it applies to, which may be consistent
                    // with how the formula text appears but is against the RPN ordering assumed here
                }
                if (attrPtg.isSemiVolatile()) {
                    // similar to tAttrSpace - RPN is violated
                    continue;
                }
                if (attrPtg.isSum()) {
                    String[] operands = getOperands(stack, attrPtg.getNumberOfOperands());
                    stack.push(attrPtg.toFormulaString(operands));
                    continue;
                }
                throw new RuntimeException("Unexpected tAttr: " + attrPtg.toString());
            }

            if (ptg instanceof WorkbookDependentFormula) {
                WorkbookDependentFormula optg = (WorkbookDependentFormula) ptg;
				stack.push(optg.toFormulaString(book));
                continue;
            }
            if (! (ptg instanceof OperationPtg)) {
                stack.push(ptg.toFormulaString());
                continue;
            }

            OperationPtg o = (OperationPtg) ptg;
            String[] operands = getOperands(stack, o.getNumberOfOperands());
            stack.push(o.toFormulaString(operands));
        }
        if(stack.isEmpty()) {
            // inspection of the code above reveals that every stack.pop() is followed by a
            // stack.push(). So this is either an internal error or impossible.
            throw new IllegalStateException("Stack underflow");
        }
        String result = (String) stack.pop();
        if(!stack.isEmpty()) {
            // Might be caused by some tokens like AttrPtg and Mem*Ptg, which really shouldn't
            // put anything on the stack
            throw new IllegalStateException("too much stuff left on the stack");
        }
        return result;
    }

    private static String[] getOperands(Stack stack, int nOperands) {
        String[] operands = new String[nOperands];

        for (int j = nOperands-1; j >= 0; j--) { // reverse iteration because args were pushed in-order
            if(stack.isEmpty()) {
               String msg = "Too few arguments supplied to operation. Expected (" + nOperands
                    + ") operands but got (" + (nOperands - j - 1) + ")";
                throw new IllegalStateException(msg);
            }
            operands[j] = (String) stack.pop();
        }
        return operands;
    }
}