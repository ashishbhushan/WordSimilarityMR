# Author: Ashish Bhushan
# Email: abhus@uic.edu

## Introduction
Homework assignment 1 for CS441 focuses on implementing an encoder for a Large Language Model (LLM) from scratch using distributed computing in the cloud. In this phase, we will create a mapper and reducer using Apache Hadoop to process a given text corpus, generating a YAML or CSV file containing tokens. After testing the program locally, we will deploy and run it on Amazon Elastic MapReduce (EMR).

**Video Link:** [Video Link](https://youtu.be/Ru8-tNx0Twk) (The video explains the deployment of the Hadoop application in the AWS EMR Cluster and the project structure.)

## Environment
- **OS:** Windows 11
- **IDE:** IntelliJ IDEA 2024.2.0.2 (Ultimate Edition)
- **Scala Version:** 3.3.4
- **SBT Version:** 1.10.2
- **Hadoop Version:** 3.3.6

## Running the Project
1. **Clone this repository:**
   ```bash
   git clone https://github.com/ashishbhushan/CS441HW1.git
   ```
2. **Navigate to the Project:**
   ```bash
   cd CS441HW1
   ```
3. **Open the project in IntelliJ:**  
   [How to Open a Project in IntelliJ](https://www.jetbrains.com/help/idea/import-project-or-module-wizard.html#open-project)

## Project Structure
The project comprises the following key components:

1. **Data Cleaning**  
   The initial step involves cleaning the input text corpus to remove any irrelevant data or noise. This ensures that the data is in a usable format for subsequent processing.

2. **Sharding**  
   Shards are generated from the cleaned data to facilitate parallel processing. This involves dividing the text corpus into smaller, manageable segments, allowing for efficient distributed computation.

3. **MapReduce Job**  
   The project implements one MapReduce job to encode text and generate count from the text data:
   - **Map Reduce Tokenizer Job:** Processes the input shards to count the occurrences of each word in the text corpus, generating a mapping of words along with their tokenIDs to their frequencies.

4. **Tokenization**  
   The project employs JTokkit as the tokenizer to convert the raw text into tokens, preparing the text data for the subsequent encoding process.

## Prerequisites
Before starting the project, ensure you have the following tools and accounts set up:
- **Hadoop:** Install and configure Hadoop on your local machine or cluster.
- **AWS Account:** Create an AWS account and familiarize yourself with AWS Elastic MapReduce (EMR).
- **Java:** Ensure that Java is installed and properly configured.
- **Git and GitHub:** Use Git for version control and host your project repository on GitHub.
- **IDE:** Choose an Integrated Development Environment (IDE) for coding and development.

## Usage
Follow these steps to execute the project:
Data Preparation: Ensure you have the original dataset for processing (src/main/resources/input/data.txt) - this will be passed as an argument to main.scala.
Configuration: Ensure you pass the input file path (src/main/resources/input/data.txt) and output directory (src/main/resources/output) while running main.scala. Setup the command line using intelliJ configuration if required as shown below. Please do make sure to set shorten command line parameter to jar manifest.

![image](https://github.com/user-attachments/assets/b2e58258-30a4-4dd0-9f2b-4ab0fa5c8e6d)

Shards Output Directory: Ensure src/main/resources/output/shards exists - create if it doesn't.

Execution:
Run the project via the main function - main.scala.
Executing main.scala will trigger below processes.
1. Shard generation (shardFile(lines, shardsOutputDir, linesPerShard, confmain) - main.scala:33) - Input file will be split into different files with maximum of 20 lines each (the number of files depends on the total number of lines in the input file). For now each file will contain 20 lines as per the parameter linesPerShard present in UtilitiesDir.Utilities. Once done you can see the different shards generated in the folder (src/main/resources/output/shards) as shown below.

![image](https://github.com/user-attachments/assets/0dfef966-f07a-4a83-a047-613203971108)

2. MapReduceTokenizer Job (MapReduceTokenizer.runMapReduceTokenizer(shardsOutputDir, mapRedTokenOut) - main.scala:38) - This job will take the path of the shards as the input along with an output directory to generate result. Result will contain the words obtained via each file from the shards, its respective token and the count of the words.

3. VocabStatistics (getYamlFile(text, outputDir, fs) - main.scala:45) - This will do two tasks, first task will generate the vocabulary.yaml file containing all the words along with their tokens and count. And the second task will generate a file containing only tokens (tokens.txt).
   
![image](https://github.com/user-attachments/assets/0f011125-ae9f-4df3-ba6b-c177591ed5ae)

Results: Examine the results obtained from the MapReduce job, which include the word+token count.

Location of all results obtained:
1. shards - src/main/resources/output/shards
2. mapReduceOutput - src/main/resources/output/mapRedTokenOut
3. vocabulary.yaml and tokens.txt - src/main/resources/output/vocabulary.yaml, src/main/resources/output/tokens.txt

Deployment on AWS EMR: If required, deploy the project on AWS EMR for large-scale processing.

## Conclusion
This project aims to develop a Large Language Model (LLM) encoder through a systematic approach involving data cleaning, sharding, and a MapReduce job for word counting. By utilizing JTokkit for tokenization, we are leveraging powerful tools to create a robust LLM framework. The successful completion of this project will enhance our understanding of distributed computing and natural language processing while providing practical experience with cloud-based technologies.
