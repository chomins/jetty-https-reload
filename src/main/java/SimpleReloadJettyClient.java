import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SimpleReloadJettyClient {
    private static HttpClient client;

    private static void startClient() throws Exception {

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustStoreResource(Resource.newClassPathResource("trust"));

        client = new HttpClient(sslContextFactory);
        client.start();

        req_res();
    }

    public static void req_res() throws InterruptedException, ExecutionException, TimeoutException {
        Fields.Field name = new Fields.Field("Name", "Test");
        Fields.Field age = new Fields.Field("Age", "27");
        Fields fields = new Fields();
        fields.put(name);
        fields.put(age);

        ContentResponse res = client.FORM("https://localhost:8443", fields);
        System.out.println(res.getContentAsString());
    }

    private void stopClient() throws Exception {
        client.stop();
    }

    public static void main(String[] args) throws Exception {

        SimpleReloadJettyServer.start();
        SimpleReloadJettyClient.startClient();

    }

}
