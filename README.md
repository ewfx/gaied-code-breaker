# 🚀 GAIED, Email classifier/routing from codebreaker team.

## 📌 Table of Contents
- [Introduction](#introduction)
- [What It Does](#what-it-does)
- [How We Built It](#how-we-built-it)
- [Challenges We Faced](#challenges-we-faced)
- [How to Run](#how-to-run)
- [Tech Stack](#tech-stack)
- [Team](#team)

---

## 🎯 Introduction
The application will run the .eml file from a configurable folder structure and extract the email content along with the attachment then pass the content to the pre-trained LLM model (customise pre-trained mistral model runs on ollama server deployed in local or in any VM instance) with a proper prompt to get the required response and then parsing the response to java object and returning the same as json


## ⚙️ What It Does
Email classification by using a pre-trained LLM model to get the desired response

## 🛠️ How We Built It
LLM used is mistral LLM through ollama framework, so that locally we can deploy, we can use intranet, internet is not required, completely free. customisable, have our own model created and used by keeping based model as mistral, based on the server capacity we can easily switch to larger model LLM3, Gemini, deepseek R1 as ollama has all the integration and we can interact with ollama.

## 🚧 Challenges We Faced
Major challenge is finding out a model which supports fine tune with open source capacity.

## 🏃 How to Run
1. Clone the repository  
2. We have placed our code inside the code folder which is a springboot application.
3. We can run the code and place the .eml file inside D:\loan_emails_detailed
4. We can hit the GET URL http://localhost:8080/emailroute/init
   ```

## 🏗️ Tech Stack
- 🔹 Backend: Springboot for email classification, Python to train the model.
- 🔹 Other: Mistral LLM using ollama framework

## 👥 Team
Nithya, Uttakarsh, Anil