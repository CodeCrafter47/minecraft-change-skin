package de.janmm14.minecraftchangeskin.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("FinalClass")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SkinChanger {

	private final static ThreadLocal<ObjectMapper> objectMapper = new ThreadLocal<ObjectMapper>() {
		@Override
		protected ObjectMapper initialValue() {
			return new ObjectMapper();
		}
	};

	public static void changeSkin(@NonNull final SkinChangeParams params, @Nullable final Callback<SkinChangerResult> resultProcessor) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				SkinChangerResult result = SkinChangerResult.UNKNOWN_ERROR;
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
					// authenticate
					Object payload = new AuthenticatePayload(new SkinChanger.Agent("Minecraft"), params.getEmail(), params.getPassword());

					HttpPost authReq = new HttpPost("https://authserver.mojang.com/authenticate");
					authReq.setConfig(config.build());

					StringEntity stringEntity = new StringEntity(objectMapper.get().writeValueAsString(payload));
					authReq.setEntity(stringEntity);
					authReq.setHeader("Content-type", "application/json");
					CloseableHttpResponse authResponse = client.execute(authReq);

					if (authResponse.getStatusLine().getStatusCode() != 200) {
						throw new SkinChangeException("login page not recieved, minecraft.net down?" + "\nResult:\n" +
								"Header:\n" + Arrays.deepToString(authResponse.getAllHeaders()) + "\n\n" +
								"Body:\n" + EntityUtils.toString(authResponse.getEntity()));
					}

					AuthenticateResponse auth = objectMapper.get().readValue(EntityUtils.toString(authResponse.getEntity()), AuthenticateResponse.class);

					////////////////////////////////////////////////////////////////////////////////////////////////////
					// upload skin
					HttpPut uploadPage = new HttpPut("https://api.mojang.com/user/profile/" + auth.getSelectedProfile().getUuid() + "/skin");

					uploadPage.setConfig(config.build());
					uploadPage.setHeader("Authorization", "Bearer " + auth.getAccessToken());

					stringEntity = new StringEntity(objectMapper.get().writeValueAsString(payload));
					uploadPage.setEntity(MultipartEntityBuilder.create()
							.addBinaryBody("file", params.getImage(), ContentType.create("image/png"), params.getImage().getName())
							.addTextBody("model", params.getSkinModel().toString())
							.build()
					);
					CloseableHttpResponse skinResponse = client.execute(uploadPage);

					// check success
					if (skinResponse.getStatusLine().getStatusCode() != 204) {
						uploadPage.releaseConnection();
						String headers = Arrays.deepToString(skinResponse.getAllHeaders());
						throw new SkinChangeException("upload page not recieved, wrong credentials or minecraft.net down?" + "\nResult:\n" +
								"Header:\n" + headers + "\n\n" +
								"Body:\n" + (skinResponse.getEntity() != null ? EntityUtils.toString(skinResponse.getEntity()) : "null"));
					}
					// done
					if (resultProcessor != null) {
						resultProcessor.done(SkinChangerResult.SUCCESS, null);
					}
				} catch (SkinChangeException | IOException ex) {
					if (resultProcessor != null) {
						resultProcessor.done(SkinChangerResult.UNKNOWN_ERROR, ex);
					}
				}
			}
		}).start();
	}

	@Data
	@RequiredArgsConstructor
	private static class Agent {
		private final String name;
		private final int version = 1;
	}

	@Data
	private static class AuthenticatePayload {
		private final SkinChanger.Agent agent;
		private final String username;
		private final String password;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class AuthenticateResponse {
		private String accessToken;
		private String clientToken;
		private List<SkinChanger.Profile> availableProfiles;
		private SkinChanger.Profile selectedProfile;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class Profile {
		@JsonProperty("id")
		private String uuid;
		private String name;
		private boolean legacy;
	}
}
