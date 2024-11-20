package com.dre.brewery.configuration.configurer;

import com.dre.brewery.configuration.annotation.LocalizedComment;
import eu.okaeri.configs.postprocessor.ConfigLineInfo;
import eu.okaeri.configs.postprocessor.ConfigPostprocessor;
import eu.okaeri.configs.postprocessor.format.YamlSectionWalker;
import eu.okaeri.configs.schema.ConfigDeclaration;
import eu.okaeri.configs.schema.FieldDeclaration;
import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.SerdesContext;
import eu.okaeri.configs.yaml.snakeyaml.YamlSnakeYamlConfigurer;
import lombok.NonNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BreweryXConfigurer extends YamlSnakeYamlConfigurer {
	private final Yaml yaml;
	private Map<String, Object> map = new LinkedHashMap<>();

	private String commentPrefix = "# ";

	public BreweryXConfigurer(@NonNull Yaml yaml, @NonNull Map<String, Object> map) {
		this.yaml = yaml;
		this.map = map;
	}

	public BreweryXConfigurer(@NonNull Yaml yaml) {
		this.yaml = yaml;
	}

	public BreweryXConfigurer() {
		this(createYaml());
	}

	private static Yaml createYaml() {

		LoaderOptions loaderOptions = new LoaderOptions();
		Constructor constructor = new Constructor(loaderOptions);

		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		dumperOptions.setIndent(2);
		dumperOptions.setWidth(80);
		dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

		Representer representer = new Representer(dumperOptions);
		representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

		Resolver resolver = new Resolver();

		return new Yaml(constructor, representer, dumperOptions, loaderOptions, resolver);
	}

	private static <T> T apply(T object, Consumer<T> consumer) {
		consumer.accept(object);
		return object;
	}

	@Override
	public List<String> getExtensions() {
		return Arrays.asList("yml", "yaml");
	}

	@Override
	public void setValue(@NonNull String key, Object value, GenericsDeclaration type, FieldDeclaration field) {
		Object simplified = this.simplify(value, type, SerdesContext.of(this, field), true);
		this.map.put(key, simplified);
	}

	@Override
	public void setValueUnsafe(@NonNull String key, Object value) {
		this.map.put(key, value);
	}

	@Override
	public Object getValue(@NonNull String key) {
		return this.map.get(key);
	}

	@Override
	public Object remove(@NonNull String key) {
		return this.map.remove(key);
	}

	@Override
	public boolean keyExists(@NonNull String key) {
		return this.map.containsKey(key);
	}

	@Override
	public List<String> getAllKeys() {
		return Collections.unmodifiableList(new ArrayList<>(this.map.keySet()));
	}

	@Override
	public void load(@NonNull InputStream inputStream, @NonNull ConfigDeclaration declaration) throws Exception {
		// try loading from input stream
		this.map = this.yaml.load(inputStream);
		// when no map was loaded reset with empty
		if (this.map == null) this.map = new LinkedHashMap<>();
	}

	@Override
	public void write(@NonNull OutputStream outputStream, @NonNull ConfigDeclaration declaration) throws Exception {


		Map<String, Object> mapCopy = new LinkedHashMap<>(this.map); // Not sure if I should copy or do this on the main map
		// Remove null values
		removeNullValues(mapCopy);
		// render to string
		String contents = this.yaml.dump(mapCopy);

		// postprocess
		ConfigPostprocessor.of(contents)
			// remove all current top-level comments
			.removeLines((line) -> line.startsWith(this.commentPrefix.trim()))
			// add new comments
			.updateLinesKeys(new YamlSectionWalker() {
				@Override
				public String update(String line, ConfigLineInfo lineInfo, List<ConfigLineInfo> path) {

					ConfigDeclaration currentDeclaration = declaration;
					for (int i = 0; i < (path.size() - 1); i++) {
						ConfigLineInfo pathElement = path.get(i);
						Optional<FieldDeclaration> field = currentDeclaration.getField(pathElement.getName());
						if (!field.isPresent()) {
							return line;
						}
						GenericsDeclaration fieldType = field.get().getType();
						if (!fieldType.isConfig()) {
							return line;
						}
						currentDeclaration = ConfigDeclaration.of(fieldType.getType());
					}

					Optional<FieldDeclaration> lineDeclaration = currentDeclaration.getField(lineInfo.getName());
					if (lineDeclaration.isEmpty()) {
						return line;
					}

					// Localized comments
					String[] fieldComment = lineDeclaration.get().getComment(); // regular okaeri comments
					String[] localizedComment = getFieldComments(lineDeclaration.get()); // localized ones

					// Joins 2 nullable arrays
					String[] finalComment = Stream.of(fieldComment, localizedComment)
						.filter(Objects::nonNull) // omit if null
						.flatMap(Arrays::stream)
						.toArray(String[]::new);

					if (finalComment.length == 0)
						return line;

					String comment = ConfigPostprocessor.createComment(BreweryXConfigurer.this.commentPrefix, finalComment);
					return ConfigPostprocessor.addIndent(comment, lineInfo.getIndent()) + line;
				}
			})
			// add header if available
			.prependContextComment(this.commentPrefix, declaration.getHeader())
			// save
			.write(outputStream);
	}

	@Override
	public YamlSnakeYamlConfigurer setCommentPrefix(String commentPrefix) {
		this.commentPrefix = commentPrefix;
		return this;
	}

	/**
	 * Processes the {{@link TranslationManager}} annotation
	 * Doesn't throw when there is no translation!
	 */
	public String[] getFieldComments(FieldDeclaration fieldDeclaration) {
		LocalizedComment localizedComment = fieldDeclaration.getField().getAnnotation(LocalizedComment.class);
		if (localizedComment == null || localizedComment.value().length == 0)
			return null;

		TranslationManager translationManager = TranslationManager.getInstance();

		return Arrays.stream(localizedComment.value())
			.map(translationManager::getTranslationWithFallback)
			.filter(Objects::nonNull) // Remove null translations
			.flatMap(translation -> Arrays.stream(translation.split("\n"))) // Split translations by lines
			.toArray(String[]::new);
	}

	public void removeNullValues(Map<String, Object> map) {
		if (map == null) {
			return;
		}

		// Iterate over the map and remove null values
		Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, Object> entry = iterator.next();
			Object value = entry.getValue();

			if (value == null) {
				iterator.remove(); // Remove entry if value is null
			} else if (value instanceof Map) {
				// If the value is a map, recursively remove null values inside it
				removeNullValues((Map<String, Object>) value);
				if (((Map<String, Object>) value).isEmpty()) {
					iterator.remove(); // Remove the map if it becomes empty after cleaning
				}
			}
		}
	}
}
