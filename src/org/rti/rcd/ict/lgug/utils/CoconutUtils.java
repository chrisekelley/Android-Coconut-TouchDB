package org.rti.rcd.ict.lgug.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.rti.rcd.ict.lgug.utils.AndCouch;

import com.couchbase.touchdb.TDDatabase;
import com.couchbase.touchdb.TDServer;
import com.couchbase.touchdb.TDStatus;
import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
import com.couchbase.touchdb.support.Base64;
import com.couchbase.touchdb.testapp.tests.Router;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;


public class CoconutUtils extends Activity {
	
	/**
	 * Reference to the Android context
	 */
	private static Context ctx;
	
	protected static final String TAG = "CoconutActivity";

	/**
	 *  Will check for the existence of a design doc and if it does not exist,
	 *  upload the json found at dataPath to create it
	 *  
	 *  if (dbName.equals(fileName)), then it is a design document and it compares the couchapphash to see if it needs updating
	 *  For other documents, it simply puts the file.
	 *  
	 * @param dbName - CouchDB name
	 * @param hostPortUrl - e.g.: http://0.0.0.0:5985
	 * @param docName - doc _id; dbName if null.
	 * @param fileName
	 */
	public void ensureLoadDoc(String dbName, String hostPortUrl, String docName, String fileName) {

		try {

			//Boolean toUpdate = true;
			Boolean dDoc = false;
			String data = null;
			data = readAsset(getAssets(), fileName);
			File hashCache = null;
			String md5 = null;

//			if (dbName.equals(fileName)) {
//				dDoc = true;
//				Log.v(TAG, fileName + " is a design document: " + docName + " .");
//			} else {
//				Log.v(TAG, fileName + " is not a design document.");
//			}
//
//			if (dDoc == true) {
//				hashCache = new File(CouchbaseMobile.dataPath() + "/couchapps/" + dbName + ".couchapphash");				
//				md5 = md5(data);
//				String cachedHash;
//
//				try {
//					cachedHash = readFile(hashCache);
//					toUpdate = !md5.equals(cachedHash);
//				} catch (Exception e) {
//					e.printStackTrace();
//					toUpdate = true;
//				}
//			} else {
//				//TODO: compare to version on server.
//				toUpdate = true;
//			}
			
			//Log.v(TAG, docName + " toUpdate: " + toUpdate);

			//if (toUpdate == true) {
				String docUrl = null;
				if (docName != null) {
					docUrl = hostPortUrl + dbName + "/" + docName;
				} else {
					JSONObject json = new JSONObject(data);
					docName = json.getString("_id");
					//docUrl = url + dbName + "/_design/" + dbName;
					docUrl = hostPortUrl + dbName + "/" + docName;
					Log.v(TAG, fileName + " has the docName: " + docName);
					Log.v(TAG, "docUrl: " + docUrl);
				}
				
				URL urlObject = new URL(docUrl);
				String protocol = urlObject.getProtocol();
				String hostName = urlObject.getHost();
				int port = urlObject.getPort();
				String path = urlObject.getPath();
				String queryString = urlObject.getQuery();
				
				URI uri = null;
				try {
					uri = new URI(
							protocol, 
							null, // userinfo
							hostName, 
							port,
							path,
							queryString,
					        null);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//String cleanUrlA = uri.toString();
				//Log.v(TAG, "URL toString: " + cleanUrlA + " path: " + path);
				
//				URI uri2 = null; 
//				try {
//					uri2 = new URI(docUrl.replace(" ", "%20"));
//					//Log.v(TAG, "uri2: " + uri2);
//				} catch (URISyntaxException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				String cleanUrl = uri.toASCIIString();

				AndCouch req = AndCouch.get(cleanUrl);
				Log.v(TAG, "cleanUrl: " + cleanUrl + " req.status: " + req.status);

				if (req.status == 404) {
					Log.v(TAG, "Uploading " + cleanUrl);
					//AndCouch.put(hostPortUrl + dbName, null);
					AndCouch.put(cleanUrl, data);
				} else if (req.status == 200) {
					Log.v(TAG, cleanUrl + " Found, Updating");
					String rev = req.json.getString("_rev");
					JSONObject json = new JSONObject(data);
					json.put("_rev", rev);
					//AndCouch.put(hostPortUrl + dbName, null);
					AndCouch.put(cleanUrl, json.toString());
				}

				if (dDoc == true) {
					new File(hashCache.getParent()).mkdirs();
					writeFile(hashCache, md5);
				}
//			} else {
//				Log.v(TAG, fileName + " is up to date.");
//			}

		} catch (IOException e) {
			e.printStackTrace();
			// There is no design doc to load
		} catch (JSONException e) {
			e.printStackTrace();
		}
	};

	
	public static String readAsset(AssetManager assets, String path) throws IOException {
		InputStream is = assets.open(path);
		int size = is.available();
		byte[] buffer = new byte[size];
		is.read(buffer);
		is.close();
		return new String(buffer);
	}

    public static String readFile(File file) throws IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    public static void writeFile(File file, String data) throws IOException {
    	FileWriter fstream = new FileWriter(file);
    	BufferedWriter out = new BufferedWriter(fstream);
    	out.write(data);
    	out.close();
    }
    
    public static String md5(String input){
        String res = "";
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(input.getBytes());
            byte[] md5 = algorithm.digest();
            String tmp = "";
            for (int i = 0; i < md5.length; i++) {
                tmp = (Integer.toHexString(0xFF & md5[i]));
                if (tmp.length() == 1) {
                    res += "0" + tmp;
                } else {
                    res += tmp;
                }
            }
        } catch (NoSuchAlgorithmException ex) {}
        return res;
    }
    

