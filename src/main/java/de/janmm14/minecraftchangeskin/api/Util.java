package de.janmm14.minecraftchangeskin.api;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.content.ContentBody;

import java.lang.reflect.Method;
import java.util.List;

public final class Util {
	private Util() {
		throw new UnsupportedOperationException();
	}

	/*@SuppressWarnings("unchecked")
	public static String getMultipartEntity(HttpEntity entity) throws ReflectiveOperationException {
		Class<? extends HttpEntity> clazz = entity.getClass();
		if (!clazz.getSimpleName().equalsIgnoreCase("MultipartFormEntity")) {
			return "***##NO MULTIPART##***";
		}
		Method multipart = clazz.getDeclaredMethod("getMultipart");
		multipart.setAccessible(true);
		Object abstractMultipartForm = multipart.invoke(entity);
		Class<?> clazzz = abstractMultipartForm.getClass();
		Method getBodyParts = clazzz.getDeclaredMethod("getBodyParts");
		getBodyParts.setAccessible(true);
		List<FormBodyPart> bodyParts = (List<FormBodyPart>) getBodyParts.invoke(abstractMultipartForm);
		StringBuilder sb = new StringBuilder();
		bodyParts.forEach(bodyPart -> {
			ContentBody body = bodyPart.getBody();
			sb.append("Name: ").append(bodyPart.getName())
				.append("ConentBody: ").append(body.getFilename())
				.append("/#/")
				.append(body.getCharset())
				.append("/#/")
				.append(body.getContentLength())
				.append("\n");
		});
		return sb.toString();
	}*/
}
