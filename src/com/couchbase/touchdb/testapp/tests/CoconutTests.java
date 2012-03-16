package com.couchbase.touchdb.testapp.tests;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDStatus;
import com.couchbase.touchdb.TDView;
import com.couchbase.touchdb.TDViewMapBlock;
import com.couchbase.touchdb.TDViewMapEmitBlock;
import com.couchbase.touchdb.javascript.TDJavaScriptViewCompiler;
import com.couchbase.touchdb.router.TDURLConnection;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
import com.couchbase.touchdb.support.DirUtils;

import android.test.InstrumentationTestCase;
import android.util.Log;

public class CoconutTests extends InstrumentationTestCase{
	private static boolean initializedUrlHandler = false;

    public static final String TAG = "CoconutTests";

    protected String getServerPath() {
        String filesDir = getInstrumentation().getContext().getFilesDir().getAbsolutePath() + "/tests";
        return filesDir;
    }

    @Override
    protected void setUp() throws Exception {

        //delete and recreate the server path
        String serverPath = getServerPath();
        File serverPathFile = new File(serverPath);
        DirUtils.deleteRecursive(serverPathFile);
        serverPathFile.mkdir();

        //for some reason a traditional static initializer causes junit to die
        if(!initializedUrlHandler) {
            URL.setURLStreamHandlerFactory(new TDURLStreamHandlerFactory());
            initializedUrlHandler = true;
        }
    }
    
    public void testCoconut() {

        TDServer server = null;
		TDURLConnection conn;
        String filesDir = getInstrumentation().getContext().getFilesDir().getAbsolutePath();
        try {
            server = new TDServer(filesDir);
        } catch (IOException e) {
            fail("Creating server caused IOException");
        }
        TDDatabase db = server.getDatabaseNamed("coconut-emas");
        TDView view = db.getViewNamed("byIncidentSorted");
        view.setMapReduceBlocks(new TDViewMapBlock() {
            @Override
            public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
            	if (document.get("formID").equals("incident")) {
                    emitter.emit(document.get("lastModified"), document);
            	}
            }
        }, null, "1");
        TDView.setCompiler(new TDJavaScriptViewCompiler());
        int limit = 16;
        String viewQuery = "byIncidentSorted?descending=true&limit=" + limit;
        //TDDatabase coconutDb = server.getDatabaseNamed("coconut-emas");
        //view = coconutDb.getViewNamed("byIncidentSorted");
        String path = "/coconut-emas/_design/coconut/_view/byIncidentSorted";
		conn = Router.sendRequest(server, "GET", path, null, null);
        Map<String,Object> result;
        result = (Map<String, Object>) Router.parseJSONResponse(conn);
        Log.v(TAG, String.format("%s --> %d", path, conn.getResponseCode()));
        
        Map<String,Object> doc1 = new HashMap<String,Object>();
        doc1.put("parentId", "12345");
        doc1.put("pi", "day");
        //result = (Map<String,Object>)Router.sendBody(server, "PUT", "/coconut-emas/abcdef", doc1, TDStatus.CREATED, null);
        conn = Router.sendRequest(server, "PUT", "/coconut-emas/abcdef", null, doc1);
        result = (Map<String, Object>) Router.parseJSONResponse(conn);
        Log.v(TAG, String.format("%s --> %d", path, conn.getResponseCode()));

        view = db.getViewNamed("byParentId");
        view.setMapReduceBlocks(new TDViewMapBlock() {
            @Override
            public void map(Map<String, Object> document, TDViewMapEmitBlock emitter) {
            	if (document.get("parentId").equals("12345")) {
                    emitter.emit(document.get("parentId"), document);
            	}
            }
        }, null, "1");
        path = "/coconut-emas/_design/coconut/_view/byParentId";
        // Specific keys:
        //TDQueryOptions options = new TDQueryOptions();
        List<Object> keys = new ArrayList<Object>();
        //keys.add("a5a7608b-19f8-4048-98bc-e2b54514569e");
        keys.add("12345");
        //options.setKeys(keys);

        Map<String,Object> bodyObj = new HashMap<String,Object>();
        bodyObj.put("keys", keys);

        //List<Map<String,Object>> bulk_result  = (ArrayList<Map<String,Object>>)sendBody(server, "POST", path, bodyObj, TDStatus.CREATED, null);
        conn = Router.sendRequest(server, "POST", path, null, bodyObj);
        result = (Map<String, Object>) Router.parseJSONResponse(conn);
        Log.v(TAG, String.format("%s --> %d", path, conn.getResponseCode()));
        server.close();
	}
}
