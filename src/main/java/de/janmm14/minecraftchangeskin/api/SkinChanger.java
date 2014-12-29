package de.janmm14.minecraftchangeskin.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

@SuppressWarnings("FinalClass")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SkinChanger {

	public static void main(String[] args) {
		changeSkin("", "", new File("C:\\\\"), SkinModel.STEVE, new Callback<Boolean>() {
			@Override
			public void done(Boolean result) {
				System.out.println(result);
			}
		});
	}

	public static void changeSkin(final String email, final String password, final File imageFile, final SkinModel skinModel, @Nullable final Callback<Boolean> resultProcessor) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				boolean result = false;
				callEnd:
				{
					try {
						HttpClient client = new HttpClient();
						// login page
						GetMethod getLoginPage = new GetMethod("https://minecraft.net/login");
						getLoginPage.setFollowRedirects(false);

						if (client.executeMethod(getLoginPage) != 200) {
							getLoginPage.releaseConnection();
							break callEnd;
						}
						String loginPageResponse = getLoginPage.getResponseBodyAsString();
						getLoginPage.releaseConnection();
						int lp_startIndex = loginPageResponse.indexOf("<input type=\"hidden\" name=\"authenticityToken\" value=\"");
						String loginToken = loginPageResponse.substring(lp_startIndex);
						int lp_endIndex = loginToken.indexOf("\">");
						loginToken = loginToken.substring(0, lp_endIndex);

						//login
						PostMethod login = new PostMethod("https://minecraft.net/login");
						login.addParameter("authenticityToken", loginToken);
						login.addParameter("username", email);
						login.addParameter("password", password);
						if (client.executeMethod(login) != 302) {
							login.releaseConnection();
							break callEnd;
						}
						login.releaseConnection();

						//upload page
						GetMethod getUploadPage = new GetMethod("https://minecraft.net/profile");
						getUploadPage.setFollowRedirects(false);

						if (client.executeMethod(getUploadPage) != 200) {
							getUploadPage.releaseConnection();
							break callEnd;
						}
						String uploadPageResponse = getUploadPage.getResponseBodyAsString();
						getUploadPage.releaseConnection();
						int up_startIndex = uploadPageResponse.indexOf("<input type=\"hidden\" name=\"authenticityToken\" value=\"");
						String uploadToken = uploadPageResponse.substring(up_startIndex);
						int up_endIndex = uploadToken.indexOf("\">");
						uploadToken = uploadToken.substring(0, up_endIndex);

						//upload new FileRequestEntity(imageFile, Files.probeContentType(imageFile.toPath()))
						PostMethod uploadSkin = new PostMethod("https://minecraft.net/profile");
						uploadSkin.setRequestEntity(new MultipartRequestEntity(new Part[]{
							new FilePart("file", imageFile, Files.probeContentType(imageFile.toPath()), StandardCharsets.UTF_8.name()),
							new StringPart("authenticityToken", uploadToken),
							new StringPart("model", skinModel.toString()),
						}, client.getParams()));
						client.executeMethod(uploadSkin);
						System.out.println(Arrays.toString(uploadSkin.getResponseHeaders()));
						System.out.println(uploadSkin.getResponseBodyAsString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// here you are if break callEnd; is used
				if (resultProcessor != null)
					resultProcessor.done(result);
			}
		}).start();
	}
}
