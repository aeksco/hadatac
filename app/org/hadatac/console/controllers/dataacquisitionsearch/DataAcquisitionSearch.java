package org.hadatac.console.controllers.dataacquisitionsearch;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.JsonHandler;
import org.hadatac.console.controllers.metadataacquisition.ViewSubject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;

import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.FacetsWithCategories;
import org.hadatac.console.models.SpatialQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.ObjectDetails;

import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.formdata.FacetFormData;
import org.hadatac.console.views.html.dataacquisitionsearch.dataacquisition_browser;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.entity.pojo.Measurement;


public class DataAcquisitionSearch extends Controller {

    public static FacetFormData facet_form = new FacetFormData();
    public static FacetsWithCategories field_facets = new FacetsWithCategories();
    public static FacetsWithCategories query_facets = new FacetsWithCategories();
    public static FacetsWithCategories pivot_facets = new FacetsWithCategories();
    public static FacetsWithCategories range_facets = new FacetsWithCategories();
    public static FacetsWithCategories cluster_facets = new FacetsWithCategories();
    public static SpatialQueryResults query_results = new SpatialQueryResults();
    
    //Postconditions: field_facets will be modified if there are facets in the JsonHandler
    public static void getFacets(JsonHandler jh){
    	//Get the facets
        try {
            if (jh.getFieldCountJson()) {
                for (String key : jh.categories_and_facets.keySet()) {
                    for (String facet : jh.categories_and_facets.get(key)){
                        if (facet.equals("null")) {field_facets.addFacet(key, "missing"); continue;}
                        field_facets.addFacet(key, facet);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public static List<String> getPermissions(String permissions) {
    	List<String> result = new ArrayList<String>();
    	
    	if (permissions != null) {
	    	StringTokenizer tokens = new StringTokenizer(permissions, ",");
	    	while (tokens.hasMoreTokens()) {
	    		result.add(tokens.nextToken());
	    	}
    	}
    	
    	return result;
    }

    public static Result index(int page, int rows, String facets) {
    	System.out.println("[DataAcquisitionSearch] Page: " + page + "   Rows:" + rows + "   Facets:" + facets);
    	
    	FacetHandler handler = new FacetHandler();
    	handler.loadFacets(facets);
    	System.out.println("DataAcquisitionSearch : <" + handler.toSolrQuery() + ">");

    	AcquisitionQueryResult results = null;
    	String ownerUri;
    	final SysUser user = AuthApplication.getLocalUser(session());
    	if(null == user){
    	    ownerUri = "Public";
    		results = Measurement.find(ownerUri, page, rows, handler);
    	}
    	else{
    		ownerUri = UserManagement.getUriByEmail(user.getEmail());
    		if(null == ownerUri){
    			ownerUri = "Public";
    		}
    		results = Measurement.find(ownerUri, page, rows, handler);
    	}
    	System.out.println("[DataAcquisitionSearch] Total size response: " + results.getDocumentSize());
    	
    	Set<String> setObj = new HashSet<String>();
    	ObjectDetails objDetails = new ObjectDetails();
    	if (results != null) {
    		for (Measurement m: results.getDocuments()) {
    			setObj.add(m.getObjectUri());
            }
            for (String uri: setObj) {
            	if (uri != null) {
            		String html = ViewSubject.findBasicHTML(uri);
            		if (html != null) {
            			objDetails.putObject(uri, html);
            		}
            	}
            }
        }

    	return ok(dataacquisition_browser.render(page, rows, facets, results.getDocumentSize(), 
    			results, results.toJSON(), handler, Measurement.buildQuery(ownerUri, page, rows, handler), 
    			objDetails.toJSON()));
    }

    public static Result postIndex(int page, int rows, String facets) {
    	return index(page, rows, facets);
    }
}
