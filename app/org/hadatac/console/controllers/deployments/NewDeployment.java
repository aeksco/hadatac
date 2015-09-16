package org.hadatac.console.controllers.deployments;

import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;

import org.hadatac.console.views.html.*;
import org.hadatac.console.views.html.deployments.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.console.models.DeploymentForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.http.GenericSparqlQuery;

public class NewDeployment extends Controller {
	
    public static String PREFIXES = 
    		"PREFIX sioc: <http://rdfs.org/sioc/ns#>  " + 
    		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>  " +
    		"PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>  " +
    	    "PREFIX owl: <http://www.w3.org/2002/07/owl#>  " +
    	    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>  " +
    	    "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>  " +
    	    "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
    	    "PREFIX vstoi: <http://hadatac.org/ont/vstoi#>  " +
    	    "PREFIX hasneto: <http://hadatac.org/ont/hasneto#>  " +
    	    "  ";
    
    public static String DEPLOYMENT_ABBREV = "DP";

    public static String DATA_COLLECTION_ABBREV = "DC";
    
    public static String DATASET_ABBREV = "DS";
    
    public static String CONSOLE_ID = "00000001";
    
    public static SparqlQueryResults getQueryResults(String tabName) {
	    SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults thePlatforms = null;
    	String query_json = null;
        try {
            query_json = query_submit.executeQuery(tabName);
            //System.out.println("query_json = " + query_json);
            thePlatforms = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | IOException | NullPointerException e1) {
            e1.printStackTrace();
        }
		return thePlatforms;
	}
    
    public static String getNextDynamicMetadataURI(String category) {
    	String metadataId = Long.toHexString(DataFactory.getNextDynamicMetadataId());
    	String host = Play.application().configuration().getString("hadatac.console.host");
    	for (int i = metadataId.length(); i <= 8; i++) {
    		metadataId = "0" + metadataId;
    	}
    	return host + "/hadatac/kb/" + category + "/" + CONSOLE_ID + "/" + metadataId + "/" ;   
    }
    
    // for /metadata HTTP GET requests
    public static Result index() {
    	return ok(newDeployment.render(Form.form(DeploymentForm.class), 
    			  getQueryResults("Platforms"),
    			  getQueryResults("Instruments"),
    			  getQueryResults("Detectors")));
        
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {

    	return ok(newDeployment.render(Form.form(DeploymentForm.class), 
  			  getQueryResults("Platforms"),
  			  getQueryResults("Instruments"),
  			  getQueryResults("Detectors")));
        
    }// /postIndex()

    //prov:startedAtTime		"2015-02-15T19:50:55Z"^^<http://www.w3.org/2001/XMLSchema#dateTime> .
        
    /**
     * Handles the form submission.
     */
    public static Result processForm() {
        Form<DeploymentForm> form = Form.form(DeploymentForm.class).bindFromRequest();
        DeploymentForm data = form.get();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String dateString = df.format(data.getStartDateTime());
        String insert = "";
        String deploymentUri = getNextDynamicMetadataURI(DEPLOYMENT_ABBREV);
        String dataCollectionUri = getNextDynamicMetadataURI(DATA_COLLECTION_ABBREV);
        String[] detectorUri = new String[1];
        detectorUri[0] = data.getDetector();
        Deployment deployment = DataFactory.createDeployment(deploymentUri, data.getPlatform(), data.getInstrument(), detectorUri, dateString);
        DataCollection dataCollection = DataFactory.createDataCollection(dataCollectionUri, deploymentUri);
        if (form.hasErrors()) {
        	System.out.println("HAS ERRORS");
            return badRequest(newDeployment.render(form,
			          getQueryResults("Platforms"),
			          getQueryResults("Instruments"),
			          getQueryResults("Detectors")));        
        } else {
            return ok(newDeploymentConfirm.render(data));
        }
    }

}