package com.example.TrainingsExercise;

import com.example.TrainingsExercise.models.Person;
import com.example.TrainingsExercise.models.Training;
import com.example.TrainingsExercise.models.TrainingStatus;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;

public class TrainingsExerciseApplication {

	public static void main(String[] args) {
		try {
			JSONParser parser = new JSONParser();
			List<Person> people = new ArrayList<>();
			parsePeople(parser, people);

			// List each completed training with a count of how many people have completed that training.
			getNumberOfCompletedTrainings(people);

			// Given a list of trainings and a fiscal year (defined as 7/1/n-1 – 6/30/n), for each specified training,
			// list all people that completed that training in the specified fiscal year.
			// Use parameters: Trainings = "Electrical Safety for Labs", "X-Ray Safety", "Laboratory Safety Training"; Fiscal Year = 2024
			getCompletedTrainingsInAFiscalYear(people);

			// Given a date, find all people that have any completed trainings that have already expired, or will expire within one month
			// of the specified date (A training is considered expired the day after its expiration date).
			// For each person found, list each completed training that met the previous criteria, with an additional field to indicate expired vs expires soon.
			// Use date: Oct 1st, 2023
			getExpiringTrainings(people);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getNumberOfCompletedTrainings(List<Person> people) {
		try {
			Map<String, Integer> trainingCounts = new HashMap<>();
			for (Person person : people) {
				Set<String> countedTrainings = new HashSet<>();
				for (Training training : person.getCompletions()) {
					String trainingName = training.getTrainingName();

					// Only count the training if the person hasn't been counted for it yet
					if (!countedTrainings.contains(trainingName)) {
						trainingCounts.put(trainingName, trainingCounts.getOrDefault(trainingName, 0) + 1);
						countedTrainings.add(trainingName);
					}
				}
			}
			writeOutput("target/output/completed_training_counts.json", trainingCounts);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getCompletedTrainingsInAFiscalYear(List<Person> people) {
		try {
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

	public static void getExpiringTrainings(List<Person> people) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date specifiedDate = sdf.parse("10/01/2023");
			Calendar oneMonthLater = Calendar.getInstance();
			oneMonthLater.setTime(specifiedDate);
			oneMonthLater.add(Calendar.MONTH, 1);

			Map<String, List<TrainingStatus>> outputMap = new HashMap<>();

			// Check each person's trainings for expiration
			for (Person person : people) {
				List<TrainingStatus> expiredOrSoonToExpire = new ArrayList<>();

				for (Training training : person.getCompletions()) {
					String expiresDateString = training.getExpires();

					// Check if the expiration date is null
					if (expiresDateString != null && !expiresDateString.equals("null")) {
						Date expirationDate = sdf.parse(expiresDateString);

						// Determine the status of the training
						if (expirationDate.before(specifiedDate)) {
							expiredOrSoonToExpire.add(new TrainingStatus(training.getTrainingName(), "Expired"));
						} else if (!expirationDate.before(specifiedDate) && expirationDate.before(oneMonthLater.getTime())) {
							expiredOrSoonToExpire.add(new TrainingStatus(training.getTrainingName(), "Expires Soon"));
						}
					}
				}
				// Add to output map if there are expired or soon-to-expire trainings
				if (!expiredOrSoonToExpire.isEmpty()) {
					outputMap.put(person.getName(), expiredOrSoonToExpire);
				}
			}
			writeOutput3("target/output/expired_or_soon_to_expire_trainings.json", outputMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void parsePeople(JSONParser parser, List<Person> people) {
		try {
			InputStream inputStream = TrainingsExerciseApplication.class.getResourceAsStream("/trainings (correct).txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			JSONArray a = (JSONArray) parser.parse(reader);

			// Loop through array of people
			for (Object o : a) {
				JSONObject person = (JSONObject) o;
				String name = (String) person.get("name");
				List<Training> completions = new ArrayList<>();
				JSONArray completionsArray = (JSONArray) person.get("completions");

				//Loop through completions array
				for (Object t : completionsArray) {
					JSONObject training = (JSONObject) t;
					String trainingName = (String) training.get("name");
					String timestamp = (String) training.get("timestamp");
					String expires = (String) training.get("expires");

					completions.add(new Training(trainingName, timestamp, expires));
					System.out.printf("Added %s training for %s%n", trainingName, name);
				}
				people.add(new Person(name, completions));
				System.out.printf("Added Person: %s with %d completions.%n", name, completions.size());
			}
		} catch (IOException | org.json.simple.parser.ParseException e) {
			e.printStackTrace();
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
				if (trainings.contains(training.getTrainingName())) {
					try {
						Date trainingDate = sdf.parse(training.getTimestamp());
						// Check if the completion date falls within the fiscal year and add person
						if (trainingDate != null && trainingDate.compareTo(startDate) >= 0 && trainingDate.compareTo(endDate) <= 0) {
							trainingCompletions.get(training.getTrainingName()).add(person.getName());
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return trainingCompletions;
	}

	private static void writeOutput(String filePath, Map<String, Integer> trainingCounts) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter(filePath);
		gson.toJson(trainingCounts, writer);
		writer.close();
	}

	private static void writeOutput2(String filePath, Map<String, List<String>> trainingCompletions) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter(filePath);
		gson.toJson(trainingCompletions, writer);
		writer.close();
	}

	private static void writeOutput3(String filePath, Map<String, List<TrainingStatus>> output) throws IOException {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		FileWriter writer = new FileWriter(filePath);
		gson.toJson(output, writer);
		writer.close();
	}
}