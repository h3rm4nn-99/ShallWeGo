# ShallWeGo
Crowdsourcing mobility in the smartphone era

This repository contains the code and the documentation for my Bachelor's Degree Thesis Project, developed back in Summer 2021.

This project consists of a PoC platform that enables public transportation users to share and verify information about train and buses in a specific location. 
This platform is built by three main components:

- An **Android application** built with Java and (mainly) using the OsmDroid library to implement maps;
- A **back-end REST Server** built with Java EE and the Spring Boot Framework. It features a **Genetic Algorithm** to select the best reviewers for a specific information;
- A (self hosted) **OpenStreetMap Nominatim Geocoding Server** used mainly to provide geographical information based on a Latitude and Longitude pair.
