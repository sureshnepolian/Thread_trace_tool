# Thread_trace_tool
Thread Dump Analyzer

Overview

This Spring Boot application exposes an API endpoint for parsing thread dumps. The API is particularly useful for performance engineers and developers who need to analyze Java thread dumps for debugging and performance tuning.

Features

Parse thread dump data and categorize each thread's details.
Filters threads based on a name pattern, if provided.
Returns a JSON response that includes each thread's name, state, method calls, and other unmatched lines.
API Endpoint
POST /api/threaddump1

Parameters
RequestBody: The raw thread dump data as a string.
threadNamePattern (Optional): A query parameter to filter thread names. Only threads that start with this pattern will be included in the output.
Sample Request

curl -X POST "http://localhost:8080/api/threaddump1?threadNamePattern=ueue-consumer" \
     -H "Content-Type: application/json" \
     -d "@your_thread_dump.txt"


     
Response Format
The API returns a JSON array where each object represents a parsed thread from the thread dump. Each object includes the following fields:

threadName: The name of the thread
threadState: The state of the thread
atCount: The number of at lines in the thread dump for this thread
methodClassMap: A map of simple class names to method names
unmatchedLines: An array of lines that could not be parsed

Limitations

The API expects well-formatted thread dumps. Malformed thread dumps may result in incorrect or incomplete parsing.
The API does not currently support parsing native thread dumps or non-Java thread dumps.
The optional threadNamePattern parameter is case-sensitive and filters based on the starting string of the thread name.
Thread Name is must

How to Run

Clone the repository and navigate to the project folder.
Run mvn spring-boot:run to start the application.
Use a tool like curl or Postman to make a POST request to the API endpoint.
This README is intended to be a quick guide. For more details, you can refer to the inline comments in the code.
