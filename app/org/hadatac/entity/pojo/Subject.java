package org.hadatac.entity.pojo;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;


public class Subject {
	public String uri = "";
	public String type = "";
	public String label = "";
	public String ofStudy = "";
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getOfStudy() {
		return ofStudy;
	}
	public void setOfStudy(String ofStudy) {
		this.ofStudy = ofStudy;
	}
	
	public static boolean isPlatform(String subject_uri) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?cohort WHERE {\n"
				+ "  <" + subject_uri + "> a hasco:SubjectPlatform . \n"
				+ "  <" + subject_uri + "> hasco:isSubjectOf ?cohort . \n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			return true;
		}
		
		return false;
	}
	
	public static Subject findSubject(String study_uri, String subject_id) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?uri WHERE {\n"
				+ "  ?uri hasco:originalID \"" + subject_id + "\" .\n"
				+ "  ?uri hasco:isSubjectOf ?cohort .\n"
				+ "  ?cohort hasco:isCohortOf " + study_uri + " .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			Subject subject = new Subject();
			QuerySolution soln = resultsrw.next();
			subject.setUri(soln.getResource("uri").getURI());
			return subject;
		}
		
		return null;
	}
	
	public static String findSampleUri(String study_uri, String sample_id) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?sampleUri WHERE {\n"
				+ "  ?sampleUri hasco:originalID \"" + sample_id + "\" .\n"
				+ "  ?sampleUri hasco:isSampleOf ?subjectUri .\n"
				+ "  ?subjectUri hasco:isSubjectOf ?cohort .\n"
				+ "  ?cohort hasco:isCohortOf " + study_uri + " .\n"
				+ "}";
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			return soln.getResource("sampleUri").getURI();
		}
		
		return null;
	}
	
	public static String findCodeValue(String attr_uri, String code) {
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?codeValue WHERE {"
                + " ?uri hasco:hasReference <" + attr_uri + ">."
                + " ?value hasco:isPossibleValueOf ?uri . "
                + " ?value hasco:hasCode \"" + code + "\" . "
                + " OPTIONAL { ?value hasco:hasCodeValue ?codeValue . }"        
                + " }";
        
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
        ResultSet results = qexec.execSelect();
        ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
        
        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            if (null != soln.getLiteral("codeValue")) {
            	String codeValue = soln.getLiteral("codeValue").toString();
            	if (!codeValue.equals("")) {
            		return codeValue;
            	}
            }
        }
        
        return null;
    }
	
	public static String checkObjectUri(String obj_uri, String attr_uri) {
		attr_uri = ValueCellProcessing.replacePrefixEx(attr_uri);
		String objUri = obj_uri;
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?s ?o WHERE {"
                + " ?ar hasco:hasReference <" + attr_uri + "> . "
                + " ?ar rdfs:label ?l ."
                + " ?s hasco:hasAssociatedObject ?o . "
                + " ?s rdfs:label ?l . "       
                + " }";
        
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
        ResultSet results = qexec.execSelect();
        ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
        
        //System.out.println("resultsrw.size(): " + resultsrw.size());
        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            if (null != soln.getResource("o")) {
            	String attributeAssociation = soln.getResource("o").toString();
            	//System.out.println("attributeAssociation: " + attributeAssociation);
            	if (attributeAssociation.equals(ValueCellProcessing.replacePrefixEx("chear-kb:ObjectTypeMother"))) {
            		String motherUri = obj_uri + "-mother";
            		
            		Model model = ModelFactory.createDefaultModel();
            		DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(
            				Collections.getCollectionsName(Collections.METADATA_GRAPH));
            		model.add(model.createResource(motherUri), 
            				model.createProperty("rdf:type"),
            				model.createResource(ValueCellProcessing.replacePrefixEx("sio:Human")));
            		
            		model.add(model.createResource(motherUri), 
            				model.createProperty(ValueCellProcessing.replacePrefixEx("chear:Mother")),
            				model.createResource(obj_uri));

            		accessor.add(model);
            		objUri = motherUri;
            		//System.out.println("================================== Changed to: " + motherUri);
            	}
            }
        }
        
        return objUri;
    }
}


