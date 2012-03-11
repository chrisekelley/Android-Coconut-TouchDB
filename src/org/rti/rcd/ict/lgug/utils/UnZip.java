package org.rti.rcd.ict.lgug.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * kudos: http://stackoverflow.com/a/5028521
 *
 */
public class UnZip implements Runnable {

    File archive;
    String outputDir;
    static Handler myHandler;

    public UnZip(Handler handler, File ziparchive, String directory) {
    	myHandler = handler;
    	archive = ziparchive;
    	outputDir = directory;
    }

    public void log(String log) {
            Log.v("unzip", log);
    }

    @SuppressWarnings("unchecked")
    public void run() {
    	Message msg;
    	try {
    		ZipFile zipfile = new ZipFile(archive);
    		for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements();) {
    			ZipEntry entry = (ZipEntry) e.nextElement();
    			msg = new Message();
    			msg.what = 0;
    			msg.obj = "Extracting " + entry.getName();
    			myHandler.sendMessage(msg);
    			unzipEntry(zipfile, entry, outputDir);
    		}
    	} catch (Exception e) {
    		log("Error while extracting file " + archive);
    	}
    	msg = new Message();
    	msg.obj = "Extracting " + archive;
    	msg.what = 1;
    	myHandler.sendMessage(msg);
    }

    @SuppressWarnings("unchecked")
    public void unzipArchive(File archive, String outputDir) {
            try {
                    ZipFile zipfile = new ZipFile(archive);
                    for (Enumeration e = zipfile.entries(); 
e.hasMoreElements();) {
                            ZipEntry entry = (ZipEntry) e.nextElement();
                            unzipEntry(zipfile, entry, outputDir);
                    }
            } catch (Exception e) {
                    log("Error while extracting file " + archive);
            }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry,
    		String outputDir) throws IOException {

    	if (entry.isDirectory()) {
    		createDir(new File(outputDir, entry.getName()));
    		return;
    	}

    	File outputFile = new File(outputDir, entry.getName());
    	if (!outputFile.getParentFile().exists()) {
    		createDir(outputFile.getParentFile());
    	}

    	log("Extracting: " + entry);
    	BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
    	BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

    	try {
    		IOUtils.copy(inputStream, outputStream);
    	} finally {
    		outputStream.close();
    		inputStream.close();
    	}
    }

    private void createDir(File dir) {
            log("Creating dir " + dir.getName());
            if (!dir.mkdirs())
                    throw new RuntimeException("Can not create dir " + dir);
    }
}
