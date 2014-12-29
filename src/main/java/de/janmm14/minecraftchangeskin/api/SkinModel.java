package de.janmm14.minecraftchangeskin.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SkinModel {

	STEVE("steve"), ALEX("3pxarm");

	private final String inForm;

	@Override
	public String toString() {
		return inForm;
	}
}
