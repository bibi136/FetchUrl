package transport.foxman.com.vn.geturl;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FetchUrlAsyncTask asyncTask = new FetchUrlAsyncTask();
        asyncTask.execute("http://assia.tv/live/mix-sport4/?lang=nl");

    }

    private class FetchUrlAsyncTask extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... strings) {
            String data = getStringFromUrl(strings[0]);
            List<String> allUrl = extractUrls(data);
            List<String> m3u8Url = new ArrayList<>();
            for (String url : allUrl) {
                Uri uri = Uri.parse(url);
                String lastPathSegment = uri.getLastPathSegment();
                Log.d("Hung", "Url: " + url + " --- lastPath = " + lastPathSegment);
                if (lastPathSegment != null && lastPathSegment.toLowerCase().endsWith(".m3u8")) {
                    m3u8Url.add(url);
                }
            }
            return m3u8Url;
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
