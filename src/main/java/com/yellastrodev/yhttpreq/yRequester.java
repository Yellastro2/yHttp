package com.yellastrodev.yhttpreq;

import android.util.Log;
import com.yellastrodev.yhttpreq.yMain;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class yRequester
{
	public static int CACHE_NO = 0,
	CACHE_REGULAR = 1, CACHE_ONLY = 2;

	private static yRequester sRequester = null;

	private yMain mMain;

	private String mUrl = "";

	private yCallback mErrorHandlr = null;

	private List<String[]> mlsHeaders = new ArrayList<>();
	
	
	public static yRequester getRequester(yMain fAct)
	{
		if(sRequester==null)
			sRequester=  new yRequester(fAct);
		return sRequester;
	}
	
	public yRequester(yMain fAct)
	{
		mMain = fAct;
	}
	
	public yRequester setUrl(String fUrl)
	{
		mUrl = fUrl;
		return this;
	}
	
	public void setHeader(String fKey,String fVal)
	{
		mlsHeaders.add(new String[]{fKey,fVal});
	}
	
	public void setErrorHandler(yCallback call) {
		mErrorHandlr = call;
	}
	
	public yRequest getRequest()
	{
		return new yRequest();
	}
	
	public class yRequest
	{

		String mBody = null;
		private String mRout = "";
		private String mMeth = "GET";
		private List<String[]> mlsReqHeaders = new ArrayList<>();

		private yCallback mCallback = new yCallback(){
			@Override
			public void error(String fMsg)
			{
				mMain.onError(fMsg);
			}

			@Override
			public void call(String fRes)
			{
			}
		};

		private JSONObject mParam;

		private boolean isOnlyCache;

		private int mCacheMode;

		private String mSingleUrl = "";
		
		public yRequest()
		{
			mCacheMode = CACHE_REGULAR;
		}

		public yRequest setUrl(String fUrl) {
			mSingleUrl = fUrl;
			return this;
		}
		
		public yRequest setRout(String fRout)
		{
			mRout = fRout;
			return this;
		}
		
		public yRequest setHeader(String fKey,String fVal)
		{
			mlsReqHeaders.add(new String[]{fKey,fVal});
			return this;
		}
		
		public yRequest setMeth(String fMeth)
		{
			mMeth = fMeth;
			return this;
		}
		
		public yRequest setCallback(yCallback fRout)
		{
			mCallback = fRout;
			return this;
		}
		public yRequest setParam(JSONObject fRout)
		{
			mParam = fRout;
			return this;
		}
		
		public yRequest setCacheMode(int fMode)
		{
			if(fMode == CACHE_ONLY)
				isOnlyCache = true;
			mCacheMode = fMode;
			return this;
		}
		
		public yRequest setBody(String fB)
		{
			mBody = fB;
			return this;
		}
		
		public void run()
		{
			if(mSingleUrl.isEmpty())
				mSingleUrl = mUrl;
			if(mSingleUrl.isEmpty())
			{
				mMain.onError("Pls configure url");
				return;
			}
			sendReq(mSingleUrl,mMain,mRout,mCallback,mParam,mMeth,
			mMain.getToken(),isOnlyCache);
		}
		
		
		public  void sendReq(final String mUrl,final yMain fCxt,
							 final String fRout,final yCallback fCallback,
							 final JSONObject fParam,final String fMeth,
							 final String fToken,final boolean isCache)
		{
			new Thread(){public void run(){

					final Runnable fOnEnd = fCxt.dialProgress();

					String fUrl = "";
					fUrl = mUrl;
					fUrl = fUrl
						+ fRout;
					try {
						String fQueryParam = "";
						if(fParam!=null)
						{

							fQueryParam = getQuery(fParam);

							//fParam.toString();
							//if(fMeth.equals("GET"))
							{
								Log.e(yHttpConst.TAG,"request:"+fQueryParam);
								fUrl +="?"+fQueryParam;
							}
						}


						URL url = new URL(fUrl);

						//URL url = new URL("http","127.0.0.1",3000,fRout+"?"+fQueryParam);
						Log.e(yHttpConst.TAG,"open connection: "+ url.toString());
						HttpURLConnection urlConnection 
							= (HttpURLConnection) url.openConnection();
						Log.e(yHttpConst.TAG,"conn: "+ urlConnection.getURL());
						if(fToken!=null&&!fToken.isEmpty())
							urlConnection.setRequestProperty("X-API-Key",fToken);
						for(String[] qHeader : mlsHeaders){
							urlConnection.setRequestProperty(qHeader[0],qHeader[1]);
						}
						for(String[] qHeader : mlsReqHeaders){
							urlConnection.setRequestProperty(qHeader[0],qHeader[1]);
						}
						//urlConnection.setRequestProperty("content-type", "application/json");
						urlConnection.setConnectTimeout(10000);  
						urlConnection.setReadTimeout(10000);
						urlConnection.setDoInput(true);
						urlConnection.setRequestMethod("GET");
						urlConnection.setUseCaches(true);

						String fReqHeads = 
							urlConnection.getRequestProperties().toString();
						Log.e(yHttpConst.TAG,"request Head: \n"+fReqHeads);

						
						if(mCacheMode!=CACHE_NO)
						{
							Log.e(yHttpConst.TAG,"try to cache http");
							String fRespCache = ResponseCache.getCache(fCxt)
								.getResponse(fUrl, fCallback);

							if(fRespCache!=null)
							{
								Log.e(yHttpConst.TAG,"get some cache");
								if(mCacheMode == CACHE_ONLY)
								{
									//fOnEnd.run();
									return;
								}
							}
							else
								Log.e(yHttpConst.TAG,"cache not found");
							
						}
    					
						try {

							if(fMeth.equals("POST")||fMeth.equals("PATCH")||
								   fMeth.equals("PUT"))
								{
									urlConnection.setRequestMethod(fMeth);
									urlConnection.setDoOutput(true);
									DataOutputStream localDataOutputStream 
										= new DataOutputStream(urlConnection.getOutputStream());
									BufferedWriter outputStream = new BufferedWriter(
										new OutputStreamWriter(localDataOutputStream, "UTF-8"));
									/*if(fParam!=null)
									{
										String fPostParam = URLDecoder.decode(fParam.toString(),"UTF-8");
										
										Log.e(yHttpConst.TAG,"request:"+fPostParam);
									
										outputStream.write(fPostParam);
									
									
									/*localDataOutputStream.writeBytes(fPostParam);
									 localDataOutputStream.flush();
									 localDataOutputStream.close();*/
								//	}else{
									
										if(mBody!=null)
											outputStream.write(mBody);
									//}
									outputStream.flush();
									outputStream.close();

							}else
								urlConnection.connect();
							int fCode = urlConnection.getResponseCode();
							String fHeaders = 
								urlConnection.getHeaderFields().toString();
							//Log.e(yHttpConst.TAG,"responce Head: \n"+fHeaders);
							if(fOnEnd!=null)
								fOnEnd.run();
							if(fCode==200)
							{
								successResp(fCxt,urlConnection,fCallback);
							}else
							{
								String fMsg = 
									urlConnection.getResponseMessage();


								BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "UTF8"));
								final String fResult = fMsg +"\n"+ readStream(in);
								int dgcn=4;

								Log.e(yHttpConst.TAG,"message: " + fResult);
								if (fCode == 403) 
								{

									fCxt.runOnUiThread(new Runnable(){public void run(){
												if(mErrorHandlr!=null)
												{
													mErrorHandlr.error("403/n"+fResult);
												}
												fCallback.error("403/n"+fResult);
											}});
								}else
								{
									if(fResult==null)
										fCxt.runOnUiThread(new Runnable(){public void run(){
													if(mErrorHandlr!=null)
													{
														mErrorHandlr.error("404");
													}
													fCallback.error("какаято");}});
									else
										fCxt.runOnUiThread(new Runnable(){public void run(){
													fCallback.error(fResult);}});
								}
							}


						} finally {
							urlConnection.disconnect();
						}
					} catch (final Exception e) {

						fCxt.runOnUiThread(new Runnable(){public void run(){
									if(mErrorHandlr!=null)
									{
										mErrorHandlr.error(e.toString());
									}
									fCallback.error(e.toString());}});
						e.printStackTrace();
						if(fOnEnd!=null)
							fOnEnd.run();
					}
				}}.start();
		}
	}
	
				
							

	
	
	static void successResp(yMain fCxt,URLConnection fCon,final yCallback fCallback) throws IOException
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(fCon.getInputStream(), "UTF8"));
		final String fResult = readStream(in);

		ResponseCache.getCache(fCxt).setResponce(
		fCon.getURL().toString(),fResult);
		Log.e(yHttpConst.TAG,"success for "+fCon.getURL().toString());
		fCxt.runOnUiThread(new Runnable(){public void run(){
					fCallback.onRessult(fResult);
				}});
	}
	
	/*
	public static void sendFile(final yMain fCxt,
							  final yCallback fCallback,final Uri fFile,
							   final String fToken)
	{
		new Thread(){public void run(){

				final Runnable fOnEnd = fCxt.dialProgress();
				
				String fUrl = "";

				fUrl = yHttpConst.MAINURL;
				fUrl = fUrl //+ "/saveFile";
					+ yHttpConst.MET_UPLFILE;
				try {
					String fQueryParam = "";
					
					URL url = new URL(fUrl);

					//URL url = new URL("http","127.0.0.1",3000,fRout+"?"+fQueryParam);
					Log.e(yHttpConst.TAG,"open connection: "+ url.toString());
					HttpURLConnection urlConnection 
						= (HttpURLConnection) url.openConnection();
					if(!fToken.isEmpty())
					    urlConnection.setRequestProperty("X-API-Key",fToken);
						
					Bitmap bitmap = MediaStore.Images.Media
						.getBitmap(fCxt.getContentResolver(), fFile);

					urlConnection.setDoOutput(true);
					
					
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					/* example for setting a HttpMultipartMode */
					//builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

					/* example for adding an image part */
					/*String filename = "filename.jpg";
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
					String fByteStr2 = bos.toString();
					byte[] fArray = //Base64.encodeBase64(
					bos.toByteArray();
					String fByteStr = android.util.Base64.encodeToString(fArray, android.util.Base64.DEFAULT);
					ContentBody contentPart = new ByteArrayBody(fArray,"image/jpg", filename);
						//new StringBody(fByteStr,"image/jpeg",Charset.forName("UTF-8"));
					//image should be a String
					
					builder.addPart("file", contentPart);
					HttpEntity reqEntity = builder.build();
					//urlConnection.setRequestProperty("content-type", "application/json");
					urlConnection.setRequestProperty("Connection", "Keep-Alive");
					urlConnection.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
					urlConnection.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());
					
					urlConnection.setConnectTimeout(100000);  
					urlConnection.setReadTimeout(100000);
					urlConnection.setDoInput(true);
					urlConnection.setRequestMethod("POST");
					urlConnection.setDoOutput(true);

					 
					String fReqHeads = 
						urlConnection.getRequestProperties().toString();
					Log.e(yHttpConst.TAG,"request Head: \n"+fReqHeads);

					try {
						DataOutputStream localDataOutputStream 
							= new DataOutputStream(urlConnection.getOutputStream());
						reqEntity.writeTo(urlConnection.getOutputStream());
								//bitmap.compress(Bitmap.CompressFormat.JPEG, 100, localDataOutputStream);
								localDataOutputStream.flush();
								localDataOutputStream.close();
								/*localDataOutputStream.writeBytes(fPostParam);
								 localDataOutputStream.flush();
								 localDataOutputStream.close();*/
						/*int fCode = urlConnection.getResponseCode();
						String fHeaders = 
							urlConnection.getHeaderFields().toString();
						Log.e(yHttpConst.TAG,"responce Head: \n"+fHeaders);
						fOnEnd.run();
						if(fCode==200)
						{
							BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF8"));
							final String fResult = readStream(in);
							
						    fCxt.runOnUiThread(new Runnable(){public void run(){
										fCallback.call(fResult);
									}});
						}else
						{
							String fMsg = 
								urlConnection.getResponseMessage();


							BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getErrorStream(), "UTF8"));
							final String fResult = fMsg +"\n"+ readStream(in);
							int dgcn=4;

							Log.e(yHttpConst.TAG,"message: " + fResult);
							if (fCode == 403) 
							{

								fCxt.runOnUiThread(new Runnable(){public void run(){
											fCallback.error("403/n"+fResult);
										}});
							}else
							{
								if(fResult==null)
									fCxt.runOnUiThread(new Runnable(){public void run(){
												fCallback.error("какаято");}});
								else
									fCxt.runOnUiThread(new Runnable(){public void run(){
												fCallback.error(fResult);}});;
							}
						}


					} finally {
						urlConnection.disconnect();
					}
				} catch (final Exception e) {

					fCxt.runOnUiThread(new Runnable(){public void run(){
								fCallback.error(e.toString());}});
					e.printStackTrace();
					fOnEnd.run();
				}
			}}.start();
	}*/

	public static String getQuery(JSONObject params) throws UnsupportedEncodingException, JSONException
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		Iterator<String> fIt = params.keys();
		while (fIt.hasNext())
		{
			if (first)
				first = false;
			else
				result.append("&");
			String qKey = fIt.next();
			result.append(URLEncoder.encode(qKey, "UTF-8"));
			result.append("=");
			
			String qParam = params.getString(qKey);
			if (qParam.contains("@"))
				result.append(qParam);
			else
			{
				if(qKey.equals("specialistids"))
				{
					qParam = 
					qParam.replace("[","")
					.replace("]","");
				}
				
				result.append(URLDecoder.decode(qParam, "UTF-8"));
			}
				
		}

		return result.toString();
	}

	public static String readStream(BufferedReader fIn)
	{
		int ch;
		StringBuilder sb = new StringBuilder();
		try {
			while ((ch = fIn.read()) != -1)
				sb.append((char)ch);
		} catch (IOException e) {e.printStackTrace();
		}

		return sb.toString();
	}
}
