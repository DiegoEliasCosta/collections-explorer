package de.heidelberg.collectionsexplorer.visitors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import de.heidelberg.collectionsexplorer.Filter;
import de.heidelberg.collectionsexplorer.Result;
import de.heidelberg.collectionsexplorer.beans.ObjectCreationInfo;
import de.heidelberg.collectionsexplorer.beans.ObjectCreationInfo.ObjectCreationInfoBuilder;
import de.heidelberg.collectionsexplorer.beans.StringListInfo;

/**
 * Visitor that records object creation instances on code.
 * 
 * @author diego.costa
 *
 */
public class ObjectCreationVisitor extends VoidVisitorAdapter<Result<ObjectCreationInfo>> {

	Filter filter;
	
	Map<String, String> importsDeclared;

	public ObjectCreationVisitor(Filter filter) {
		this.filter = filter;
		importsDeclared = new HashMap<>();
	}
	
	@Override
	public void visit(ObjectCreationExpr n, Result<ObjectCreationInfo> ret) {

		String type = n.getTypeAsString();
		if (filter.filter_match(type)) {
			ret.add(parse(n));
		}
		super.visit(n, ret);
	}
	
	@Override
	public void visit(ImportDeclaration n, Result<ObjectCreationInfo> arg) {
			
		Name name = n.getName();
		
		String identifier = name.getIdentifier();
		Optional<Name> qualifier = name.getQualifier();
		
		if(qualifier.isPresent()) {
			importsDeclared.put(identifier, qualifier.get().asString());
		}
		
		super.visit(n, arg);
		
	}


	private ObjectCreationInfo parse(ObjectCreationExpr exp) {

		// Builds the info
		ObjectCreationInfoBuilder builder = ObjectCreationInfo.builder();

		// Class
		builder.className(retrieveClass(exp));
		
		// Type Name
		builder.objectType(exp.getType().getNameAsString());
		
		// Full Name
		builder.fullObjectType(retrieveFullObjectType(exp));
		
		// Argument Types
		builder.argumentTypes(retrieveTypeArguments(exp));

		// Arguments
		builder.arguments(retrieveArguments(exp));

		// Position in the code
		Position position = exp.getBegin().get();
		builder.lineNumber(position.line);
		builder.columnNumber(position.column);
		return builder.build();
	}

	private String retrieveFullObjectType(ObjectCreationExpr exp) {
		
		String type = exp.getType().getNameAsString();
		
		if(importsDeclared.containsKey(type)) {
			String qualifiedName = importsDeclared.get(type);
			return qualifiedName + "." + type;
			
		}
		
		return type;
	}

	private StringListInfo retrieveArguments(ObjectCreationExpr exp) {

		NodeList<Expression> arguments = exp.getArguments();

		List<String> argumentsAsString = new ArrayList<>();
		// Populate
		arguments.stream().forEach(f -> argumentsAsString.add(f.toString()));

		return new StringListInfo(argumentsAsString);
	}

	private StringListInfo retrieveTypeArguments(ObjectCreationExpr n) {

		Optional<NodeList<Type>> typeArguments = n.getType().getTypeArguments();
		if (typeArguments.isPresent()) {

			NodeList<Type> nodeList = typeArguments.get();
			List<String> argumentTypes = new ArrayList<>();
			nodeList.stream().forEach(f -> argumentTypes.add(f.asString()));
			return new StringListInfo(argumentTypes);

		}
		// Empty
		return new StringListInfo();
	}

	private String retrieveClass(ObjectCreationExpr n) {
		Optional<ClassOrInterfaceDeclaration> ancestorOfType = n.findAncestor(ClassOrInterfaceDeclaration.class);
		if (ancestorOfType.isPresent()) {

			ClassOrInterfaceDeclaration declr = ancestorOfType.get();
			return declr.getNameAsString();
		}

		return "";
	}
}