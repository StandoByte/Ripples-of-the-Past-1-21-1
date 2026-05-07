package com.github.standobyte.jojo.client.standskin.text;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.TranslatableFormatException;

public abstract class CustomLangTranslatableContents extends TranslatableContents {

	public CustomLangTranslatableContents(String key, String fallback, Object[] args) {
		super(key, fallback, args);
	}

	/* Copypasted from TranslatableContents to use another Language instance.
	 * 
	 * Another option would be to use @ModifyVariable on the variable in TranslatableContents#decompose(),
	 * but I chose to override that method instead. Doing a (this instanceof StandSkinTranslatableContents) check
	 * in every single TranslatableContents#decompose() call felt like the more cursed option (both of them are).
	 */
	@Override
	public void decompose() {
		Language language = getLanguage();
		if (language != this.decomposedWith) {
			this.decomposedWith = language;

			Component langComponent = language.getComponent(this.key);
			if (langComponent != null) {
				this.decomposedParts = ImmutableList.of(langComponent);
				return;
			}

			String s = this.fallback != null ? language.getOrDefault(this.key, this.fallback) : language.getOrDefault(this.key);

			try {
				Builder<FormattedText> builder = ImmutableList.builder();
				this.decomposeTemplate(s, builder::add);
				this.decomposedParts = builder.build();
			} catch (TranslatableFormatException translatableformatexception) {
				this.decomposedParts = ImmutableList.of(FormattedText.of(s));
			}
		}
	}
	
	public abstract Language getLanguage();

	public static class SpecificLang extends CustomLangTranslatableContents {
		protected Language language;

		public SpecificLang(Language language, String key, String fallback, Object[] args) {
			super(key, fallback, args);
			this.language = language;
		}

		@Override
		public Language getLanguage() {
			return language;
		}
		
	}
	
}
