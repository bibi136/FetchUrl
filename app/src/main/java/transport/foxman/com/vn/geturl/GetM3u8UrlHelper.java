package transport.foxman.com.vn.geturl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetM3u8UrlHelper {

    private static final Pattern URL_PATTERN = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
            + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
            + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    private static final String FAKE_CHROME_USERAGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X " +
        "10_13_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.77 Safari/537.36";

    private WebView webView;

    private OnGetLinkListener listener;

    private ViewGroup parent;

    public GetM3u8UrlHelper(Context context, OnGetLinkListener linkListener) {
        listener = linkListener;
        this.parent = new LinearLayout(context);
        initWebview(context);
    }

    private static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<>();
        Matcher urlMatcher = URL_PATTERN.matcher(text);

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

        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }

    private static List<String> getm3u8Link(String data) {
        List<String> allUrl = extractUrls(data);
        List<String> m3u8Url = new ArrayList<>();
        for (String url : allUrl) {
            Uri uri = Uri.parse(url);
            String lastPathSegment = uri.getLastPathSegment();
            if (lastPathSegment != null && lastPathSegment.toLowerCase()
                .endsWith(".m3u8") && !m3u8Url.contains(url)) {
                m3u8Url.add(url);
            }
        }
        return m3u8Url;
    }

    public void getm3u8Url(String url) {
        new FetchUrlAsyncTask().execute(url);
    }

    private void loadUrlUsingWebview(String url) {
        Log.d("Hung", "loadUrlUsingWebview: Load using webview");
        webView.loadUrl(url);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebview(Context context) {
        webView = new WebView(context);
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setUserAgentString(FAKE_CHROME_USERAGENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HtmlViewer");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webview, String url) {
                super.onPageFinished(webview, url);
                Log.d("Hung", "onPageFinished: ");
                webview.loadUrl("javascript:window.HtmlViewer.showHTML" +
                    "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });
        parent.addView(webView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public interface OnGetLinkListener {

        void onGetLinkSuccess(List<String> urls);

        void onGetLinkFailed(String error);
    }

    class FetchUrlAsyncTask extends AsyncTask<String, Void, List<String>> {

        private String requestUrl;

        @Override
        protected List<String> doInBackground(String... strings) {
            requestUrl = strings[0];
            String data = getStringFromUrl(requestUrl);
            return getm3u8Link(data);
        }

        @Override
        protected void onPostExecute(List<String> m3u8Url) {
            super.onPostExecute(m3u8Url);
            if (!m3u8Url.isEmpty()) {
                // Add all url if result not empty
                if (listener != null) listener.onGetLinkSuccess(m3u8Url);
            } else {
                // try to using webview
                loadUrlUsingWebview(requestUrl);
            }
        }
    }

    class MyJavaScriptInterface {

        @JavascriptInterface
        public void showHTML(String html) {
            List<String> links = getm3u8Link(html);
            if (!links.isEmpty()) {
                listener.onGetLinkSuccess(links);
            } else {
                listener.onGetLinkFailed("Khong lay duoc link nao");
            }
        }
    }
}
