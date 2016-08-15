import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class ImportFileAction extends AnAction
{
    private static final String LOGIN_URL = "http://localhost:8080/ace/security/token";
    private static final String IMPORT_URL = "http://localhost:8080/ace/import";
    private static final String CREDENTIALS = "{\"username\":\"admin\",\"password\":\"123456\"}";
    private static final String X_AUTH_TOKEN = "X-Auth-Token";

    @Override
    public void actionPerformed(AnActionEvent event)
    {
        VirtualFile file = VIRTUAL_FILE.getData(event.getDataContext());
        if (file == null) {
            return;
        }
        try {
            Client client = ClientBuilder.newClient();
            client.register(JacksonJsonProvider.class);

            String token = login(client);
            post(client, token, file.contentsToByteArray());

            displayMessage(event, "Imported: " + file.getCanonicalPath(), MessageType.INFO);
        } catch (Exception e) {
            displayMessage(event, "Import failed: " + file.getCanonicalPath(), MessageType.ERROR);
            throw new RuntimeException(e);
        }
    }

    private void post(Client client, String token, byte[] data)
    {
        Response response =
            client.target(IMPORT_URL)
                  .request()
                  .header(X_AUTH_TOKEN, token)
                  .post(Entity.entity(data, APPLICATION_JSON));
        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            throw new RuntimeException("Import failed! " + response.readEntity(String.class));
        }
    }

    private String login(Client client)
    {
        return client.target(LOGIN_URL)
                     .request(MediaType.APPLICATION_JSON)
                     .post(Entity.json(CREDENTIALS))
                     .readEntity(new GenericType<Map<String, String>>() {})
                     .get("token");
    }

    private void displayMessage(AnActionEvent event, String message, MessageType info)
    {
        Project project = DataKeys.PROJECT.getData(event.getDataContext());
        StatusBar statusBar =
            WindowManager.getInstance()
                         .getStatusBar(project);
        JBPopupFactory.getInstance()
                      .createHtmlTextBalloonBuilder(message, info, null)
                      .setFadeoutTime(7500)
                      .createBalloon()
                      .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
    }
}
