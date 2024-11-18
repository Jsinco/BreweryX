package com.dre.brewery.files.configurer;

import com.dre.brewery.files.Config;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Translation {

    ENGLISH("en", 0),
    GERMAN("de", 1),
    SPANISH("es", 2),
    FRENCH("fr", 3),
    ITALIAN("it", 4),
    RUSSIAN("ru", 5),
    CHINESE("zh", 6);

    private final String key;
    private final int commentIndex;

    // TODO: Need someone to look at this
    // This SHOULD be fine? I'm making a separate static field from this separate from our Config class because I don't want to
    // have to worry about potential issues when the Config is reading/writing. I'd rather just have this variable set once and not have to worry
    // about this variable again. (Though I'm not too sure what to do about config reloading just yet)
    public static Translation ACTIVE_TRANSLATION = Config.getInstance().getLanguage();
}
