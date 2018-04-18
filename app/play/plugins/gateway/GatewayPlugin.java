package play.plugins.gateway;

import helpers.gateway.Gateway;
import play.Play;
import play.PlayPlugin;
import play.mvc.Http;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jtremeaux
 */
public class GatewayPlugin extends PlayPlugin {
    private Map<String, String> routeMap;

    @Override
    public void onConfigurationRead() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        routeMap = new HashMap<>();
        String fileName = "conf/routes-gateway";
        File routeFile = Play.getFile(fileName);
        if (!routeFile.exists()) {
            throw new RuntimeException("Please create the route file " + fileName);
        }
        parse(routeFile);
    }

    private void parse(File routeFile) {
        String content;
        try {
            content = new String(Files.readAllBytes(routeFile.toPath()));
        } catch (IOException e) {
            throw new RuntimeException("Error reading route file: " + routeFile, e);
        }
        for (String line : content.split("\n")) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] route = line.split("\\s+");
            if (route.length == 2) {
                routeMap.put(route[0], route[1]);
            }
        }
    }

    @Override
    public boolean rawInvocation(Http.Request request, Http.Response response) throws Exception {
        if (routeMap == null) {
            loadConfiguration();
        }
        for (Map.Entry<String, String> route : routeMap.entrySet()) {
            if (request.url.startsWith(route.getKey())) {
                try {
                    proxyRoute(request, response, route.getKey(), route.getValue());
                } catch (Exception e) {
                    throw new RuntimeException("Error proxying request", e);
                }
                return true;
            }
        }
        return super.rawInvocation(request, response);
    }

    private static void proxyRoute(Http.Request request, Http.Response response, String key, String value) {
        String newUrl = value + request.url.substring(key.length());
        Gateway gateway = new Gateway.Builder().build();
        gateway.proxy(request, response, newUrl);
    }

}
