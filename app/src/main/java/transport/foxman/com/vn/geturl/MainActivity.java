package transport.foxman.com.vn.geturl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GetM3u8UrlHelper.OnGetLinkListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewGroup viewGroup = findViewById(R.id.container);
        GetM3u8UrlHelper getM3u8UrlHelper = new GetM3u8UrlHelper(this, this);
        getM3u8UrlHelper.getm3u8Url("http://xoac.tv/lives/5bc22494fe6d244495a6ae77");
//        new FetchUrlAsyncTask().execute("https://adfhd.live/watch.php?ID=12635");
//        new FetchUrlAsyncTask().execute("https://www.zingsanam.com/live.php?couple=15399");
//        new FetchUrlAsyncTask().execute("http://xoac.tv/lives/5bb84e3c39137d1edb49113a");
//        new FetchUrlAsyncTask().execute("https://assia.tv/live/mix-sport4/?lang=nl");
//        new FetchUrlAsyncTask().execute("https://www.lineduball.com/watch.php?ID=12635");
    }

    @Override
    public void onGetLinkSuccess(List<String> urls) {
        for (String url : urls) {
            Log.d("Hung", "Getlink successfull link = [" + url + "]");
        }
    }

    @Override
    public void onGetLinkFailed(String error) {
        Log.d("Hung", error);

    }
}
