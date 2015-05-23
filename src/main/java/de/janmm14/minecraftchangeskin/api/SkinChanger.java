package de.janmm14.minecraftchangeskin.api;

import de.janmm14.minecraftchangeskin.bukkit.Main;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Locale;

@SuppressWarnings("FinalClass")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SkinChanger {

	public static void changeSkin(@NonNull final SkinChangeParams params, @Nullable final Callback<Boolean> resultProcessor) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean result = false;
				Exception error = null;
				CloseableHttpClient client = HttpClients.createDefault();
				HttpHost proxy = null;
				if (!params.getProxyIp().trim().isEmpty() && params.getProxyPort() > 0) {
					proxy = new HttpHost(params.getProxyIp(), params.getProxyPort());
				}
				try {
					RequestConfig.Builder config = RequestConfig.copy(RequestConfig.DEFAULT)
						.setRedirectsEnabled(true)
						.setCircularRedirectsAllowed(false)
						.setRelativeRedirectsAllowed(true)
						.setMaxRedirects(10);
					config.setProxy(proxy);

					////////////////////////////////////////////////////////////////////////////////////////////////////
					// login page
					HttpGet loginPage = new HttpGet("https://minecraft.net/login");

					loginPage.setConfig(config.build());

					CloseableHttpResponse loginPageResponse = client.execute(loginPage);
					if (loginPageResponse.getStatusLine().getStatusCode() != 200) {
						loginPage.releaseConnection();
						throw new SkinChangeException("login page not recieved, minecraft.net down?" + "\nResult:\n" +
							"Header:\n" + Arrays.deepToString(loginPageResponse.getAllHeaders()) + "\n\n" +
							"Body:\n" + EntityUtils.toString(loginPageResponse.getEntity()));
					}
					String loginPageResponseStr = EntityUtils.toString(loginPageResponse.getEntity());
					loginPageResponse.close();
					loginPage.releaseConnection();
					// extract token
					int lp_startIndex = loginPageResponseStr.indexOf("<input type=\"hidden\" name=\"authenticityToken\" value=\"");
					String loginToken = loginPageResponseStr.substring(lp_startIndex);
					int lp_endIndex = loginToken.indexOf("\">");
					loginToken = loginToken.substring(53, lp_endIndex);

					////////////////////////////////////////////////////////////////////////////////////////////////////
					//login
					HttpPost login = new HttpPost("https://minecraft.net/login");
					login.setConfig(config.build());
					login.setEntity(MultipartEntityBuilder.create()
							.addTextBody("authenticityToken", loginToken)
							.addTextBody("username", params.getEmail())
							.addTextBody("password", params.getPassword())
							.build()
					);
					CloseableHttpResponse loginResponse = client.execute(login);
					// check success
					if (loginResponse.getStatusLine().getStatusCode() != 302) {
						loginResponse.close();
						login.releaseConnection();
						throw new SkinChangeException("login not successfull" + "\nResult:\n" +
							"Header:\n" + Arrays.deepToString(loginResponse.getAllHeaders()) + "\n\n" +
							"Body:\n" + EntityUtils.toString(loginResponse.getEntity()));
					}
					loginResponse.close();
					login.releaseConnection();

					////////////////////////////////////////////////////////////////////////////////////////////////////
					//upload page
					HttpGet uploadPage = new HttpGet("https://minecraft.net/profile");

					uploadPage.setConfig(config.setRedirectsEnabled(false).setMaxRedirects(0).build());

					CloseableHttpResponse uploadPageResponse = client.execute(uploadPage);
					// check success
					if (uploadPageResponse.getStatusLine().getStatusCode() != 200) {
						uploadPage.releaseConnection();
						throw new SkinChangeException("upload page not recieved, wrong credentials or minecraft.net down?" + "\nResult:\n" +
							"Header:\n" + Arrays.deepToString(uploadPageResponse.getAllHeaders()) + "\n\n" +
							"Body:\n" + EntityUtils.toString(uploadPageResponse.getEntity()));
					}
					String uploadPageResponseStr = EntityUtils.toString(uploadPageResponse.getEntity());
					uploadPage.releaseConnection();
					// extract token
					int up_startIndex = uploadPageResponseStr.indexOf("<input type=\"hidden\" name=\"authenticityToken\" value=\"");
					String uploadToken = uploadPageResponseStr.substring(up_startIndex);
					int up_endIndex = uploadToken.indexOf("\">");
					uploadToken = uploadToken.substring(53, up_endIndex);

					////////////////////////////////////////////////////////////////////////////////////////////////////
					//upload
					HttpPost uploadSkin = new HttpPost("https://minecraft.net/profile/skin");
					uploadSkin.setConfig(config.build());
					uploadSkin.setEntity(MultipartEntityBuilder.create()
											 .addBinaryBody("skin", params.getImage())
											 .addTextBody("authenticityToken", uploadToken)
											 .addTextBody("model", params.getSkinModel().toString())
											 .build());
					JavaPlugin.getPlugin(Main.class).getLogger().info("UPLOAD SKIN ENTITY: " + EntityUtils.toString(uploadSkin.getEntity()));
					JavaPlugin.getPlugin(Main.class).getLogger().info("UPLOAD SKIN HEADERS: " + Arrays.deepToString(uploadSkin.getAllHeaders()));


					CloseableHttpResponse uploadSkinResponse = client.execute(uploadSkin);

					// check success
					if (uploadSkinResponse.getStatusLine().getStatusCode() != 302) {
						uploadPage.releaseConnection();
						throw new SkinChangeException("could not upload skin!" + "\nResult:\n" +
							"Header:\n" + Arrays.deepToString(uploadSkinResponse.getAllHeaders()) + "\n\n" +
							"Body:\n" + EntityUtils.toString(uploadSkinResponse.getEntity()));
					}
					boolean cookie = false;
					for (Header h : uploadSkinResponse.getHeaders("Set-Cookie")) {
						if (h.getValue().trim().toLowerCase(Locale.ENGLISH).contains("success=Your+skin+has+been+changed".toLowerCase(Locale.ENGLISH))) {
							result = true;
							cookie = true;
							break;
						}
					}
					if (!cookie) {
						throw new SkinChangeException("skin upload not successfull!" + "\nResult:\n" +
														  "Header:\n" + Arrays.deepToString(uploadSkinResponse.getAllHeaders()) + "\n\n" +
														  "Body:\n" + EntityUtils.toString(uploadSkinResponse.getEntity()));
					}
				} catch (Exception e) {
					System.out.println("[MC.NET SKIN CHANGE API] Error while changing skin:");
					e.printStackTrace();
					error = e;
					result = false;
				}
				if (resultProcessor != null) {
					resultProcessor.done(result, error);
				}
			}
		}).start();
	}
}
