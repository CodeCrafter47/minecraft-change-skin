package de.janmm14.minecraftchangeskin.api;

import javax.annotation.Nullable;

public interface Callback<V> {

	void done(@Nullable V result, @Nullable Throwable error);
}
