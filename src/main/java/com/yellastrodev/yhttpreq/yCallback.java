package com.yellastrodev.yhttpreq;

public abstract class yCallback {

	private boolean isCalled = false;

	private String mLastRes = "";

	public abstract void error(String fMsg);

    protected abstract void call(String fRes);
	public void onRessult(String fRes)
	{
		if(isCalled && fRes.length()==mLastRes.length())
			return;
		isCalled = true;
		mLastRes = fRes;
		call(fRes);
	}
}
