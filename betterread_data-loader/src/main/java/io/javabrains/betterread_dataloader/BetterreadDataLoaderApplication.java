package io.javabrains.betterread_dataloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import connection.DataStaxAstraProperties;
import io.javabrains.betterread_dataloader.author.Author;
import io.javabrains.betterread_dataloader.author.AuthorRepository;
import io.javabrains.betterread_dataloader.book.Book;
import io.javabrains.betterread_dataloader.book.BookRepository;

@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class)
public class BetterreadDataLoaderApplication {

	@Autowired
	AuthorRepository authorRepository;

	@Autowired
	BookRepository bookRepository;

	@Value("${datadump.loction.author}")
	private String authorDumpLocation;

	@Value("${datadump.loction.works}")
	private String worksDumpLocation;

	public static void main(String[] args) {
		SpringApplication.run(BetterreadDataLoaderApplication.class, args);

	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}

	private void initAuthor() {
		Path path = Paths.get(authorDumpLocation);

		try (Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				String jsonstring = line.substring(line.indexOf("{"));

				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(jsonstring);
					Author author = new Author();

					author.setName(jsonObject.optString("name"));
					author.setPersonalName(jsonObject.optString("personal_name"));
					author.setId(jsonObject.optString("key").replace("/authors/", ""));

					authorRepository.save(author);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initWorks() {
		Path path = Paths.get(worksDumpLocation);
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:s.SSSSSS");

		try (Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				String jsonstring = line.substring(line.indexOf("{"));

				JSONObject jsonObject;
				try {
					jsonObject = new JSONObject(jsonstring);
					Book book = new Book();

					book.setId(jsonObject.optString("key").replace("/works/", ""));
					book.setName(jsonObject.optString("title"));
					// book.setAuthorIds(authorIds);
					// book.setAuthorNames(authorNames);

					JSONObject descriptionObj = jsonObject.optJSONObject("description");
					if (descriptionObj != null)
						book.setDescription(descriptionObj.optString("value"));

					JSONObject publishedDateObj = jsonObject.optJSONObject("created");
					if (publishedDateObj != null)
						book.setPublishedDate(LocalDate.parse(publishedDateObj.optString("value"), dateTimeFormatter));

					JSONArray coverJsonArray = jsonObject.optJSONArray("covers");
					if (coverJsonArray != null) {
						List<String> coverIds = new ArrayList<>();

						for (int i = 0; i < coverJsonArray.length(); i++) {
							coverIds.add(coverJsonArray.getString(i));
						}
						book.setCoverIds(coverIds);
					}

					JSONArray authorJsonArray = jsonObject.optJSONArray("authors");
					if (authorJsonArray != null) {
						List<String> authorIds = new ArrayList<>();

						for (int i = 0; i < authorJsonArray.length(); i++) {
							String authorId = authorJsonArray.getJSONObject(i).getJSONObject("author").getString("key")
									.replace("/authors/", "");
							authorIds.add(authorId);
						}
						book.setAuthorIds(authorIds);

						List<String> authorNames = authorIds.stream().map(id -> authorRepository.findById(id))
								.map(optionalAuthor -> {
									if (optionalAuthor == null)
										return "Unknown Author";
									return optionalAuthor.get().getName();
								}).collect(Collectors.toList());

						book.setAuthorNames(authorNames);

					}
					System.out.println("saving Book" + book.getName() + "...");
					bookRepository.save(book);

				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@PostConstruct
	public void start() {
		// initAuthor();
		initWorks();

	}

}
