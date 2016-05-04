package de.janmm14.minecraftchangeskin.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SkinModel {

	STEVE(""), ALEX("slim");

	private final String inForm;

	@Override
	public String toString() {
		return inForm;
	}
}
