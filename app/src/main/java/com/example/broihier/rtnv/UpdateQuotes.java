package com.example.broihier.rtnv;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

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
	private String ticker;
	private String result = null;

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
			Document doc = Jsoup.connect("https://finance.yahoo.com/quote/" + ticker).userAgent("Chrome for Android").get();
			Elements elements = doc.getElementsByTag("span");
            Pattern previousClose = Pattern.compile("Previous Close");
            Pattern valuePattern = Pattern.compile("(\\d+\\.?\\d*)");
            String value = "";
            boolean previousCloseFound = false;
            for (Element e : elements) {
                Matcher label = previousClose.matcher(e.html());
                if (label.find()) {
					Log.d("rtnv", "previous close pattern was found");
					previousCloseFound = true;
//				} else {
//					Log.d("rtnv", e.toString());
				}
				Matcher valueMatcher = valuePattern.matcher(e.html());
				if (previousCloseFound) {
					if (valueMatcher.find()) {
						int count = valueMatcher.groupCount();
						Log.d("rtnv", "value pattern found, count: " + count);
						if (valueMatcher.groupCount() != 1) {
							Log.e("rtnv", "Warning - expecting a count of 1, got: " + count);
						}
						value = valueMatcher.group(1);
						break;
					} else {
						Log.d("rtnv", e.toString());
					}
                }
            }
            Log.d("rtnv", "value found with jsoup: " + value);
            if (value.equals("")) {
                result = "Not found";
            } else {
                result = value;
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

