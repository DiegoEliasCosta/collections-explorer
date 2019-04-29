package de.heidelberg.collectionsexplorer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import de.heidelberg.collectionsexplorer.beans.StreamOperationsInfo;
import de.heidelberg.collectionsexplorer.context.Result;
import de.heidelberg.collectionsexplorer.visitors.StreamAPIUsageVisitor;

public class StreamAPIUsageTest {

	String classA = new String("package de.heidelberg.collectionsexplorer.examples;" 
			+ "import java.util.HashMap;"
			+ "import java.util.Map;"
			+ "import java.util.Collectors;"
			+ "public class ClassA {"
			+ "	private Map<String, Integer> map = new HashMap<String, Integer>();" 
			+ "   private void method() {"
			+ "   	map.values().stream().filter(x -> x == 0).collect(Collectors.toList());"
			+ "	 }" 
			+ "}");


	@Test
	public void parsingClassAStreamAPIUsage() {

		try {
			// Set up a minimal type solver that only looks at the classes used to run this
			// sample.
			CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
			combinedTypeSolver.add(new ReflectionTypeSolver());

			// Configure JavaParser to use type resolution
			JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
			JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);

			CompilationUnit compilationUnit = JavaParser.parse(classA);
			Result<StreamOperationsInfo> result = new Result<>("");

			compilationUnit.accept(new StreamAPIUsageVisitor(Filter.NO_FILTER), result);

			StreamOperationsInfo entries = result.getEntries().get(0);
			
			List<String> listedInfo = entries.getStreamOperations().getListedInfo();
			assertEquals(5, listedInfo.size());
			assertEquals("stream", listedInfo.get(2));
			assertEquals("filter", listedInfo.get(3));
			assertEquals("collect", listedInfo.get(4));

			assertEquals("de.heidelberg.collectionsexplorer.examples", entries.getPackageName());
			
		} catch (Exception e) {
			assertFalse(true);
			throw e;
		}
	}
	



}
