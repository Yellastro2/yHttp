package com.yellastrodev.yhttpreq;
import android.content.ContentResolver;
import java.io.File;

public interface yMain
{

	public File getExternalCacheDir();


	public ContentResolver getContentResolver();


	public void runOnUiThread(Runnable run);


	public Runnable dialProgress();


	public String getToken();


	public void onError(String fMsg);

    
    
    
}
