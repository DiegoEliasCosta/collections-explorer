package de.heidelberg.collectionsexplorer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import de.heidelberg.collectionsexplorer.beans.GenericInfo;

public class VisitorReportContext<T extends GenericInfo> {
	
	private Report report;
	private VisitorType visitorType;
	private Filter filter;
	
	
	public Report getReport() {
		return this.report;
	}
	
	public void inspect(CompilationUnit cu, String path, TypeSolver solver) {
		
		Result<T> objResult = new Result<>(path);
		
		// We have a state per file 
		VoidVisitorAdapter<Result<T>> instance = visitorType.getInstance(filter, solver);
		
		cu.accept(instance, objResult);
		report.add(objResult);
		
	}

}
