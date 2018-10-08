package transport.foxman.com.vn.geturl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final Pattern urlPattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private WebView webView;

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        Matcher urlMatcher = urlPattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(1),
                urlMatcher.end(0)));
        }

        return containedUrls;
    }

    private static String getStringFromUrl(String stringUrl) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            URL url = new URL(stringUrl);

            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
                System.out.println(line);
            }
            in.close();

        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        return stringBuffer.toString();
    }

    private static List<String> getm3u8Link(String data) {
        List<String> allUrl = extractUrls(data);
        List<String> m3u8Url = new ArrayList<>();
        for (String url : allUrl) {
            Uri uri = Uri.parse(url);
            String lastPathSegment = uri.getLastPathSegment();
            Log.d("Hung", "Url: " + url + " --- lastPath = " + lastPathSegment);
            if (lastPathSegment != null && lastPathSegment.toLowerCase()
                .endsWith(".m3u8") && !m3u8Url.contains(url)) {
                m3u8Url.add(url);
            }
        }
        return m3u8Url;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(this), "HtmlViewer");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webview, String url) {
                super.onPageFinished(webview, url);
                webview.loadUrl("javascript:window.HtmlViewer.showHTML" +
                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });
        webView.loadUrl("http://xoac.tv/lives/5bb84e3c39137d1edb49113a");
        new FetchUrlAsyncTask().execute("http://xoac.tv/lives/5bb84e3c39137d1edb49113a");
//        new FetchUrlAsyncTask().execute("https://assia.tv/live/mix-sport4/?lang=nl");
//        new FetchUrlAsyncTask().execute("https://www.lineduball.com/watch.php?ID=12635");
//        new FetchUrlAsyncTask().execute("https://adfhd.live/watch.php?ID=12635");
//        new FetchUrlAsyncTask().execute("https://www.zingsanam.com/live.php?couple=15399");
    }

    class MyJavaScriptInterface {

        private Context ctx;

        MyJavaScriptInterface(Context ctx) {
            this.ctx = ctx;
        }

        @JavascriptInterface
        public void showHTML(String html) {
            List<String> strings = getm3u8Link(html);
            for (String string : strings) {
                Log.d("Hung", "m3u8 url: " + string);
            }
        }

    }

    private class FetchUrlAsyncTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... strings) {
            String data = getStringFromUrl(strings[0]);
            return getm3u8Link(data);
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            for (String string : strings) {
                Log.d("Hung", "m3u8 url: " + string);
            }
        }
    }
}
