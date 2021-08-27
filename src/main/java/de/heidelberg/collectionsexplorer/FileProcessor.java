package de.heidelberg.collectionsexplorer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.List;

import org.pmw.tinylog.Logger;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;

import de.heidelberg.collectionsexplorer.context.Result;
import de.heidelberg.collectionsexplorer.context.VisitorReportContext;
import de.heidelberg.collectionsexplorer.context.VisitorType;
import me.tongfei.progressbar.ProgressBar;

/**
 * File processor provides the logic for parsing either a file or a path using
 * JavaParser. When it encounters a file, it will call the MyVisitor function,
 * which in turn creates a result, which can then be added to a final report.
 * 
 * A result contains all the found "new" statements in a given file. A report is
 * a list of results, by file.
 * 
 * @author diego.costa
 *
 */
public class FileProcessor {
	
	private static final String UTF_8 = "utf-8";
	
	EnumMap<VisitorType, VisitorReportContext<?>> visitorCtxs;
	Filter filter;
	
	public FileProcessor(Filter filter) throws IOException {
		super();
		this.filter = filter;
		this.visitorCtxs = new EnumMap<>(VisitorType.class);
	}


	/**
	 * This method handles processing a giving File. Processing in this context
	 * means that it will parse the file with JavaParser.parse(), then have it
	 * be handled by MyVisitor, which in turn compiles results. Those results
	 * are appended to a Report object.
	 * 
	 * @param f
	 *            the file to be parsed
	 * @return
	 * 		The {@link Result} object containing the info of the file or <code>null</code> 
	 * in case of any {@link Exception} in the parse 
	 */
	public void process(File f) { 

		try(FileInputStream in = new FileInputStream(f.getAbsolutePath())){
			CompilationUnit cu;
			try {
				cu = StaticJavaParser.parse(in, Charset.forName(UTF_8));
				
				for(VisitorReportContext<?> ctx : visitorCtxs.values()) {
					ctx.inspect(cu, f.getAbsolutePath());
				}
				
			} catch (Error e) {
				Logger.error(String.format("Critical Javaparser error while processing the file %s.", f.getName()));
			}
			
		} catch (Exception e) {
			// We can ignore small errors here
			Logger.error(String.format("Error while processing the file %s.", f.getName()));
		}
	}
	
	/**
	 * Process a List of Files
	 * @param filesList
	 * @param solver 
	 */
	public void process(List<File> filesList) {
		
		Logger.info(String.format("%d files to process", filesList.size()));
		
		ProgressBar pb = new ProgressBar("Processing", filesList.size());
		pb.start();
		
		for(File file: filesList) {
			Logger.debug(String.format("Processing file %s", file.getPath()));
			process(file);
			pb.step();
		}
		
		pb.stop();
	}


	public EnumMap<VisitorType, VisitorReportContext<?>> getAllVisitorContexts() {
		return this.visitorCtxs;
	}


	public void addVisitorContext(VisitorType type) {
		this.visitorCtxs.put(type, new VisitorReportContext<>(type, filter));
	}

}
