import com.azeesoft.libs.httprequester.core.AZHTTPRequester;
import com.azeesoft.libs.httprequester.core.tools.QT;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;

/**
 * Created by azizt on 8/25/2017.
 */
public class Tester {

    final static String BASE_URL = "http://azee.me/test/php/";

    public static void main(String[] args) {
        AZHTTPRequester azhttpRequester = new AZHTTPRequester(BASE_URL + "echo.php");
        azhttpRequester.addParam("msg", "Hola!");
        azhttpRequester.addOnResultListener(new AZHTTPRequester.OnResultListener() {
            @Override
            public void onResult(AZHTTPRequester azhttpRequester, JSONObject jobj) {
                QT.PL(jobj.toString());
            }
        });
        azhttpRequester.addOnErrorListener(new AZHTTPRequester.OnErrorListener() {
            @Override
            public void onError(AZHTTPRequester azhttpRequester, String errMsg) {
                QT.PL("Error!");
            }
        });

        try {
            File file = new File(Tester.class.getResource("Azee.jpg").toURI());
            azhttpRequester.addFile("image", file);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        /*azhttpRequester.setUiUpdater(new AZHTTPRequester.UiUpdater() {
            @Override
            public void runOnUiThread(Runnable runnable) {
                runnable.run();
            }
        });*/
        azhttpRequester.sendRequest();
    }
}
