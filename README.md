# BandPal
An Android social media app that allows musicians to meet and form bands within their local area. With BandPal, users can fill their profile with their musical passions then find (and be found by) those who share those passions.

BandPal was my dissertation project submitted for my Computer Science MSc at the University of Birmingham.

## Overview

Centered around a search and match ‘points-based’ algorithm, BandPal takes a user’s influences (via LastFM artists API), instruments (via MusicBrainz’s instruments API), location (via Google Maps API), age and search-radius to produce likeminded local matches to be connected and chatted with in-app.

The app utilises gamification elements in the form of a trophy system. Trophies are rewards for building a full profile and using BandPal to its full potential. The more trophies the higher a user appears in searches and the more likely they are to find accurate matches.

Go to the ‘info’ folder for a full report on the app’s development and features, as well as videos of common user journeys.

### Tools
- Kotlin (back and frontend) and (a v.small amount of) server-side JavaScript for device-to-device notifications
- Developed in Android Studio
- Uses Google Firebase for database
- Incorporates LastFM, MusicBrainz and Google Maps APIs
- Allows login via pre-existing Google and Facebook accounts