    /**
     * extracts a zip archive from the assets dir. Copies to a writable dir first.
     * @param ctx TODO
     * @param destinationDirectory TODO
     * @param argv
     * @throws Exception 
     */
    public static void unZipFromAssets (Context ctx, String file, String destinationDirectory) throws Exception {
    	String destinationFilename = extractFromAssets(ctx, file, destinationDirectory);		
    	try {
    		unZip(destinationFilename, destinationDirectory);
    	} catch (Exception e) {
    		throw new Exception(e);
    	}
    }


	public static String extractFromAssets(Context ctx, String file, String destinationDirectory) throws IOException, FileNotFoundException {
		final int BUFFER = 2048;
    	BufferedOutputStream dest = null;
    	AssetManager assetManager = ctx.getAssets();
    	InputStream in = assetManager.open(file);	
    	String destinationFilename = destinationDirectory + File.separator + file;
		OutputStream out = new FileOutputStream(destinationFilename);
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
		in.close();
		out.close();
		return destinationFilename;
	}
    
    /**
     * extracts a zip archive. Does not handle directories
     * kudos: http://java.sun.com/developer/technicalArticles/Programming/compression/
     * @param argv
     * @throws IOException 
     */
    public static void unZipNoDirs (String file, String destinationDirectory) throws IOException {
    	final int BUFFER = 2048;
    	BufferedOutputStream dest = null;
    	FileInputStream fis = new FileInputStream(file);
    	ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
    	ZipEntry entry;
    	while((entry = zis.getNextEntry()) != null) {
    		int count;
    		byte data[] = new byte[BUFFER];
    		// write the files to the disk
    		String destinationFilename = destinationDirectory + File.separator + entry.getName();
    		FileOutputStream fos = new FileOutputStream(destinationFilename);
    		dest = new BufferedOutputStream(fos, BUFFER);
    		while ((count = zis.read(data, 0, BUFFER)) != -1) {
    			dest.write(data, 0, count);
    		}
    		dest.flush();
    		dest.close();
    	}
    	zis.close();
    }
    
    /**
     * kudos: http://www.jondev.net/articles/Unzipping_Files_with_Android_(Programmatically)
     * @param zipFile
     * @param destinationDirectory
     */
    public static void unZip(String zipFile, String destinationDirectory) { 
    	try  { 
    		FileInputStream fin = new FileInputStream(zipFile); 
    		ZipInputStream zin = new ZipInputStream(fin); 
    		ZipEntry ze = null; 
    		while ((ze = zin.getNextEntry()) != null) { 
    			Log.v("Decompress", "Unzipping " + ze.getName()); 
    			String destinationPath = destinationDirectory + File.separator + ze.getName();
    			if(ze.isDirectory()) { 
    				dirChecker(destinationPath); 
    			} else { 
    				FileOutputStream fout;
					try {
						File outputFile = new File(destinationPath);
						if (!outputFile.getParentFile().exists()){
							dirChecker(outputFile.getParentFile().getPath());
						}
						fout = new FileOutputStream(destinationPath);
	    				for (int c = zin.read(); c != -1; c = zin.read()) { 
	    					fout.write(c); 
	    				} 
	    				zin.closeEntry(); 
	    				fout.close(); 
					} catch (Exception e) {
						// ok for now.
						Log.v("Decompress", "Error: " + e.getMessage()); 
					}
    			}
    		} 
    		zin.close(); 
    	} catch(Exception e) { 
    		Log.e("Decompress", "unzip", e); 
    	} 
    } 
     
    private static void dirChecker(String destinationPath) { 
    	File f = new File(destinationPath); 
    	if(!f.isDirectory()) { 
    		f.mkdirs(); 
    	} 
    } 
    
    /**
     * Uses private Router.send method.
     * @param server
     * @return
     */
    private String createTestDatabase(TDServer server) {
		//to ensure this test is easily repeatable we will explicitly remove
		//any stale foo.touchdb
		TDDatabase db = server.getExistingDatabaseNamed("coconut-emas");
		if(db != null) {
			db.deleteDatabase();
		}
		//URL.setURLStreamHandlerFactory(new TDURLStreamHandlerFactory());
		TDURLStreamHandlerFactory.registerSelfIgnoreError();
		Router.send(server, "PUT", "/coconut-emas", TDStatus.CREATED, null);
		//Map<String,Object> dbInfo = (Map<String,Object>)Router.send(server, "GET", "/coconut", TDStatus.OK, null);
		// PUT:
		Map<String,Object> doc1 = new HashMap<String,Object>();
		doc1.put("message", "hello");
		Map<String,Object> result = (Map<String,Object>)Router.sendBody(server, "PUT", "/coconut-emas/doc1", doc1, TDStatus.CREATED, null);
		//String revID = (String)result.get("rev");
		//TDBlobStore attachments = db.getAttachments();
		byte[] attach1 = "This is the body of attach1".getBytes();
		String base64 = Base64.encodeBytes(attach1);
		Map<String,Object> attachment = new HashMap<String,Object>();
		attachment.put("content_type", "text/plain");
		attachment.put("data", base64);
		Map<String,Object> attachmentDict = new HashMap<String,Object>();
		attachmentDict.put("attach", attachment);
		Map<String,Object> properties = new HashMap<String,Object>();
		properties.put("foo", 1);
		properties.put("bar", false);
		properties.put("_attachments", attachmentDict);

		/*TDStatus status = new TDStatus();
		TDRevision rev1 = db.putRevision(new TDRevision(properties), null, false, status);*/
		String couchAppUrl = "coconut-emas/doc1/";
		return couchAppUrl;
	}
    
}
