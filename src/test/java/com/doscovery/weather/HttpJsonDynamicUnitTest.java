package com.doscovery.weather;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.Stopwatch;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import com.discovery.weather.WeatherApiDemoApplication;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WeatherApiDemoApplication.class)
@WebAppConfiguration
public class HttpJsonDynamicUnitTest {
	private static class Colors {
		public static final String BLACK = "\033[0;30m";

		public static final String BLACK_BOLD = "\033[1;30m";
		public static final String BLUE = "\033[0;34m";
		public static final String BLUE_BOLD = "\033[1;34m";
		public static final String GREEN = "\033[0;32m";
		public static final String GREEN_BOLD = "\033[1;32m";
		public static final String RED = "\033[0;31m";

		public static final String RED_BOLD = "\033[1;31m";
		public static final String RESET = "\033[0m";
		public static final String WHITE = "\033[0;37m";
		public static final String WHITE_BOLD = "\033[1;37m";
		public static final String YELLOW = "\033[0;33m";
		public static final String YELLOW_BOLD = "\033[1;33m";
	}

	private static final MediaType CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON_UTF8;
	private static final MediaType CONTENT_TYPE_TEXT = MediaType.TEXT_PLAIN;

	private static HttpMessageConverter mappingJackson2HttpMessageConverter;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	Map<String, Long> executionTime = new HashMap<>();

	Map<String, String> httpJsonAndTestname = new HashMap<>();

	List<String> httpJsonFiles = new ArrayList<>();

	private MockMvc mockMvc;
	@Rule
	public Stopwatch stopwatch = new Stopwatch() {
	};
	Map<String, Pair<Pair<String, String>, Pair<String, String>>> testFailures = new HashMap<>();
	@Rule
	public TestWatcher watchman = new TestWatcher() {
		@Override
		public Statement apply(final Statement base, final Description description) {
			return super.apply(base, description);
		}

		@Override
		protected void failed(final Throwable e, final Description description) {
			generateReportForRuntimeFailureExecution();
		}

		@Override
		protected void finished(final Description description) {
			super.finished(description);
		}

		@Override
		protected void starting(final Description description) {
			super.starting(description);
		}

		@Override
		protected void succeeded(final Description description) {
			generateReportForProperExecution();
		}
	};

	@Autowired
	private WebApplicationContext webApplicationContext;

	private void addTestFailure(final String filename, final Pair<Pair<String, String>, Pair<String, String>> failure) {
		if (testFailures.containsKey(filename)) {
			throw new Error("I should skip rest of the test cases.");
		}

		testFailures.put(filename, failure);
	}

