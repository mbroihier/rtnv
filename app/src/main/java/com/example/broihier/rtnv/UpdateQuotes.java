package com.example.broihier.rtnv;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateQuotes {
    /* UpdateQuotes class                                                                                            */
    /* ============================================================================================================= */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Description:                                                                                                  */
    /*     This class reads stock quotes from the Yahoo finance web site and gets the closing value from the stream  */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ============================================================================================================= */
	String ticker;
	URL getRequest = null;
	String result = null;

	public void setTicker(String _ticker) {
	/* setTicker - method that sets the ticker that is to be read from the web                                       */
    /* ============================================================================================================= */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Description:                                                                                                  */
    /*     This method sets the ticker that is to be read from Yahoo and builds the HTTP request to do the GET.      */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Inputs:                                                                                                       */
    /*     Mnemonic      Parameter                      Source                                                       */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*    _ticker        ticker                         calling object                                               */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Processing:                                                                                                   */
    /*   set ticker instance object;                                                                                 */
    /*   create HTTP get request;                                                                                    */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Outputs:                                                                                                      */
    /*     Mnemonic      Parameter                      Destination                                                  */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*    ticker         internal ticker value          this object                                                  */
    /*    getRequest     internal get request object    this object                                                  */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ============================================================================================================= */
		ticker = _ticker;
	}

	public void get() {
	/* get - method that does the get request                                                                        */
    /* ============================================================================================================= */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Description:                                                                                                  */
    /*     This method does the HTTP GET request.                                                                    */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Inputs:                                                                                                       */
    /*     Mnemonic      Parameter                      Source                                                       */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Processing:                                                                                                   */
    /*   set HTTP get request;                                                                                       */
    /*   execute request;                                                                                            */
    /*   read result and extract previous closing price;                                                             */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Outputs:                                                                                                      */
    /*     Mnemonic      Parameter                      Destination                                                  */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*                   HTTP get request               Yahoo                                                        */
    /*    result         closing value                  this object                                                  */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ============================================================================================================= */
		try {
			Log.d("rtnv", "in try");
			getRequest = new URL("http://finance.yahoo.com/quote/" + ticker);
			URLConnection con = getRequest.openConnection();
			InputStream inputStream = con.getInputStream();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			result = "";
			String line;
			int lastSize = 0;
			do {
				while ((line = bufferedReader.readLine()) != null) {
					result += line;
				}
				if (result.length() > lastSize) {
					Log.d("rtnv", "Buffer current size: " + result.length());
					lastSize = result.length();
				}
			} while (!result.contains("/html>"));
			Pattern p = Pattern.compile("previousClose\": *([0-9]+(.[0-9]+){0,1}),");
			Matcher m = p.matcher(result);
			if (m.find()) {
				result = m.group(1);
			} else {
				Log.d("rtnv", "previousClose not found: " + result);
				Pattern pExist = Pattern.compile("symbol (.*) doesn't exist");
				Matcher mExist = pExist.matcher(result);
				if (mExist.find()) {
					Log.d("rtnv", "Symbol: " + mExist.group(1) + " was not found");
				} else {
					Pattern pOther = Pattern.compile("PREV_CLOSE-value[^>]+> *([0-9]+.[0-9]+)");
					Matcher mOther = pOther.matcher(result);
					if (mOther.find()) {
						result = mOther.group(1);
					} else {
						Log.d("rtnv", "Search for PREV_CLOSE-value failed too");
						result = "Not found";
					}
				}
			}
		} catch (Exception e) {
			Log.d("UpdateQuotes", "Got an exception when attempting to get a quote");
			e.printStackTrace();
			result = "Got exception";
		}
	}

	public String getResult() {
	/* getResult - getter to return last closing price                                                               */
    /* ============================================================================================================= */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Description:                                                                                                  */
    /*     This method returns the result of the Yahoo read.                                                         */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Inputs:                                                                                                       */
    /*     Mnemonic      Parameter                      Source                                                       */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Processing:                                                                                                   */
    /*   return price;                                                                                               */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ------------------------------------------------------------------------------------------------------------- */
    /*                                                                                                               */
    /* Outputs:                                                                                                      */
    /*     Mnemonic      Parameter                      Destination                                                  */
    /*   ___________    ___________                     ____________________________________________________________ */
    /*                   closing value                  calling object                                               */
    /*                                                                                                               */
    /* ------------------------------------------------------------------------------------------------------------- */
    /* ============================================================================================================= */
		return result;
	}
}
