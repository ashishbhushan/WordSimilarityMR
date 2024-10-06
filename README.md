# Author: Ashish Bhushan
# Email: abhus@uic.edu

## Introduction
Homework assignment 1 for CS441 focuses on implementing an encoder for a Large Language Model (LLM) from scratch using distributed computing in the cloud. In this phase, we will create a mapper and reducer using Apache Hadoop to process a given text corpus, generating a YAML or CSV file containing tokens. After testing the program locally, we will deploy and run it on Amazon Elastic MapReduce (EMR).

**Video Link:** [Video Link] (The video explains the deployment of the Hadoop application in the AWS EMR Cluster and the project structure.)

## Environment
- **OS:** Windows 11
- **IDE:** IntelliJ IDEA 2024.2.0.2 (Ultimate Edition)
- **Scala Version:** 3.3.4
- **SBT Version:** 1.10.2
- **Hadoop Version:** 3.3.6

## Running the Project
1. **Clone this repository:**
   ```bash
   git clone https://github.com/ashishbhushan/CS441.git
   git checkout master
   ```
2. **Navigate to the Project:**
   ```bash
   cd CS441
   ```
3. **Open the project in IntelliJ:**  
   [How to Open a Project in IntelliJ](https://www.jetbrains.com/help/idea/import-project-or-module-wizard.html#open-project)

## Project Structure
The project comprises the following key components:

1. **Data Cleaning**  
   The initial step involves cleaning the input text corpus to remove any irrelevant data or noise. This ensures that the data is in a usable format for subsequent processing.

2. **Sharding**  
   Shards are generated from the cleaned data to facilitate parallel processing. This involves dividing the text corpus into smaller, manageable segments, allowing for efficient distributed computation.

3. **MapReduce Jobs**  
   The project implements three main MapReduce jobs to encode text and generate count from the text data:
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

## Conclusion
This project aims to develop a Large Language Model (LLM) encoder through a systematic approach involving data cleaning, sharding, and a MapReduce job for word counting. By utilizing JTokkit for tokenization, we are leveraging powerful tools to create a robust LLM framework. The successful completion of this project will enhance our understanding of distributed computing and natural language processing while providing practical experience with cloud-based technologies.