	@Test
	public void dynamicTests() {
		try {
			httpJsonFiles = Files.list(Paths.get("src/test/resources/testcases")).filter(Files::isRegularFile)
					.map(f -> f.getFileName().toString()).filter(f -> f.endsWith(".json")).collect(toList());
		} catch (final IOException ex) {
			throw new Error(ex.toString());
		}

		if (!httpJsonFiles.isEmpty()) {
			List<String> testnames = new ArrayList<>();

			final ClassPathResource resource = new ClassPathResource("testcases/description.txt");
			try (InputStream inputStream = resource.getInputStream()) {
				testnames = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
						.collect(toList());
			} catch (final IOException ex) {
				System.out.println(String.join("\n",
						Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

				throw new Error(ex.toString());
			}

			if (!testnames.isEmpty()) {
				assertEquals(httpJsonFiles.size(), testnames.size());

				for (int i = 0; i < testnames.size(); i++) {
					final String[] testname = testnames.get(i).split(": ");
					httpJsonAndTestname.put(testname[0], testname[1]);
				}

				final AtomicInteger processedRequestCount = new AtomicInteger(1);

				httpJsonFiles.forEach(filename -> {
					if (testFailures.containsKey(filename)) {
						return;
					}

					final List<String> jsonStrings = new ArrayList<>();

					final ClassPathResource jsonResource = new ClassPathResource("testcases/" + filename);
					try (InputStream inputStream = jsonResource.getInputStream()) {
						new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
								.collect(toList()).forEach(jsonString -> jsonStrings.add(jsonString));
					} catch (final IOException ex) {
						System.out.println(String.join("\n",
								Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

						throw new Error(ex.toString());
					}

					if (!jsonStrings.isEmpty()) {
						jsonStrings.forEach(jsonString -> {
							if (testFailures.containsKey(filename)) {
								return;
							}

							try {
								final JsonNode jsonObject = OBJECT_MAPPER.readTree(jsonString);

								final JsonNode request = jsonObject.get("request");
								final JsonNode response = jsonObject.get("response");

								final String method = request.get("method").asText();
								final String url = request.get("url").asText();
								final String body = request.get("body").toString();
								final String statusCode = response.get("status_code").asText();

								final String requestID = Colors.BLUE_BOLD
										+ String.format("Processing request %d ", processedRequestCount.get())
										+ Colors.RESET;
								String requestMessage = String.format("%s %s", method, url);

								if (method.charAt(0) == 'P') {
									requestMessage = String.format("%s %s %s", method, url, body);
								}

								System.out.println(requestID + Colors.WHITE_BOLD + requestMessage + Colors.RESET);

								processedRequestCount.set(processedRequestCount.incrementAndGet());

								switch (method) {
								case "POST": {
									MediaType contentType = MediaType.ALL;
									final String type = request.get("headers").get("Content-Type").asText();

									if (type.equals("application/json")) {
										contentType = CONTENT_TYPE_JSON;
									} else if (type.equals("text/plain")) {
										contentType = CONTENT_TYPE_TEXT;
									}

									if (!contentType.equals(MediaType.ALL)) {
										try {
											final ResultActions resultActions = mockMvc
													.perform(post(url).content(body).contentType(CONTENT_TYPE_JSON));
											final MockHttpServletResponse mockResponse = resultActions.andReturn()
													.getResponse();

											validateStatusCode(filename, method + " " + url, statusCode,
													String.valueOf(mockResponse.getStatus()));
										} catch (final Exception ex) {
											System.out.println(String.join("\n", Stream.of(ex.getStackTrace())
													.map(trace -> trace.toString()).collect(toList())));

											throw new Error(ex.toString());
										}
									}

									break;
								}
								case "PUT": {
									MediaType contentType = MediaType.ALL;
									final String type = request.get("headers").get("Content-Type").asText();

									if (type.equals("application/json")) {
										contentType = CONTENT_TYPE_JSON;
									} else if (type.equals("text/plain")) {
										contentType = CONTENT_TYPE_TEXT;
									}

									if (!contentType.equals(MediaType.ALL)) {
										try {
											final ResultActions resultActions = mockMvc
													.perform(put(url).content(body).contentType(CONTENT_TYPE_JSON));
											final MockHttpServletResponse mockResponse = resultActions.andReturn()
													.getResponse();

											validateStatusCode(filename, method + " " + url, statusCode,
													String.valueOf(mockResponse.getStatus()));
										} catch (final Exception ex) {
											System.out.println(String.join("\n", Stream.of(ex.getStackTrace())
													.map(trace -> trace.toString()).collect(toList())));

											throw new Error(ex.toString());
										}
									}

									break;
								}
								case "DELETE":
									try {
										final ResultActions resultActions = mockMvc.perform(delete(url));
										final MockHttpServletResponse mockResponse = resultActions.andReturn()
												.getResponse();

										validateStatusCode(filename, method + " " + url, statusCode,
												String.valueOf(mockResponse.getStatus()));
									} catch (final Exception ex) {
										System.out.println(String.join("\n", Stream.of(ex.getStackTrace())
												.map(trace -> trace.toString()).collect(toList())));

										throw new Error(ex.toString());
									}

									break;
								case "GET":
									try {
										final ResultActions resultActions = mockMvc.perform(get(url));
										final MockHttpServletResponse mockResponse = resultActions.andReturn()
												.getResponse();

										if (validateStatusCode(filename, method + " " + url, statusCode,
												String.valueOf(mockResponse.getStatus()))) {
											final JsonNode expectedType = response.get("headers").get("Content-Type");
											if (expectedType != null) {
												if (mockResponse.containsHeader("content-type")) {
													validateContentType(filename, method + " " + url,
															expectedType.asText(), mockResponse.getContentType());
												}

												if (statusCode.equals("200")) {
													final String responseBody = mockResponse.getContentAsString();
													final JsonNode expectedResponseBodyJson = response.get("body");

													if (expectedType.asText().equals("application/json")) {
														final JsonNode responseBodyJson = OBJECT_MAPPER
																.readTree(responseBody);

														validateJsonResponse(filename, method + " " + url,
																expectedResponseBodyJson, responseBodyJson);
													} else if (expectedType.asText().equals("text/plain")) {
														validateTextResponse(filename, method + " " + url,
																expectedResponseBodyJson.toString(), responseBody);
													}
												}
											}
										}
									} catch (final Exception ex) {
										System.out.println(String.join("\n", Stream.of(ex.getStackTrace())
												.map(trace -> trace.toString()).collect(toList())));

										throw new Error(ex.toString());
									}

									break;
								default:
									break;
								}
							} catch (final IOException ex) {
								System.out.println(String.join("\n", Stream.of(ex.getStackTrace())
										.map(trace -> trace.toString()).collect(toList())));

								throw new Error(ex.toString());
							}
						});
					}

					executionTime.put(filename, stopwatch.runtime(TimeUnit.MILLISECONDS));
				});
			}
		}
	}

	private void generateReportForProperExecution() {
		final List<Long> executionTimeInSeconds = executionTime.keySet().stream().sorted()
				.map(filename -> executionTime.get(filename)).collect(toList());

		for (int i = 1; i < executionTimeInSeconds.size(); i++) {
			executionTime.put(httpJsonFiles.get(i),
					executionTimeInSeconds.get(i) < executionTimeInSeconds.get(i - 1) ? 0
							: executionTimeInSeconds.get(i) - executionTimeInSeconds.get(i - 1));
		}

		final Set<String> failedTestFiles = testFailures.keySet();

		final String DASHES = "------------------------------------------------------------------------";
		final String ANSI_SUMMARY = DASHES + "\n" + Colors.BLUE_BOLD + "TEST SUMMARY\n" + Colors.RESET + DASHES;
		final String ANSI_RESULT = DASHES + "\n" + Colors.BLUE_BOLD + "TEST RESULT\n" + Colors.RESET + DASHES;
		final String ANSI_REPORT = DASHES + "\n" + Colors.BLUE_BOLD + "FAILURE REPORT %s\n" + Colors.RESET + DASHES;
		final String ANSI_FAILURE = Colors.RED_BOLD + "Failure" + Colors.RESET;
		final String ANSI_SUCCESS = Colors.GREEN_BOLD + "Success" + Colors.RESET;

		final File reportFolder = new File("target/customReports");
		reportFolder.mkdir();

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("target/customReports/result.txt"))) {
			writer.write(Colors.WHITE_BOLD
					+ " _    _       _ _     _______        _     _____                       _   \n"
					+ "| |  | |     (_) |   |__   __|      | |   |  __ \\                     | |  \n"
					+ "| |  | |_ __  _| |_     | | ___  ___| |_  | |__) |___ _ __   ___  _ __| |_ \n"
					+ "| |  | | '_ \\| | __|    | |/ _ \\/ __| __| |  _  // _ \\ '_ \\ / _ \\| '__| __|\n"
					+ "| |__| | | | | | |_     | |  __/\\__ \\ |_  | | \\ \\  __/ |_) | (_) | |  | |_ \n"
					+ " \\____/|_| |_|_|\\__|    |_|\\___||___/\\__| |_|  \\_\\___| .__/ \\___/|_|   \\__|\n"
					+ "                                                     | |                   \n"
					+ "                                                     |_|                   " + Colors.RESET);
			writer.newLine();

			writer.write(ANSI_SUMMARY);
			writer.newLine();
			writer.write("Tests: " + httpJsonFiles.size());
			writer.write(", ");
			writer.write("Success: " + (httpJsonFiles.size() - failedTestFiles.size()));
			writer.write(", ");

			if (!failedTestFiles.isEmpty()) {
				writer.write("Failure: " + failedTestFiles.size());
				writer.write(", ");
			}

			writer.write(
					"Total time: " + executionTimeInSeconds.get(executionTimeInSeconds.size() - 1) / 1000.0f + "s");
			writer.newLine();
			writer.newLine();

			writer.write(ANSI_RESULT);
			writer.newLine();

			httpJsonFiles.forEach(filename -> {
				if (failedTestFiles.contains(filename)) {
					try {
						writer.write(Colors.WHITE_BOLD + filename + ": " + Colors.RESET + ANSI_FAILURE + " ("
								+ executionTime.get(filename) / 1000.0f + "s)");
						writer.newLine();
					} catch (final IOException ex) {
						System.out.println(String.join("\n",
								Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

						throw new Error(ex.toString());
					}
				} else {
					try {
						writer.write(Colors.WHITE_BOLD + filename + ": " + Colors.RESET + ANSI_SUCCESS + " ("
								+ executionTime.get(filename) / 1000.0f + "s)");
						writer.newLine();
					} catch (final IOException ex) {
						System.out.println(String.join("\n",
								Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

						throw new Error(ex.toString());
					}
				}
			});

			writer.newLine();

			final Map<String, String> fileFailureReason = new HashMap<>();

			if (!failedTestFiles.isEmpty()) {
				failedTestFiles.stream().sorted().forEachOrdered(filename -> {
					final Pair<Pair<String, String>, Pair<String, String>> report = testFailures.get(filename);

//					final String testcase = report.getKey().getKey();
//					final String reason = report.getKey().getValue();
//
//					final String expected = report.getValue().getKey();
//					final String found = report.getValue().getValue();

					final String testcase = report.getFirst().getFirst();
					final String reason = report.getFirst().getSecond();

					final String expected = report.getSecond().getFirst();
					final String found = report.getSecond().getSecond();

					fileFailureReason.put(filename, reason);

					try {
						writer.write(String.format(ANSI_REPORT, filename));
						writer.newLine();
						writer.write(Colors.WHITE_BOLD + "[Test Case]" + Colors.RESET + " " + testcase);
						writer.newLine();
						writer.write(Colors.WHITE_BOLD + "[   Reason]" + Colors.RESET + " " + Colors.RED_BOLD + reason
								+ Colors.RESET);
						writer.newLine();
						writer.write(Colors.WHITE_BOLD + "[ Expected]" + Colors.RESET + " " + expected);
						writer.newLine();
						writer.write(Colors.WHITE_BOLD + "[    Found]" + Colors.RESET + " " + found);
						writer.newLine();
						writer.newLine();
					} catch (final IOException ex) {
						System.out.println(String.join("\n",
								Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

						throw new Error(ex.toString());
					}
				});
			}
		} catch (final IOException ex) {
			System.out.println(
					String.join("\n", Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

			throw new Error(ex.toString());
		}

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("target/customReports/result.xml"))) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.write(String.format(
					"<testsuite name=\"%s\" time=\"%f\" tests=\"%d\" errors=\"0\" skipped=\"0\" failures=\"%d\">\n",
					this.getClass().getName(), executionTimeInSeconds.get(executionTimeInSeconds.size() - 1) / 1000.0f,
					httpJsonFiles.size(), failedTestFiles.size()));

			httpJsonFiles.stream().sorted().forEachOrdered(filename -> {
				try {
					if (!failedTestFiles.contains(filename)) {
						writer.write(String.format("    <testcase name=\"%s\" classname=\"%s\" time=\"%f\"/>\n",
								httpJsonAndTestname.get(filename), this.getClass().getName(),
								executionTime.get(filename) / 1000.0f));
					} else {
						final Pair<Pair<String, String>, Pair<String, String>> report = testFailures.get(filename);
						// final String reason = report.getKey().getValue();
						final String reason = report.getFirst().getFirst();

						writer.write(String.format(
								"    <testcase name=\"%s\" classname=\"%s\" time=\"%f\">\n"
										+ "        <failure>\n            " + reason
										+ "\n        </failure>\n    </testcase>\n",
								httpJsonAndTestname.get(filename), this.getClass().getName(),
								executionTime.get(filename) / 1000.0f));
					}
				} catch (final IOException ex) {
					System.out.println(String.join("\n",
							Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

					throw new Error(ex.toString());
				}
			});

			writer.write("</testsuite>\n");
		} catch (final IOException ex) {
			System.out.println(
					String.join("\n", Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

			throw new Error(ex.toString());
		}
	}

	private void generateReportForRuntimeFailureExecution() {
		final String DASHES = "------------------------------------------------------------------------";
		final String ANSI_SUMMARY = DASHES + "\n" + Colors.BLUE_BOLD + "TEST SUMMARY\n" + Colors.RESET + DASHES;
		final String ANSI_RESULT = DASHES + "\n" + Colors.BLUE_BOLD + "TEST RESULT\n" + Colors.RESET + DASHES;
		final String ANSI_REPORT = DASHES + "\n" + Colors.BLUE_BOLD + "FAILURE REPORT %s\n" + Colors.RESET + DASHES;
		final String ANSI_FAILURE = Colors.RED_BOLD + "Failure" + Colors.RESET;
		final String ANSI_SUCCESS = Colors.GREEN_BOLD + "Success" + Colors.RESET;

		final File reportFolder = new File("target/customReports");
		reportFolder.mkdir();

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("target/customReports/result.txt"))) {
			writer.write(Colors.WHITE_BOLD
					+ " _    _       _ _     _______        _     _____                       _   \n"
					+ "| |  | |     (_) |   |__   __|      | |   |  __ \\                     | |  \n"
					+ "| |  | |_ __  _| |_     | | ___  ___| |_  | |__) |___ _ __   ___  _ __| |_ \n"
					+ "| |  | | '_ \\| | __|    | |/ _ \\/ __| __| |  _  // _ \\ '_ \\ / _ \\| '__| __|\n"
					+ "| |__| | | | | | |_     | |  __/\\__ \\ |_  | | \\ \\  __/ |_) | (_) | |  | |_ \n"
					+ " \\____/|_| |_|_|\\__|    |_|\\___||___/\\__| |_|  \\_\\___| .__/ \\___/|_|   \\__|\n"
					+ "                                                     | |                   \n"
					+ "                                                     |_|                   " + Colors.RESET);
			writer.newLine();

			writer.write(ANSI_SUMMARY);
			writer.newLine();
			writer.write("Tests: " + httpJsonFiles.size());
			writer.write(", ");
			writer.write("Success: 0");
			writer.write(", ");
			writer.write("Failure: " + httpJsonFiles.size());
			writer.write(", ");
			writer.write("Total time: 0s");
			writer.newLine();
			writer.newLine();

			writer.write(ANSI_RESULT);
			writer.newLine();

			httpJsonFiles.forEach(filename -> {
				try {
					writer.write(Colors.WHITE_BOLD + filename + ": " + Colors.RESET + ANSI_FAILURE + " (0s)");
					writer.newLine();
				} catch (final IOException ex) {
					System.out.println(String.join("\n",
							Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

					throw new Error(ex.toString());
				}
			});
		} catch (final IOException ex) {
			System.out.println(
					String.join("\n", Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

			throw new Error(ex.toString());
		}

		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("target/customReports/result.xml"))) {
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			writer.write(String.format(
					"<testsuite name=\"%s\" time=\"%f\" tests=\"%d\" errors=\"0\" skipped=\"0\" failures=\"%d\">\n",
					this.getClass().getName(), 0.0f, httpJsonFiles.size(), httpJsonFiles.size()));

			httpJsonFiles.stream().sorted().forEachOrdered(filename -> {
				try {
					writer.write(String.format("    <testcase name=\"%s\" classname=\"%s\" time=\"%f\">\n"
							+ "        <failure>\n            Runtime Error\n        </failure>\n    </testcase>\n",
							httpJsonAndTestname.get(filename), this.getClass().getName(), 0.0f));
				} catch (final IOException ex) {
					System.out.println(String.join("\n",
							Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

					throw new Error(ex.toString());
				}
			});

			writer.write("</testsuite>\n");
		} catch (final IOException ex) {
			System.out.println(
					String.join("\n", Stream.of(ex.getStackTrace()).map(trace -> trace.toString()).collect(toList())));

			throw new Error(ex.toString());
		}
	}

	@Before
	public void getContext() {
		mockMvc = webAppContextSetup(webApplicationContext).build();
		assertNotNull(mockMvc);
	}

	@Autowired
	public void setConverters(final HttpMessageConverter<?>[] converters) {
		mappingJackson2HttpMessageConverter = Stream.of(converters)
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().orElse(null);

		assertNotNull(mappingJackson2HttpMessageConverter);
	}

	private boolean validateContentType(final String filename, final String testcase, final String expected,
			final String found) {
		if (!found.startsWith(expected)) {
			final String reason = "Content type";
			// addTestFailure(filename, new Pair(new Pair(testcase, reason), new
			// Pair(expected, found)));
			addTestFailure(filename, Pair.of(Pair.of(testcase, reason), Pair.of(expected, found)));

			return false;
		}

		return true;
	}

	private boolean validateJsonResponse(final String filename, final String testcase, final JsonNode expected,
			final JsonNode found) {
		try {
			final List<JsonNode> expectedResponseJsonList = OBJECT_MAPPER.readValue(expected.toString(),
					new TypeReference<List<JsonNode>>() {
					});

			final List<JsonNode> responseBodyJsonList = OBJECT_MAPPER.readValue(found.toString(),
					new TypeReference<List<JsonNode>>() {
					});

			if (expectedResponseJsonList.size() != responseBodyJsonList.size()) {
				final String reason = "Response Json array size does not match with the expected array size";
				addTestFailure(filename,
						Pair.of(Pair.of(testcase, reason), Pair.of(String.valueOf(expectedResponseJsonList.size()),
								String.valueOf(responseBodyJsonList.size()))));

				return false;
			} else {
				for (int i = 0; i < expectedResponseJsonList.size(); i++) {
					final JsonNode expectedJson = expectedResponseJsonList.get(i);
					final JsonNode foundJson = responseBodyJsonList.get(i);

					if (!expectedJson.equals(foundJson)) {
						final String reason = String
								.format("Response Json (at index %d) does not match with the expected Json", i);
						addTestFailure(filename, Pair.of(Pair.of(testcase, reason),
								Pair.of(expectedJson.toString(), foundJson.toString())));

						return false;
					}
				}
			}
		} catch (final IOException ex) {
			if (!expected.equals(found)) {
				final String reason = "Response Json does not match with the expected Json";
				addTestFailure(filename,
						Pair.of(Pair.of(testcase, reason), Pair.of(expected.toString(), found.toString())));

				return false;
			}
		}

		return true;
	}

	private boolean validateStatusCode(final String filename, final String testcase, final String expected,
			final String found) {
		if (!expected.equals(found)) {
			final String reason = "Status code";
			addTestFailure(filename, Pair.of(Pair.of(testcase, reason), Pair.of(expected, found)));

			return false;
		}

		return true;
	}

	private boolean validateTextResponse(final String filename, final String testcase, final String expected,
			final String found) {
		if (!expected.equals(found)) {
			final String reason = "Response text does not match with the expected response";
			addTestFailure(filename, Pair.of(Pair.of(testcase, reason), Pair.of(expected, found)));

			return false;
		}

		return true;
	}
}
