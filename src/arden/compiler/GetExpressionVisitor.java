// arden2bytecode
// Copyright (c) 2010, Daniel Grunwald
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification, are
// permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice, this list
//   of conditions and the following disclaimer.
//
// - Redistributions in binary form must reproduce the above copyright notice, this list
//   of conditions and the following disclaimer in the documentation and/or other materials
//   provided with the distribution.
//
// - Neither the name of the owner nor the names of its contributors may be used to
//   endorse or promote products derived from this software without specific prior written
//   permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &AS IS& AND ANY EXPRESS
// OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
// IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

package arden.compiler;

import arden.compiler.analysis.AnalysisAdapter;
import arden.compiler.node.AAgoExprBefore;
import arden.compiler.node.AAndExprOr;
import arden.compiler.node.ABeforeExprConstruct;
import arden.compiler.node.ACompExprNot;
import arden.compiler.node.AConstrExprPower;
import arden.compiler.node.AExpfExprFactor;
import arden.compiler.node.AExprExprFunction;
import arden.compiler.node.AFuncExprAgo;
import arden.compiler.node.ANotExprAnd;
import arden.compiler.node.AOrExprRange;
import arden.compiler.node.APlusExprString;
import arden.compiler.node.APowerExprTimes;
import arden.compiler.node.ARangeExprWhere;
import arden.compiler.node.ASortExpr;
import arden.compiler.node.AStrExprComparison;
import arden.compiler.node.ATimesExprPlus;
import arden.compiler.node.AWhereExprSort;
import arden.compiler.node.Node;

/**
 * Retrieves the first concrete expression node.
 * 
 * @author Daniel Grunwald
 * 
 */
final class GetExpressionVisitor extends AnalysisAdapter {
	Node result;

	@Override
	public void defaultCase(Node node) {
		result = node;
	}

	@Override
	public void caseASortExpr(ASortExpr node) {
		node.getExprSort().apply(this);
	}

	@Override
	public void caseAWhereExprSort(AWhereExprSort node) {
		node.getExprWhere().apply(this);
	}

	@Override
	public void caseARangeExprWhere(ARangeExprWhere node) {
		node.getExprRange().apply(this);
	}

	@Override
	public void caseAOrExprRange(AOrExprRange node) {
		node.getExprOr().apply(this);
	}

	@Override
	public void caseAAndExprOr(AAndExprOr node) {
		node.getExprAnd().apply(this);
	}

	@Override
	public void caseANotExprAnd(ANotExprAnd node) {
		node.getExprNot().apply(this);
	}

	@Override
	public void caseACompExprNot(ACompExprNot node) {
		node.getExprComparison().apply(this);
	}

	@Override
	public void caseAStrExprComparison(AStrExprComparison node) {
		node.getExprString().apply(this);
	}

	@Override
	public void caseAPlusExprString(APlusExprString node) {
		node.getExprPlus().apply(this);
	}

	@Override
	public void caseATimesExprPlus(ATimesExprPlus node) {
		node.getExprTimes().apply(this);
	}

	@Override
	public void caseAPowerExprTimes(APowerExprTimes node) {
		node.getExprPower().apply(this);
	}

	@Override
	public void caseAConstrExprPower(AConstrExprPower node) {
		node.getExprConstruct().apply(this);
	}

	@Override
	public void caseABeforeExprConstruct(ABeforeExprConstruct node) {
		node.getExprBefore().apply(this);
	}

	@Override
	public void caseAAgoExprBefore(AAgoExprBefore node) {
		node.getExprAgo().apply(this);
	}

	@Override
	public void caseAFuncExprAgo(AFuncExprAgo node) {
		node.getExprFunction().apply(this);
	}

	@Override
	public void caseAExprExprFunction(AExprExprFunction node) {
		node.getExprFactor().apply(this);
	}

	@Override
	public void caseAExpfExprFactor(AExpfExprFactor node) {
		node.getExprFactorAtom().apply(this);
	}
}
