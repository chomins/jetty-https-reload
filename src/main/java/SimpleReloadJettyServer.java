import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class MyHandler extends AbstractHandler {

    @Override
    public void handle(String target, Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/plain;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        PrintWriter out = response.getWriter();

        for (Enumeration<String> e = baseRequest.getParameterNames();
             e.hasMoreElements();) {
            String name = e.nextElement();
            out.format("server:  your %s -> %s%n", name, baseRequest.getParameter(name));
        }
    }
}

public class SimpleReloadJettyServer {
    private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);
    static Server jettyServer = new Server();
    static SimpleReloadJettyClient client = new SimpleReloadJettyClient();

    public static void start() throws Exception {

        HttpConfiguration config = new HttpConfiguration();
        config.addCustomizer(new SecureRequestCustomizer());
        config.addCustomizer(new ForwardedRequestCustomizer());

        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(config);
        ServerConnector httpConnector = new ServerConnector(jettyServer, httpConnectionFactory);
        httpConnector.setPort(8080); // IP tables redirect 80 -> 8080
        jettyServer.addConnector(httpConnector);

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath("src/main/resources/server");
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");
        ServerConnector httpsConnector = new ServerConnector(jettyServer, sslContextFactory, new HttpConnectionFactory(config));
        httpsConnector.setPort(8443); // IP tables redirect 8443 -> 443
        jettyServer.addConnector(httpsConnector);

        FileWatcher.onFileChange(Paths.get(URI.create(sslContextFactory.getKeyStorePath())), () ->
                sslContextFactory.reload(scf -> {
                    try {
                        SimpleReloadJettyClient.req_res();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                    }
                }));

        MyHandler myHandler = new MyHandler();
        jettyServer.setHandler(myHandler);
        jettyServer.start();
    }

    public void stop () throws Exception {
        jettyServer.stop();
    }

}
