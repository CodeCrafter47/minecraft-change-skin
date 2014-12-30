package de.janmm14.minecraftchangeskin.api;

import lombok.*;

import java.io.File;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter(AccessLevel.PRIVATE)

public class SkinChangeParams {

	@NonNull
	private final String email;
	@NonNull
	private final String password;
	@NonNull
	private final File image;
	@NonNull
	private SkinModel skinModel = SkinModel.STEVE;
	@NonNull
	private String proxyIp = "";
	@NonNull
	private int proxyPort = -1;

	@Getter
	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Builder {

		@NonNull
		private String email;
		@NonNull
		private String password;
		@NonNull
		private File image;
		@NonNull
		private SkinModel skinModel = SkinModel.STEVE;
		@NonNull
		private String proxyIp = "";
		@NonNull
		private int proxyPort = -1;

		public static Builder create() {
			return new Builder();
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public Builder image(File image) {
			this.image = image;
			return this;
		}

		public Builder skinModel(SkinModel skinModel) {
			this.skinModel = skinModel;
			return this;
		}

		public Builder proxyIp(String proxyIp) {
			this.proxyIp = proxyIp;
			return this;
		}

		public Builder proxyPort(int proxyPort) {
			this.proxyPort = proxyPort;
			return this;
		}

		public SkinChangeParams build() {
			return new SkinChangeParams(email, password, image, skinModel, proxyIp, proxyPort);
		}
	}
}
