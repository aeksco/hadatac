package org.hadatac.console.controllers.metadata.concepts;

import org.hadatac.console.http.GetSparqlQuery;

import java.io.IOException;
import java.util.TreeMap;

import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadata.concepts.entitycharacteristic_browser;
import org.hadatac.console.views.html.error_page;


public class EntityCharacteristic extends Controller {
	
	// for /metadata HTTP GET requests
    public static Result index() {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "EntityCharacteristics";
        String query_json = null;
        System.out.println("EntityCharacteristic.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            System.out.println("query_json = " + query_json);
            theResults = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("EntityCharacteristic index() was called!");
        return ok(entitycharacteristic_browser.render(theResults, tabName));
    }// /index()


    // for /metadata HTTP POST requests
    public static Result postIndex() {        
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults theResults;
        String tabName = "EntityCharacteristics";
        String query_json = null;
        System.out.println("EntityCharacteristic.java is requesting: " + tabName);
        try {
            query_json = query_submit.executeQuery(tabName);
            theResults = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
            //e1.printStackTrace();
        }
        System.out.println("EntityCharacteristic postIndex() was called!");
        return ok(entitycharacteristic_browser.render(theResults, tabName));
    }// /postIndex()
}
