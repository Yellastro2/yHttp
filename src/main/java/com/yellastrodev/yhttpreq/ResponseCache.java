package com.yellastrodev.yhttpreq;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Map;
import android.util.LruCache;

public class ResponseCache
 {
    static String sFilename = "resps",
	kItemDiv = "<;>",kFieldsDiv = "<:>";
    private static ResponseCache mCache = null;
	
	public static ResponseCache getCache(yMain fAct)
	{
		if(mCache==null)
			mCache = new ResponseCache(fAct);
		return mCache;
	}
    
	private LruCache<String, String> memoryCache;
	yMain mAct;
	
	public ResponseCache(yMain faact)
	{
		mAct = faact;

		// Use 1/8th of the available memory for this memory cache.
		final int cacheSize = 8*1024*1024*10;

		memoryCache = new LruCache<String, String>(cacheSize) {
			@Override
			protected int sizeOf(String key, String bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getBytes().length;
			}
		};
		readDiskCache();
	}
	
	public String getResponse(String fUrlKey,final yCallback fClb)
	{
		final String fRes = memoryCache.get(fUrlKey);
		if(fRes!=null)
		{
			mAct.runOnUiThread(new Runnable(){public void run(){
						fClb.onRessult(fRes);
					}});
		}
		return fRes;
	}
	
	public void setResponce(String fKey,String fRes)
	{
		memoryCache.put(fKey,fRes);
		Log.e(yHttpConst.TAG,"resp cache size :"+
		memoryCache.size());
		setDiskCache();
	}
	
	void readDiskCache()
	{
		File outputDir = mAct.getExternalCacheDir(); // context being the Activity pointer

		File fFile = new File(outputDir, sFilename);
		StringBuilder text = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(fFile));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			br.close();
			String[] fBody = text.toString().split(kItemDiv);
			for(String qItem : fBody)
			{
				String[] qParts = qItem.split(kFieldsDiv);
				memoryCache.put(qParts[0],qParts[1]);
			}
		} catch (Exception e)
		{e.printStackTrace();}
	}
	
	void setDiskCache()
	{
		File outputDir = mAct.getExternalCacheDir(); // context being the Activity pointer
		
		File fFile = new File(outputDir, sFilename);
		try
		{
			FileOutputStream outputStream = new FileOutputStream(fFile.getAbsolutePath());
		
			String fBody = "";
			Map<String, String> fMap = memoryCache.snapshot();
			for(String qKey : fMap.keySet())
			{
				if(fBody.length()>0)
					fBody += kItemDiv;
				fBody += qKey +kFieldsDiv+fMap.get(qKey);
			}
			outputStream.write(fBody.getBytes());

		
		} catch (Exception e)
		{e.printStackTrace();}
	}
}
