package com.galapea.techblog.textstorage;

import com.galapea.techblog.textstorage.entity.User;
import com.galapea.techblog.textstorage.model.CreateSnippet;
import com.galapea.techblog.textstorage.model.CreateUser;
import com.galapea.techblog.textstorage.service.SnippetService;
import com.galapea.techblog.textstorage.service.UserService;
import com.toshiba.mwcloud.gs.GSException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppSeeder implements CommandLineRunner {
    private final Logger log = LoggerFactory.getLogger(AppSeeder.class);
    private final UserService userService;
    private final SnippetService snippetService;

    @Override
    public void run(String... args) throws Exception {
        log.info("run....");
        seedUsers();
        Instant start = Instant.now();
        seedSnippets();
        Instant end = Instant.now();
        log.info(
                "Seed snippets execution time: {} seconds",
                Duration.between(start, end).toSeconds());
    }

    private void seedUsers() throws GSException {
        log.info("seed users....");
        CreateUser user = CreateUser.builder()
                .email("random.rand1@mail.com")
                .fullName("Mr. Random rand")
                .build();
        userService.create(user);
        user = CreateUser.builder()
                .email("woman.random@mail.com")
                .fullName("Mrs Woman Random")
                .build();
        userService.create(user);
        user = CreateUser.builder()
                .email("dom.dom.rand@mail.com")
                .fullName("Dom dom rand")
                .build();
        userService.create(user);
        user = CreateUser.builder()
                .email("makachakalalakala@mail.com")
                .fullName("Makachakalalakala")
                .build();
        userService.create(user);
    }

    private int getRandomNumberBetween(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private void seedSnippets() {
        List<User> users = userService.fetchAll();
        log.info("Waiting seed snippets....");
        CreateSnippet snippet = new CreateSnippet();
        snippet.setTitle("GridDB Java API Sample of Collection Operations");
        snippet.setContent(snippetContent1());
        snippet.setUserId(users.get(0).getId());
        snippetService.create(snippet);
        try {
            Thread.sleep(getRandomNumberBetween(30000, 50000));
        } catch (InterruptedException e) {
        }
        snippet = new CreateSnippet();
        snippet.setTitle("How to Live Tail Docker Logs");
        snippet.setContent(
                """
		In this article, we’ll talk a lot about Docker logs. You’ll learn how to display Docker logs in your console. Also, you’ll learn tips and tricks related to displaying logs. Lastly, this article will discuss how to configure Docker to send logs to SolarWinds® Papertrail™.
						""");
        snippet.setUserId(users.get(1).getId());
        snippetService.create(snippet);
        try {
            Thread.sleep(getRandomNumberBetween(60000, 70000));
        } catch (InterruptedException e) {
        }
        snippet = new CreateSnippet();
        snippet.setTitle("Python Playwright");
        snippet.setContent(
                """
		Playwright is a browser automation library developed and released by Microsoft in 2020. Since then, it has become one of the most popular libraries in automated testing of web applications and general browser automation tasks. However, it also plays a critical role in web scraping, especially in dealing with modern JavaScript-heavy websites where traditional HTML parsing tools like Beautiful Soup fall short.
						""");
        snippet.setUserId(users.get(2).getId());
        snippetService.create(snippet);
        try {
            Thread.sleep(getRandomNumberBetween(70000, 80000));
        } catch (InterruptedException e) {
        }
        snippet = new CreateSnippet();
        snippet.setTitle("Using HTML select options with Thymeleaf");
        snippet.setContent(contentUsingHTMLSelectThymeleaf());
        snippet.setUserId(users.get(2).getId());
        snippetService.create(snippet);
    }

    private String contentUsingHTMLSelectThymeleaf() {
        return """
		The relevant part of the Thymeleaf template should look like this:

<div>
	<label for="coachId" th:text="#{team.coach}"></label>
	<div>
	<select th:field="*{coachId}">
		<option th:each="user : ${users}"
			th:text="${user.name}"
			th:value="${user.id}">
	</select>
	</div>
</div>
Important points:

The <select> has a th:field attribute that references to the coachId property of the team form data object.
We create as many <option> subtags as there are users via the th:each iteration.
Use th:text for the visible text that the user will see.
Use th:value for the value associated with the option (The primary key of the user in our case)
						""";
    }

    private String snippetContent1() {
        return """
package test;


import java.util.Arrays;
import java.util.Properties;

import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;


// Operation on Collection data
public class Sample1 {

	static class Person {
		@RowKey String name;
		boolean status;
		long count;
		byte[] lob;
	}

	public static void main(String[] args) throws GSException {

		// Acquiring a GridStore instance
		Properties props = new Properties();
		props.setProperty("notificationAddress", args[0]);
		props.setProperty("notificationPort", args[1]);
		props.setProperty("clusterName", args[2]);
		props.setProperty("user", args[3]);
		props.setProperty("password", args[4]);
		GridStore store = GridStoreFactory.getInstance().getGridStore(props);

		// Creating a collection
		Collection<String, Person> col = store.putCollection("col01", Person.class);

		// Setting an index for a column
		col.createIndex("count");

		// Setting auto-commit off
		col.setAutoCommit(false);

		// Preparing row data
		Person person = new Person();
		person.name = "name01";
		person.status = false;
		person.count = 1;
		person.lob = new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74 };

		// Operating a row in KV format: RowKey is "name01"
		boolean update = true;
		col.put(person);	// Registration
		person = col.get(person.name, update);	// Aquisition (Locking to update)
		col.remove(person.name);	// Deletion

		// Operating a row in KV format: RowKey is "name02"
		col.put("name02", person);	// Registration (Specifying RowKey)

		// Committing transaction (releasing lock)
		col.commit();

		// Search rows in a container
		Query<Person> query = col.query("select * where name = 'name02'");

		// Fetching and updating retrieved rows
		RowSet<Person> rs = query.fetch(update);
		while (rs.hasNext()) {
			// Update the searched Row
			Person person1 = rs.next();
			person1.count += 1;
			rs.update(person1);

			System.out.println("Person:" +
					" name=" + person1.name +
					" status=" + person1.status +
					" count=" + person1.count +
					" lob=" + Arrays.toString(person1.lob));
		}

		// Committing transaction
		col.commit();

		// Releasing resource
		store.close();
	}

}
						""";
    }
}
