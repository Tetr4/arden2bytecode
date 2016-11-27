package arden.compiler;

import arden.compiler.analysis.DepthFirstAdapter;
import arden.compiler.node.TIdentifier;

/**
 * Check the AST for errors, which were allowed by the grammar, and therefore
 * have not been caught by the parser.
 */
public class ErrorAnalysis extends DepthFirstAdapter {

	@Override
	public void caseTIdentifier(TIdentifier node) {
		if (node.getText().length() > 80) {
			throw new RuntimeCompilerException(node, "Identifiers must be 1 to 80 characters in length");
		}
	}

}
