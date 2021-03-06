package de.heidelberg.collectionsexplorer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * 
 * Taken from here: https://github.com/javaparser/javasymbolsolver-maven-sample/
 * 
 * @author diego.costa
 *
 */
@DisplayName("Test that showcases how to use type solver")
public class TypeSolverExampleTest {

	
	@Test
	public void extractTypeInformation() throws Exception {
		
		// Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);

        // Parse some code
        CompilationUnit cu = JavaParser.parse("class X { int x() { return 1 + 1.0 - 5; } }");

        // Find all the calculations with two sides:
        cu.findAll(BinaryExpr.class).forEach(be -> {
            // Find out what type it has:
            ResolvedType resolvedType = be.calculateResolvedType();

         // Show that it's "double" in every case:
            System.out.println(be.toString() + " is a: " + resolvedType);
        });
		
	}
}
