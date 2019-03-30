package de.heidelberg.collectionsexplorer.util;

import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;

/**
 * Utility methods for extracting information from the {@link Node} object.
 * Use this class to avoid replicating code throughout the project.
 * 
 * @author diego.costa
 *
 */
public class ParserUtil {
	
	private static final int NO_INFO = -1;

	public static String retrieveClass(Node n) {
		
		Optional<ClassOrInterfaceDeclaration> ancestorOfType = n.findAncestor(ClassOrInterfaceDeclaration.class);

		if (ancestorOfType.isPresent()) {
			ClassOrInterfaceDeclaration declr = ancestorOfType.get();
			return declr.getNameAsString();
		}

		return "";
	}

	public static int getLineNumber(NodeWithRange<?> n) {
		Optional<Position> begin = n.getBegin();
		if(begin.isPresent()) {
			return begin.get().line;
		}
		
		return NO_INFO;
	}
	
	public static int getColumn(NodeWithRange<?> n) {
		Optional<Position> begin = n.getBegin();
		if(begin.isPresent()) {
			return begin.get().column;
		}
		
		return NO_INFO;
	}

	public static String retrievePackageName(Node node) {
		
		Optional<CompilationUnit> ancestorOfType = node.findAncestor(CompilationUnit.class);
		
		if(ancestorOfType.isPresent()) {
			CompilationUnit packageDeclaration = ancestorOfType.get();
			
			Optional<PackageDeclaration> pkg = packageDeclaration.getPackageDeclaration();
			if(pkg.isPresent()) {
				return pkg.get().getNameAsString();
			}
		}
		
		return "";
	}
	
}
