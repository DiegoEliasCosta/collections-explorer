package de.heidelberg.collectionsexplorer.beans;

import com.opencsv.bean.CsvBindByName;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
public class StreamOperationsInfo extends GenericInfo {
	
	@Getter	@CsvBindByName private String className;
	@Getter	@CsvBindByName private String packageName;
	
	@Getter	@CsvBindByName private int lineNumber;
	@Getter	@CsvBindByName private int columnNumber;
	
	@Getter	@CsvBindByName private String sourceType;
	@Getter @CsvBindByName private StringListInfo streamOperations;
	@Getter @CsvBindByName private StringListInfo argumentTypes;
	
	@Getter @CsvBindByName private String fullStreamOperation;
	

}
