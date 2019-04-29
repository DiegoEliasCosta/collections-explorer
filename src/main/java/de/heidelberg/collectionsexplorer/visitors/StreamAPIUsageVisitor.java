package de.heidelberg.collectionsexplorer.visitors;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.pmw.tinylog.Logger;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;

import de.heidelberg.collectionsexplorer.Filter;
import de.heidelberg.collectionsexplorer.beans.StreamOperationsInfo;
import de.heidelberg.collectionsexplorer.beans.StreamOperationsInfo.StreamOperationsInfoBuilder;
import de.heidelberg.collectionsexplorer.beans.StringListInfo;
import de.heidelberg.collectionsexplorer.context.Result;
import de.heidelberg.collectionsexplorer.util.ParserUtil;

// FIXME: This class can be more generic - extract all methodCall until you
// reach a particular methodCall (anchor)
public class StreamAPIUsageVisitor extends VoidVisitorAdapter<Result<StreamOperationsInfo>> {

    private static final String UNKNOWN_TYPE = "UNK";

    private static final String STREAM = "stream";
    private static final String PARALLEL_STREAM = "parallelStream";

    public StreamAPIUsageVisitor(Filter filter) {
        // Ignoring filter for now...

    }

    @Override
    public void visit(final MethodCallExpr n, final Result<StreamOperationsInfo> result) {

        // super.visit(n,arg);
        List<MethodCallExpr> allExpCalls = n.findAll(MethodCallExpr.class);

        // Get stream operations
        StreamOperationsInfo info = extractStreamOperations(n, allExpCalls);
        if (info != null) {
            result.add(info);
        }

    }

    private StreamOperationsInfo extractStreamOperations(MethodCallExpr methodCall, List<MethodCallExpr> allExpCalls) {

        // Find if there is a stream method call in the chain
		Optional<MethodCallExpr> streamMethodCall = allExpCalls.stream()
				.filter(x -> returnsStreamType(x)).findAny();

        if (streamMethodCall.isPresent()) { // Stream method confirmed

            StreamOperationsInfoBuilder builder = StreamOperationsInfo.builder();

            // FullStreamCall
            builder.fullStreamOperation(methodCall.toString());

            // Class Name
            builder.className(ParserUtil.retrieveClass(methodCall));

            // Package Name
            builder.packageName(ParserUtil.retrievePackageName(methodCall));

            // Position (line + col)
            builder.lineNumber(ParserUtil.getLineNumber(methodCall));
            builder.columnNumber(ParserUtil.getColumn(methodCall));

            // Stream chain operations
            StringListInfo chain = extractMethodChain(methodCall);
            builder.streamOperations(chain);

            // Source type
            Optional<Expression> scope = streamMethodCall.get().getScope();
            String type = extractType(scope);
            builder.sourceType(type);

            return builder.build();
        }

        return null;
    }

    private boolean returnsStreamType(MethodCallExpr methodCallExpr) {

        try {
            ResolvedMethodDeclaration resolve = methodCallExpr.resolve();
            String describe = resolve.getReturnType().describe();
            // Return
            return describe.startsWith("java.util.stream");

        } catch (Exception e) {
            // Trace level as this is expected to happen quite often
            Logger.trace(String.format("Error while identifying the " +
                    "types for the method call = %s", methodCallExpr.toString()));

        }

        return methodCallExpr.getNameAsString().equals("stream")
                || methodCallExpr.getNameAsString().equals("parallelStream");

    }

    private StringListInfo extractMethodChain(MethodCallExpr expr) {

        List<MethodCallExpr> allMethodCalls = expr.findAll(MethodCallExpr.class);

        // Current implementation takes every single method call even the ones
        // not related to stream -> This needs to be filtered later
        List<String> streamChain = allMethodCalls.stream()
                .map(x -> x.getNameAsString()).collect(Collectors.toList());

        // We add the stream/parallelstream as our search above does not
        // cover the stream method itself (takeWhile)
        //streamChain.add(methodCallAnchor.getNameAsString());

        // We need to reverse here as we walk
        // from the last operation -> stream
        Collections.reverse(streamChain);

        return new StringListInfo(streamChain);
    }

    private String extractType(Optional<Expression> scope) {
        try {
            if (scope.isPresent()) {

                Expression expression = scope.get();
                ResolvedType resolvedType = expression.calculateResolvedType();

                // ResolvedType rt = JavaParserFacade.get(solver).getType(scope.get());
                return resolvedType.describe();
            }
        } catch (Exception e) {
            // Trace level as this is expected to happen quite often
            Logger.trace(String.format("Error while identifying the types for the scope = %s", scope.get().toString()));
        }
        return UNKNOWN_TYPE;
    }

}
