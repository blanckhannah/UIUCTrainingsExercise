package com.example.TrainingsExercise;

import com.example.TrainingsExercise.models.Person;
import com.example.TrainingsExercise.models.Training;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@SpringBootApplication
public class TrainingsExerciseApplication {
	public static void main(String[] args) {
		// List each completed training with a count of how many people have completed that training.
		task1();
		// Given a list of trainings and a fiscal year (defined as 7/1/n-1 – 6/30/n), for each specified training,
		// list all people that completed that training in the specified fiscal year.
		// Use parameters: Trainings = "Electrical Safety for Labs", "X-Ray Safety", "Laboratory Safety Training"; Fiscal Year = 2024
		task2();
		// Given a date, find all people that have any completed trainings that have already expired, or will expire within one month
		// of the specified date (A training is considered expired the day after its expiration date).
		// For each person found, list each completed training that met the previous criteria, with an additional field to indicate expired vs expires soon.
		// Use date: Oct 1st, 2023
		task3();
	}

	public static void task1() {
		try {
			File file = new File(TrainingsExerciseApplication.class.getResource("/trainings (correct).txt").toURI());
			Scanner scanner = new Scanner(file);
			List<Person> people = new ArrayList<>();

			String currentPersonName = null;
			List<Training> currentCompletions = new ArrayList<>();

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();

				if (line.startsWith("{")) {
					while (scanner.hasNextLine()) {
						line = scanner.nextLine().trim();

						// Get the person's name
						if (line.startsWith("\"name\":")) {
							currentPersonName = extractValue(line);
						}
						// Get the completions for the person
						else if (line.startsWith("\"completions\":")) {
							while (scanner.hasNextLine()) {
								line = scanner.nextLine().trim();
								if (line.equals("]")) {
									break;
								}
								// Process each training object
								if (line.startsWith("{")) {
									String trainingName = null;
									String timestamp = null;
									String expires = null;
									// Read trainings
									while (scanner.hasNextLine()) {
										line = scanner.nextLine().trim();
										if (line.startsWith("\"name\":")) {
											trainingName = extractValue(line);
										} else if (line.startsWith("\"timestamp\":")) {
											timestamp = extractValue(line);
										} else if (line.startsWith("\"expires\":")) {
											expires = extractValue(line);
										}
										if (line.equals("}")) {
											break;
										}
									}
									// Add training to completions if the name is not null
									if (trainingName != null) {
										currentCompletions.add(new Training(trainingName, timestamp, expires));
									}
								}
							}
						}
						// Add the person to the list if reached the end
						if (line.equals("}")) {
							people.add(new Person(currentPersonName, currentCompletions));
							break;
						}
					}
				}
			}
			scanner.close();
			// Count how many people completed each training
			Map<String, Integer> trainingCounts = new HashMap<>();
			for (Person person : people) {
				for (Training training : person.getCompletions()) {
					trainingCounts.put(training.getName(), trainingCounts.getOrDefault(training.getName(), 0) + 1);
				}
			}
			writeOutput("target/output/completed_training_counts.json", trainingCounts);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void task2() {
		try {
			File file = new File(TrainingsExerciseApplication.class.getResource("/trainings (correct).txt").toURI());
			Scanner scanner = new Scanner(file);
			List<Person> people = new ArrayList<>();

			parsePeople(scanner, people);
			scanner.close();

			// Trainings and fiscal year
			List<String> trainings = Arrays.asList("Electrical Safety for Labs", "X-Ray Safety", "Laboratory Safety Training");
			String fiscalYear = "2024";

			// Get training completions based on the specified fiscal year
			Map<String, List<String>> trainingCompletions = getTrainingCompletions(trainings, fiscalYear, people);

			writeOutput2("target/output/fiscal_year_training_completions.json", trainingCompletions);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void task3() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date specifiedDate = sdf.parse("10/01/2023");
			Calendar oneMonthLater = Calendar.getInstance();
			oneMonthLater.setTime(specifiedDate);
			oneMonthLater.add(Calendar.MONTH, 1);

			StringBuilder output = new StringBuilder();

			File file = new File(TrainingsExerciseApplication.class.getResource("/trainings (correct).txt").toURI());
			Scanner scanner = new Scanner(file);
			List<Person> people = new ArrayList<>();

			parsePeople(scanner, people);
			scanner.close();

			// Check each person's trainings for expiration criteria
			for (Person person : people) {
				List<Training> expiredOrSoonToExpire = new ArrayList<>();

				for (Training training : person.getCompletions()) {
					String expiresDateString = training.getExpires();

					// Check if the expiration date is null
					if (expiresDateString != null && !expiresDateString.equals("null")) {
						Date expirationDate = sdf.parse(expiresDateString);

						// Determine the status of the training
						if (expirationDate.before(specifiedDate)) {
							expiredOrSoonToExpire.add(new Training(training.getName(), training.getTimestamp(), "Expired"));
						} else if (!expirationDate.before(specifiedDate) && expirationDate.before(oneMonthLater.getTime())) {
							expiredOrSoonToExpire.add(new Training(training.getName(), training.getTimestamp(), "Expires Soon"));
						}
					}
				}
				// format for output
				if (!expiredOrSoonToExpire.isEmpty()) {
					output.append(person.getName()).append("\n");
					for (Training training : expiredOrSoonToExpire) {
						output.append("     Training: ").append(training.getName())
								.append(", Status: ").append(training.getExpires()).append(",\n");
					}
				}
			}
			writeOutput3("target/output/expired_or_soon_to_expire_trainings.json", output.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void parsePeople(Scanner scanner, List<Person> people) {
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();

			// Check for the start of an array of people
			if (line.startsWith("{")) {
				String currentPersonName = null;
				List<Training> currentCompletions = new ArrayList<>();

				// Read the person properties
				while (scanner.hasNextLine()) {
					line = scanner.nextLine().trim();

					// Check for name
					if (line.startsWith("\"name\":")) {
						currentPersonName = extractValue(line);
						System.out.printf("Found name: %s%n", currentPersonName);
					}
					// Look for completions array
					else if (line.startsWith("\"completions\":")) {
						// Read the next line to check for the array start
						if (!line.contains("[")) {
							System.err.println("Expected [ but found: " + line);
							throw new IllegalArgumentException("Expected [ for completions array.");
						}
						if (line.contains("]")) {
							break;
						}
						// Process each training object
						while (scanner.hasNextLine()) {
							line = scanner.nextLine().trim();

							if (line.startsWith("{")) {
								String trainingName = null;
								String timestamp = null;
								String expires = null;

								// Read training details
								while (scanner.hasNextLine()) {
									line = scanner.nextLine().trim();
									if (line.startsWith("\"name\":")) {
										trainingName = extractValue(line);
									} else if (line.startsWith("\"timestamp\":")) {
										timestamp = extractValue(line);
									} else if (line.startsWith("\"expires\":")) {
										expires = extractValue(line);
										currentCompletions.add(new Training(trainingName, timestamp, expires));
										System.out.printf("Added %s for %s%n", trainingName, currentPersonName);
									}
									if (line.equals("}")) {
										break;
										// End of trainings
									}
								}
							}
							// End of the current person object
							if (line.equals("}")) {
								if (currentPersonName != null && !currentCompletions.isEmpty()) {
									people.add(new Person(currentPersonName, new ArrayList<>(currentCompletions)));
									System.out.printf("Added Person: %s with %d completions.%n", currentPersonName, currentCompletions.size());
								} else {
									System.out.printf("Skipped Person: %s with 0 completions.%n", currentPersonName);
								}
								currentCompletions.clear();
								break;
								// Exit to continue with the next person
							}
						}
					}
				}
			}
		}
	}

	public static Map<String, List<String>> getTrainingCompletions(List<String> trainings, String fiscalYear, List<Person> people) {
		Map<String, List<String>> trainingCompletions = new HashMap<>();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

		// Start and end date for the fiscal year
		String startDateStr = "07/01/" + (Integer.parseInt(fiscalYear) - 1);
		String endDateStr = "06/30/" + fiscalYear;
		Date startDate = null;
		Date endDate = null;

		try {
			startDate = sdf.parse(startDateStr);
			endDate = sdf.parse(endDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Initialize with empty lists for each training
		for (String trainingName : trainings) {
			trainingCompletions.put(trainingName, new ArrayList<>());
		}
		// Iterate through each person and their completions
		for (Person person : people) {
			for (Training training : person.getCompletions()) {
				if (trainings.contains(training.getName())) {
					try {
						Date trainingDate = sdf.parse(training.getTimestamp());
						// Check if the completion date falls within the fiscal year and add person
						if (trainingDate != null && trainingDate.compareTo(startDate) >= 0 && trainingDate.compareTo(endDate) <= 0) {
							trainingCompletions.get(training.getName()).add(person.getName());
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return trainingCompletions;
	}

	private static String extractValue(String line) {
		return line.split(":")[1].trim().replace("\"", "").replace(",", "");
	}

	private static void writeOutput(String filePath, Map<String, Integer> trainingCounts) throws IOException {
		FileWriter writer = new FileWriter(filePath);
		for (Map.Entry<String, Integer> entry : trainingCounts.entrySet()) {
			writer.write(String.format("{\"%s\": %d}%n", entry.getKey(), entry.getValue()));
		}
		writer.close();
	}

	private static void writeOutput2(String filePath, Map<String, List<String>> trainingCompletions) throws IOException {
		FileWriter writer = new FileWriter(filePath);
		writer.write("{\n");
		for (Map.Entry<String, List<String>> entry : trainingCompletions.entrySet()) {
			writer.write(String.format("  \"%s\": %s,%n", entry.getKey(), entry.getValue()));
		}
		writer.write("}\n");
		writer.close();
	}

	private static void writeOutput3(String filePath, String output) throws IOException {
		FileWriter writer = new FileWriter(filePath);
		writer.write(output);
		writer.close();
	}
}